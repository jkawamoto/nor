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

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import nor.http.server.HttpRequestHandler;
import nor.util.log.EasyLogger;

class ListenWorker implements Runnable, Closeable{

	private final String hostname;
	private final int port;

	private final ThreadManager tmanager;;

	private Selector selector;
	private boolean running = true;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(ListenWorker.class);

	public ListenWorker(final String hostname, final int port, final HttpRequestHandler handler, final int minThreads, final int queueSize, final int waitTime){

		this.hostname = hostname;
		this.port = port;

		this.tmanager = new ThreadManager(handler, minThreads, queueSize, waitTime);

	}

	@Override
	public void run() {

		// スレッドの名前を変更
		Thread.currentThread().setName(this.getClass().getSimpleName());

		try{

			// セレクタの用意
			this.selector = Selector.open();

			// サーバソケットチャネルの作成
			this.createServerChannel(selector);
			LOGGER.info("Starts listening");

			// セレクタにイベントが通知されるごとに処理
			while (this.running) {

				// セレクタにイベントが発生するまでブロック
				final int nc = selector.select();
				LOGGER.finest("Begins a selection (" + nc + ")");

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

								LOGGER.finest("Receive an accsptable key from " + socket.socket());

								// ノンブロッキングモード
								final Connection con = new Connection(socket, this.selector);

								// キューにこのコネクションを追加
								this.tmanager.offer(con);

							}else{

								LOGGER.finest("Receive an accsptable but null key");
								key.cancel();

							}

						}else{

							// クライアントとの通信処理
							final Object o = key.attachment();
							if(o != null){
								assert(o instanceof Connection);

								final Connection con = (Connection)o;
								int ret = -1;
								if(key.isReadable()){

									LOGGER.finest("Receives a readable key from the " + con.toString());
									ret = con.loadFromChannel();

								}else if(key.isWritable() && key.isValid()){

									LOGGER.finest("Receives a writable key from the " + con.toString());
									ret = con.storeToChannel();

								}

								if(ret == -1){

									con.close();

								}

							}else{

								LOGGER.finest("Receives a no associated key");
								key.cancel();

							}

						}

					}catch(final CancelledKeyException e){

						LOGGER.throwing("run", e);

						final Object o = key.attachment();
						if(o instanceof Connection){

							((Connection)o).close();

						}

					}finally{

						iter.remove();

					}

				}

				LOGGER.finest("Ends the selection");

			}

			selector.close();
			LOGGER.info("Ends listening");


		}catch(final IOException e){

			LOGGER.throwing("run", e);
			LOGGER.severe("ListenWorker is stopped by " + e.toString());
			e.printStackTrace();

		}

	}

	@Override
	public void close() throws IOException{

		this.running = false;
		if(this.selector != null){

			this.selector.wakeup();

		}

	}

	private void createServerChannel(final Selector selector) throws IOException{

		// サーバソケットチャネルの作成
		final ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		serverChannel.socket().bind(new InetSocketAddress(this.hostname, this.port));
		LOGGER.info("Bind socket to port: " + this.port);

		// セレクタにサーバソケットチャンネルを登録
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

	}

}
