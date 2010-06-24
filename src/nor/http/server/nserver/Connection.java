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
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

class Connection implements Closeable{

	private final SelectionKey key;

	private final SocketChannelInputStream in;
	private final SocketChannelOutputStream out;

	private final SocketChannel schannel;

	public Connection(final SocketChannel schannel, final Selector selector) throws IOException{

		// ソケットをノンブロッキングモードにする
		this.schannel = schannel;
		this.schannel.configureBlocking(false);

		// ソケットをセレクタに登録
		this.key = this.schannel.register(selector, 0);

		// 登録キーにこのオブジェクトを添付
		this.key.attach(this);

		this.in = new SocketChannelInputStream(this.key);
		this.out = new SocketChannelOutputStream(this.key);


	}

	public int loadFromChannel(){

		return this.in.loadFromChannel(this.schannel);

	}

	public int storeToChannel(){

		return this.out.storeToChannel(this.schannel);

	}


	@Override
	public void close() throws IOException{

		final Selector selector = this.key.selector();
		this.key.attach(null);
		this.key.channel().close();

		this.key.cancel();

		selector.wakeup();

	}

	public InputStream getInputStream(){

		return this.in;

	}

	public OutputStream getOutputStream(){

		return this.out;

	}

	@Override
	public String toString(){

		return String.format("Connection from the %s", this.schannel.socket());

	}

}
