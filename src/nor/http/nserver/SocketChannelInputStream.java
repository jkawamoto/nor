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
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

import nor.util.log.EasyLogger;

class SocketChannelInputStream extends InputStream{

	private final SelectionKey key;
	private final ByteBuffer buffer;
	private boolean closed = false;

	private int timeout = 1000;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(SocketChannelInputStream.class);

	public SocketChannelInputStream(final SelectionKey key){

		this.key = key;
		this.buffer = ByteBuffer.allocate(1024*64);
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

			return -1;

		}

		if(this.available() == 0){

			this.load();
			if(this.available() == 0){

				return -1;

			}

		}

		return this.buffer.get() & 0xff;

	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		if(this.closed){

			return -1;

		}

		if(this.available() == 0){

			this.load();
			if(this.available() == 0){

				return -1;

			}

		}

		// 有効サイズとlenの小さい方分だけコピーする
		final int available = this.available();
		if(len > available){


			this.buffer.get(b, off, available);
			return available;

		}else{

			this.buffer.get(b, off, len);
			return len;

		}

	}

	@Override
	public int available() throws IOException {

		return this.buffer.limit() - this.buffer.position();

	}

	@Override
	public void close() throws IOException {

		if(this.key.isValid()){

			this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_READ);

		}

	}

	//----------------------------------------------------------------------------
	//  SocketChannel との通信
	//----------------------------------------------------------------------------
	public synchronized int loadFromChannel(final ReadableByteChannel channel){
		LOGGER.entering("loadFromChannel", channel);

		int ret = -1;
		try{

			this.buffer.clear();
			ret = channel.read(this.buffer);
			this.buffer.flip();
			this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_READ);

		}catch(final IOException e){

			LOGGER.throwing("loadFromChannel", e);

		}finally{

			this.notify();

		}

		LOGGER.exiting("loadFromChannel", ret);
		return ret;

	}

	//============================================================================
	//  private methods
	//============================================================================
	private synchronized void load(){
		LOGGER.entering("load");

		try {

			this.key.interestOps(this.key.interestOps() | SelectionKey.OP_READ);
			this.key.selector().wakeup();
			this.wait(this.timeout);

		}catch(final InterruptedException e) {

			LOGGER.throwing("load", e);

		}catch(final CancelledKeyException e){

			LOGGER.throwing("load", e);

		}

		LOGGER.exiting("load");
	}

}
