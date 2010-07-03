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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import nor.util.log.EasyLogger;

class SocketChannelOutputStream extends OutputStream{

	/**
	 * セレクタに登録されているキー．
	 * null の場合，ストリームは閉じられている．
	 */
	private SelectionKey key;

	private final ByteBuffer buffer;
	private int timeout = 1000;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(SocketChannelOutputStream.class);

	public SocketChannelOutputStream(final SelectionKey key){

		this.key = key;
		this.buffer = ByteBuffer.allocate(Connection.BufferSize);

	}

	//============================================================================
	//  OntputStream のオーバーライド
	//============================================================================
	@Override
	public void write(int b) throws IOException {

		if(this.key != null){

			if(this.available() == 0){

				this.flush();
				if(this.available() == 0){

					this.key = null;
					return;

				}

			}

			try{

				this.buffer.put((byte)b);

			}catch(final BufferOverflowException e){

				LOGGER.severe(e.getMessage() + " pos " + this.buffer.position() + ", lim " + this.buffer.limit() + ", cap " + this.buffer.capacity());

			}

		}

	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		if(this.key != null){

			while(len != 0){

				if(this.available() == 0){

					this.flush();
					if(this.available() == 0){

						this.key = null;
						return;

					}

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

			}

		}

	}

	@Override
	public synchronized void flush() throws IOException {

		if(this.key != null){

			try {

				this.key.interestOps(this.key.interestOps() | SelectionKey.OP_WRITE);
				this.key.selector().wakeup();

				this.wait(this.timeout);

			}catch(final InterruptedException e){

				this.key = null;
				LOGGER.throwing("flush", e);
				throw new IOException(e);

			}catch(final CancelledKeyException e){

				this.key = null;
				LOGGER.throwing("flush", e);
				throw new IOException(e);

			}

		}

	}

	@Override
	public void close() throws IOException {

		this.flush();
		if(this.key != null){

			if(this.key.isValid()){

				this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_WRITE);

			}

			this.key = null;

		}

	}

	//============================================================================
	// SocketChannel との通信
	//============================================================================
	public synchronized int storeToChannel(final WritableByteChannel channel){

		int ret = -1;
		try{

			if(this.key != null){

				this.buffer.flip();
				ret = channel.write(this.buffer);
				this.buffer.clear();

				this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_WRITE);

			}

		}catch(final IOException e){

			LOGGER.warning(e.getMessage());
			this.key = null;

		}finally{

			this.notify();

		}

		return ret;

	}

	private int available(){

		return this.buffer.limit() - this.buffer.position();

	}

}
