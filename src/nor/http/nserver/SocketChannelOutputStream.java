/**
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
package nor.http.nserver;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;

import nor.util.log.EasyLogger;

class SocketChannelOutputStream extends OutputStream{

	private final SelectionKey key;
	private final ByteBuffer buffer;
	private int timeout = 1000;

	private boolean isAlive = true;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(SocketChannelOutputStream.class);

	public SocketChannelOutputStream(final SelectionKey key){

		this.key = key;
		this.buffer = ByteBuffer.allocate(1024*64);

	}

	//============================================================================
	//  OntputStream のオーバーライド
	//============================================================================
	@Override
	public void write(int b) throws IOException {

		if(this.isAlive){

			if(this.available() == 0){

				this.flush();

			}

			this.buffer.put((byte)b);

		}

	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		if(this.isAlive){

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

			}

		}

	}

	@Override
	public synchronized void flush() throws IOException {

		if(this.isAlive){

			try {

				this.key.interestOps(this.key.interestOps() | SelectionKey.OP_WRITE);
				this.key.selector().wakeup();

				this.wait(this.timeout);

			}catch(final InterruptedException e){

				LOGGER.warning(e.getMessage());
				throw new IOException(e);

			}catch(final CancelledKeyException e){

				LOGGER.throwing("flush", e);
				throw new IOException(e);

			}

			if(!this.isAlive){

				throw new IOException();

			}

		}

	}

	@Override
	public void close() throws IOException {

		this.flush();

	}

	//============================================================================
	// SocketChannel との通信
	//============================================================================
	public synchronized int storeToChannel(final WritableByteChannel channel){

		int ret = -1;
		try{

			this.buffer.flip();
			ret = channel.write(this.buffer);
			this.buffer.clear();

			this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_WRITE);

		}catch(final IOException e){

			this.isAlive = false;
			LOGGER.throwing("storeToChannel", e);

		}finally{

			this.notify();

		}

		return ret;

	}

	private int available() throws IOException {

		return this.buffer.limit() - this.buffer.position();

	}

}
