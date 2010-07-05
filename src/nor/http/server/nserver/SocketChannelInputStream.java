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

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;

import nor.util.log.Logger;

/**
 *
 *
 *
 * このストリームを閉じた場合，このストリームに関する操作のみ不能になる．
 * キーはセレクタに登録されたまま．キーをセレクタから削除する場合は，コネクションのクローズを呼ぶ．
 *
 * @author Junpei
 *
 */
class SocketChannelInputStream extends InputStream{

	/**
	 * セレクタに登録されているキー．
	 * null の場合，ストリームは閉じられている．
	 */
	private SelectionKey key;

	private final ByteBuffer buffer;

	private static final Logger LOGGER = Logger.getLogger(SocketChannelInputStream.class);

	//============================================================================
	//  Constants
	//============================================================================
	private static final int BufferSize;
	private static final int Timeout;


	//============================================================================
	//  Constractor
	//============================================================================
	public SocketChannelInputStream(final SelectionKey key){

		this.key = key;
		this.buffer = ByteBuffer.allocate(BufferSize);
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

		if(this.key == null){

			return -1;

		}

		if(this.available() == 0){

			this.load();
			if(this.available() == 0){

				this.close();
				return -1;

			}

		}

		return this.buffer.get() & 0xff;

	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		if(this.key == null){

			return -1;

		}

		if(this.available() == 0){

			this.load();
			if(this.available() == 0){

				this.close();
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
	public int available(){

		return this.buffer.limit() - this.buffer.position();

	}

	@Override
	public void close(){

		if(this.key != null){

			if(this.key.isValid()){

				this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_READ);

			}

			this.key = null;

		}

	}

	//----------------------------------------------------------------------------
	//  SocketChannel との通信
	//----------------------------------------------------------------------------
	public synchronized int loadFromChannel(final ReadableByteChannel channel){
		LOGGER.entering("loadFromChannel", channel);

		int ret = -1;
		try{

			if(this.key != null){

				this.buffer.clear();
				ret = channel.read(this.buffer);
				this.buffer.flip();
				this.key.interestOps(this.key.interestOps() & ~SelectionKey.OP_READ);

			}

		}catch(final IOException e){

			LOGGER.warning(e.getMessage());
			this.close();

		}finally{

			this.notify();

		}

		LOGGER.exiting("loadFromChannel", ret);
		return ret;

	}

	//============================================================================
	//  Private methods
	//============================================================================
	private synchronized void load() throws IOException{
		LOGGER.entering("load");

		if(this.key != null){

			try {

				this.key.interestOps(this.key.interestOps() | SelectionKey.OP_READ);
				this.key.selector().wakeup();
				this.wait(Timeout);

			}catch(final InterruptedException e) {

				this.close();
				Thread.currentThread().interrupt();

				LOGGER.throwing("load", e);
				throw new IOException(e);

			}catch(final CancelledKeyException e){

				this.close();
				LOGGER.throwing("load", e);
				throw new IOException(e);

			}

		}

		LOGGER.exiting("load");
	}

	//============================================================================
	//  Class constructor
	//============================================================================
	static{

		final String classname = SocketChannelInputStream.class.getName();
		BufferSize = Integer.valueOf(System.getProperty(String.format("%s.BufferSize", classname)));
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));

	}

}
