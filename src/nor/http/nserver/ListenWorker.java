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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nor.http.server.HttpRequestHandler;

class ListenWorker implements Runnable, Closeable{

	private final String hostname;
	private final int port;

	private final Closeable[] workers;
	private final ExecutorService pool;

	private final Queue<Connection> queue = new Queue<Connection>();

	private Selector selector;
	private boolean running = true;

	public ListenWorker(final String hostname, final int port, final HttpRequestHandler handler, final int nThreads){

		this.hostname = hostname;
		this.port = port;

		this.workers = new Closeable[nThreads];
		this.pool = Executors.newFixedThreadPool(nThreads);
		for(int i = 0; i != nThreads; ++i){

			final ServiceWorker w = new ServiceWorker(this.queue, handler);
			this.workers[i] = w;
			this.pool.execute(w);

		}

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

			// セレクタにイベントが通知されるごとに処理
			System.out.println("Start listening.");
			while (this.running) {

				// セレクタにイベントが発生するまでブロック
				selector.select();

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

								// ノンブロッキングモード
								final Connection con = new Connection(socket, selector);

								// キューにこのコネクションを追加
								this.queue.insert(con);

							}

						}else{

							// クライアントとの通信処理
							final Object o = key.attachment();
							if(o != null){
								 assert(o instanceof Connection);

								 final Connection con = (Connection)o;
								con.handle(key);

							}

						}
						iter.remove();

					}catch(final CancelledKeyException e){

						final Object o = key.attachment();
						if(o instanceof Connection){

							((Connection)o).close();

						}

						// e.printStackTrace();

					}catch(final IOException e){

						key.cancel();
						e.printStackTrace();

					}

				}

			}

			selector.close();
			System.out.println("End listening.");


		}catch(final IOException e){

			e.printStackTrace();

		}

	}

	@Override
	public void close() throws IOException{

		for(final Closeable w : this.workers){

			w.close();

		}
		this.pool.shutdownNow();
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
		System.out.println("Bind socket to port " + this.port);

		// セレクタにサーバソケットチャンネルを登録
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);

	}

}
