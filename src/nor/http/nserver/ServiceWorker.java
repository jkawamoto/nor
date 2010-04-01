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

import static nor.http.HeaderName.Connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;
import java.util.logging.Logger;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;

class ServiceWorker implements Runnable, Closeable{

	private final ThreadManager manager;
	private final HttpRequestHandler handler;

	private boolean running = true;

	private static final Logger LOGGER = Logger.getLogger(ServiceWorker.class.getName());

	public ServiceWorker(final ThreadManager manager, final HttpRequestHandler handler){

		this.manager = manager;
		this.handler = handler;

	}

	/**
	 * 要求に答える．
	 */
	@Override
	public synchronized void run() {

		while(this.running){

			final Connection con = this.manager.poll();
			if(con == null){

				break;

			}

			LOGGER.finest(Thread.currentThread().getName() + " begins to handle the " + con);
			try{

				// ストリームの取得
				final InputStream input = new BufferedInputStream(con.getInputStream());
				final OutputStream output = new BufferedOutputStream(con.getOutputStream());

				// 切断要求が来るまで持続接続する
				boolean keepAlive = true;
				String prefix = "";
				//					do{

				// TODO: パイプライン化に対応。作れるだけリクエストを作成してキューに入れる．

				// リクエストオブジェクト
				final HttpRequest request = HttpRequest.create(input, prefix);
				if(request == null){

					LOGGER.finest(Thread.currentThread().getName() + " receives a null request");
					keepAlive = false;
					//							break;

				}else{

					LOGGER.finest(Thread.currentThread().getName() + " receives the " + request);

					// リクエストに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(request);

					//						レスポンスを受け取って返すところは別のスレッドにできる．

					// リクエストの実行
					final HttpResponse response = handler.doRequest(request);

















					// レスポンスに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(response);

					// レスポンスの書き出し
					// TODO: レスポンスの転送側もnioを使って非同期（マネージスレッドを一つ）に行う必要がある：そうでなければでかいファイルの読み込み時にブロックしてしまう
					response.writeOut(output);
					output.flush();

					//					}while(input.available() > 0);
				}

				if(keepAlive){

					//						LOGGER.finest(Thread.currentThread().getName() + " requeues the " + con + " because it has no data to read");
					LOGGER.finest(Thread.currentThread().getName() + " requeues the " + con);
					this.manager.add(con);

				}else{

					LOGGER.finest(Thread.currentThread().getName() + " finishes to handle and closes the " + con);
					input.close();
					output.close();

					try {
						con.close();
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}

				}

			}catch(final ClosedChannelException e){

				// TODO: この例外はここよりも上位でハンドリングすべき

				LOGGER.warning(e.getLocalizedMessage());
				e.printStackTrace();

			}catch(final IOException e){

				LOGGER.warning(e.getLocalizedMessage());
				// e.printStackTrace();

			}

		}

		this.manager.endRunning(this);


		//LOGGER.exiting(ServiceWorker.class.getName(), "run");
	}

	private boolean isKeepingAlive(final HttpRequest request){

		return !request.getHeader().containsValue(Connection, "close");

	}

	private boolean isKeepingAlive(final HttpResponse response){

		return !response.getHeader().containsValue(Connection, "close");

	}

	@Override
	public void close() throws IOException {

		this.running = false;

	}

}
