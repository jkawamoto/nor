/*
 *  Copyright (C) 2010 Junpei Kawamoto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nor.http.server.nserver;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import nor.network.SelectionEventHandler;
import nor.network.SelectionEventHandlerAdapter;
import nor.network.SelectionWorker;
import nor.util.log.Logger;

/**
 *
 * @author Junpei Kawamoto
 * @since 0.2
 */
class Connection implements Closeable{

	private boolean closed;
	private SelectableChannel delegation;

	private final SelectionWorker selector;
	private final SelectionEventHandler handler;

	private final SocketChannelInputStream in;
	private final SocketChannelOutputStream out;

	private final SelectionKey key;

	private static final Logger LOGGER = Logger.getLogger(Connection.class);

	//============================================================================
	//  Constants
	//============================================================================
	private static final String AlreadyClosed = "This stream has already closed.";

	//============================================================================
	//  Constructor
	//============================================================================
	public Connection(final SocketChannel ch, final SelectionWorker selector) throws IOException{

		this.closed = false;

		this.selector = selector;
		this.handler = new ConnectionHandler();

		this.in = new SocketChannelInputStream();
		this.out = new SocketChannelOutputStream();

		this.key = this.selector.register(ch, 0, this.handler);

	}

	//============================================================================
	//  Public methods
	//============================================================================
	public InputStream getInputStream(){

		return this.in;

	}

	public OutputStream getOutputStream(){

		return this.out;

	}

	public boolean closed(){

		return this.closed;

	}

	@Override
	public void close() throws IOException{

		this.in.close();
		this.out.close();

	}

	@Override
	public String toString(){

		return String.format("%s(key = %s)", this.getClass().getSimpleName(), this.key);

	}

	/**
	 * Request binding this connection to the chennel.
	 * close が呼ばれた後に効果が出る
	 *
	 * @param ch
	 * @throws IOException
	 */
	public void requestDelegation(final SelectableChannel ch) throws IOException{

		this.delegation = ch;

	}

	//============================================================================
	//  Private methods for communicating to the inner streams
	//============================================================================
	private void addOps(final int ops){

		if(this.key.isValid()){

			this.key.interestOps(this.key.interestOps() | ops);

		}

	}

	private void removeOps(final int ops){

		if(this.key.isValid()){

			this.key.interestOps(this.key.interestOps() & ~ops);

		}

	}

	private void wakeup(){

		this.key.selector().wakeup();

	}

	/**
	 * Close event handler.
	 * This method will be called when the SocketChannelInputStream or SocketChannelOutputStream which are associated with this Connection is closed.
	 * This method check whether both streams are closed or not. If closed, the SelectionKey will be cancelled.
	 */
	private void onCloseStream(){

		if(this.in.closed() && this.out.closed()){

			try {

				if(this.delegation != null){

					LOGGER.fine("onCloseStream", "Close streams and delegate to {0}.", this.delegation);
					new Delegator(this.key, this.delegation, this.selector);

				}else{

					this.key.cancel();
					this.key.attach(null);
					this.key.channel().close();

				}

			} catch (final IOException e) {

				LOGGER.warning("onCloseStream", e.getMessage());
				LOGGER.catched(Level.FINE, "onCloseStream", e);

			}


			this.closed = true;

		}

	}

	//============================================================================
	//  Private inner classes
	//============================================================================
	private final class ConnectionHandler extends SelectionEventHandlerAdapter{

		@Override
		public void onRead(final ReadableByteChannel ch){

			Connection.this.in.onRead(ch);

		}

		@Override
		public void onWrite(final WritableByteChannel ch){

			Connection.this.out.onWrite(ch);

		}

	}

	private final class SocketChannelInputStream extends InputStream{

		private boolean closed;
		private IOException error;
		private final ByteBuffer buffer;

		//============================================================================
		//  Constractor
		//============================================================================
		public SocketChannelInputStream(){

			this.closed = false;

			this.buffer = ByteBuffer.allocate(NServer.BufferSize);
			this.buffer.limit(0);

		}

		//============================================================================
		//  public methods
		//============================================================================
		//----------------------------------------------------------------------------
		//  InputStream のオーバーライド
		//----------------------------------------------------------------------------
		@Override
		public int read() throws IOException {

			if(this.closed){

				final IOException e = new IOException(AlreadyClosed);
				LOGGER.throwing(this.getClass(), "read", e);

				throw e;

			}else{

				if(this.available() == 0){

					this.load();
					if(this.available() == 0){

						return -1;

					}

				}

				return this.buffer.get() & 0xff;

			}

		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {

			if(b == null){

				final NullPointerException e = new NullPointerException("b is null");
				LOGGER.throwing(this.getClass(), "read", e);

				throw e;

			}else if(off < 0 || len < 0 || len > b.length - off){

				final IndexOutOfBoundsException e = new IndexOutOfBoundsException("off < 0 or len < 0 or len > b.length - off");
				LOGGER.throwing(this.getClass(), "read", e);

				throw e;

			}else if(this.closed){

				final IOException e = new IOException(AlreadyClosed);
				LOGGER.throwing(this.getClass(), "read", e);

				throw e;

			}else{

				if(this.available() == 0){

					this.load();
					if(this.available() == 0){

						return -1;

					}

				}

				// 有効サイズと len の小さい方分だけコピーする
				final int available = this.available();
				if(len > available){

					this.buffer.get(b, off, available);
					return available;

				}else{

					this.buffer.get(b, off, len);
					return len;

				}

			}

		}

		@Override
		public int available(){

			return this.buffer.limit() - this.buffer.position();

		}

		@Override
		public void close(){

			if(!this.closed){

				this.closed = true;
				LOGGER.fine(this.getClass(), "close", "InputStream by {0} is closed.", Connection.this);

				Connection.this.removeOps(SelectionKey.OP_READ);
				Connection.this.onCloseStream();

			}

		}

		//----------------------------------------------------------------------------
		//  Connection との通信
		//----------------------------------------------------------------------------
		public boolean closed(){

			return this.closed;

		}

		public synchronized void onRead(final ReadableByteChannel channel){

			try{

				this.buffer.clear();
				if(channel.read(this.buffer) == -1){

					/*
					 * Reach the end-of-file
					 */
					Connection.this.removeOps(SelectionKey.OP_READ);
					this.notify();

				}else if(this.available() != 0){

					/*
					 * Read some data from that channel.
					 */
					this.buffer.flip();
					Connection.this.removeOps(SelectionKey.OP_READ);
					this.notify();

				}

			}catch(final IOException e){

				/*
				 * The read method of that channel is faild.
				 * It means this channel will be closed.
				 * In this case, the buffer has no content, then the load method returns -1.
				 * Finally, it's expected to close this stream by uses.
				 */
				LOGGER.fine(this.getClass(), "onRead", "Socket error ({0}) by {1}", e.getMessage(), Connection.this);
				LOGGER.catched(Level.FINE, this.getClass(), "onRead", e);

				Connection.this.removeOps(SelectionKey.OP_READ);
				this.error = e;
				this.notify();

			}

		}

		//============================================================================
		//  Private methods
		//============================================================================
		private synchronized void load() throws IOException{

			if(!this.closed){

				this.error = null;
				Connection.this.addOps(SelectionKey.OP_READ);
				Connection.this.wakeup();

				try {

					this.wait(NServer.Timeout);

				}catch(final InterruptedException e) {

					LOGGER.catched(Level.FINE, this.getClass(), "load", e);
					Thread.currentThread().interrupt();

				}

				if(this.error != null){

					Connection.this.close();

					final IOException e = new IOException("An error is occuered", this.error);
					LOGGER.throwing(this.getClass(), "load", e);

					throw e;

				}

			}

		}

	}

	private final class SocketChannelOutputStream extends OutputStream{

		private boolean closed;
		private boolean flushed;
		private boolean wrote;

		private final ByteBuffer buffer;

		//============================================================================
		//  Constractor
		//============================================================================
		public SocketChannelOutputStream(){

			this.closed = false;
			this.flushed = true;
			this.wrote = false;

			this.buffer = ByteBuffer.allocate(NServer.BufferSize);

		}

		//============================================================================
		//  public methods
		//============================================================================
		//----------------------------------------------------------------------------
		//  OntputStream のオーバーライド
		//----------------------------------------------------------------------------
		@Override
		public void write(int b) throws IOException {

			if(this.closed){

				final IOException e = new IOException(AlreadyClosed);
				LOGGER.throwing(this.getClass(), "write", e);

				throw e;

			}else{

				if(this.available() == 0){

					this.flush();

				}

				this.buffer.put((byte)b);
				this.flushed = false;

			}

		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {

			if(this.closed){

				final IOException e = new IOException(AlreadyClosed);
				LOGGER.throwing(this.getClass(), "write", e);

				throw e;

			}else{

				while(len != 0){

					if(this.available() == 0){

						this.flush();

					}

					final int size = this.available();
					if(len > size){

						this.buffer.put(b, off, size);
						off += size;
						len -= size;

					}else{

						this.buffer.put(b, off, len);
						off += len;
						len -= len;

					}
					this.flushed = false;

				}

			}

		}

		@Override
		public synchronized void flush() throws IOException {

			this.flushed = true;

			if(this.closed){

				final IOException e = new IOException(AlreadyClosed);
				LOGGER.throwing(this.getClass(), "flush", e);

				throw e;

			}else if(this.buffer.position() != 0){

				LOGGER.finer("flush", "Start flush.");

				this.buffer.flip();
				Connection.this.addOps(SelectionKey.OP_WRITE);
				Connection.this.wakeup();

				try {

					this.wait(NServer.Timeout);

				} catch (final InterruptedException e) {

					LOGGER.catched(Level.FINE, this.getClass(), "flush", e);
					Thread.currentThread().interrupt();

				}

				LOGGER.finer("flush", "End flush.");

				if(!this.wrote){

					/*
					 * If onWrite method failed, this connection will be closed.
					 * Then an exception (IOException) will be thrown.
					 */
					Connection.this.close();

					final IOException e = new IOException("Do not write to the stream.");
					LOGGER.throwing(this.getClass(), "flush", e);

					throw e;

				}

			}

		}

		@Override
		public void close() throws IOException {

			try{

				if(!this.flushed){

					this.flush();

				}

			}finally{

				if(!this.closed){

					this.closed = true;
					LOGGER.fine(this.getClass(), "close", "OutputStream by {0} is closed.", Connection.this);

					Connection.this.removeOps(SelectionKey.OP_WRITE);
					Connection.this.onCloseStream();

				}

			}

		}

		//----------------------------------------------------------------------------
		// Connection との通信
		//----------------------------------------------------------------------------
		public boolean closed(){

			return this.closed;

		}

		public synchronized void onWrite(final WritableByteChannel channel){

			try{

				this.wrote = false;
				channel.write(this.buffer);

				/*
				 * Finished to send the bufferd data to the channel.
				 */
				if(this.available() == 0){

					Connection.this.removeOps(SelectionKey.OP_WRITE);

					this.buffer.clear();
					this.wrote = true;

					this.notify();

				}

			}catch(final IOException e){

				/*
				 *  If an exception is thrown, it means the write method fails.
				 * Then the clear method of the buffer won't be called,
				 * so that an IOException will be thrown from the flush method and this stream will be closed.
				 */
				LOGGER.fine(this.getClass(), "onWrite", "Socket error ({0}) by {1}", e.getMessage(), Connection.this);
				LOGGER.catched(Level.FINE, this.getClass(), "onWrite", e);

				Connection.this.removeOps(SelectionKey.OP_WRITE);
				this.notify();

			}

		}

		//============================================================================
		//  Private methods
		//============================================================================
		private int available(){

			return this.buffer.limit() - this.buffer.position();

		}

	}

}