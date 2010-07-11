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
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.logging.Level;

import nor.util.log.Logger;

class ListenWorker implements Runnable, Closeable{

	private final String hostname;
	private final int port;

	private final ThreadManager tmanager;

	private final Selector selector;
	private boolean running = true;

	private static final Logger LOGGER = Logger.getLogger(ListenWorker.class);

	//============================================================================
	//  Constractor
	//============================================================================
	public ListenWorker(final String hostname, final int port, final ThreadManager manager) throws IOException{

		this.hostname = hostname;
		this.port = port;
		this.tmanager = manager;

		this.selector = Selector.open();

	}


	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public void run() {

		// スレッドの名前を変更
		Thread.currentThread().setName(this.getClass().getSimpleName());

		try{


			// サーバソケットチャネルの作成
			this.createServerChannel(selector);
			LOGGER.info("Starts listening");

			// セレクタにイベントが通知されるごとに処理
			while (this.running) {

				// セレクタにイベントが発生するまでブロック
				final int nc = selector.select();
				LOGGER.finest("run", "Begin a selection ({0} selected keys, {1} registrated keys)", nc, selector.keys().size());

				// 獲得したイベントごとに処理を実行
				final Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while(iter.hasNext()) {

					final SelectionKey key = iter.next();
					try{

						if (key.isAcceptable()) {

							// 接続要求
							final ServerSocketChannel serverChannel = (ServerSocketChannel)key.channel();
							final SocketChannel socket = serverChannel.accept();
							if(socket != null){

								LOGGER.finest("run", "Receive an accsptable key from {0}", socket.socket());

								// ノンブロッキングモード
								final Connection con = new Connection(socket, this.selector);

								// キューにこのコネクションを追加
								this.tmanager.offer(con);

							}else{

								LOGGER.finest("Receive an accsptable but null key");
								LOGGER.fine("run", "Cancel the key; {0}", key);
								key.cancel();

							}

						}else{

							// クライアントとの通信処理
							final Object o = key.attachment();
							if(o != null){
								assert(o instanceof Connection);

								final Connection con = (Connection)o;
								con.handle();

							}else{

								LOGGER.finest("Receives a no associated key");

								LOGGER.fine("run", "Cancel the key; {0}", key);
								key.cancel();

							}

						}

					}catch(final CancelledKeyException e){

						LOGGER.info(e.getMessage());
						LOGGER.catched(Level.FINE, "run", e);

						final Object o = key.attachment();
						if(o instanceof Connection){

							((Connection)o).close();

						}

					}

				}

				this.selector.selectedKeys().clear();
				LOGGER.finest("Ends the selection");

			}

			LOGGER.info("Ends listening");

		}catch(final ClosedSelectorException e){

			/* If the selecter is already selecting when this application will go to exit,
			 * this exception is threw.
			 */
			LOGGER.catched(Level.FINE, "run", e);

		}catch(final IOException e){

			LOGGER.severe("ListenWorker is stopped by " + e.toString());
			LOGGER.catched(Level.INFO, "run", e);

		}

	}

	/* (非 Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException{

		this.running = false;
		this.selector.close();

	}


	//============================================================================
	//  Private methods
	//============================================================================
	private void createServerChannel(final Selector selector) throws IOException{

		// サーバソケットチャネルの作成
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);

		// セレクタにサーバソケットチャンネルを登録
		final InetSocketAddress addr = new InetSocketAddress(this.hostname, this.port);
		serverChannel.socket().bind(addr);
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

		LOGGER.info("createServerChannel", "Bind socket to {0}", addr);

	}

}
