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

import static nor.http.HeaderName.Connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Queue;

import nor.http.HeaderName;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.util.io.NoCloseInputStream;
import nor.util.io.NoCloseOutputStream;
import nor.util.log.EasyLogger;

class ServiceWorker implements Runnable, Closeable{

	private final Queue<Connection> queue;
	private final HttpRequestHandler handler;

	private boolean running = true;

	private static int nThreads = 0;
	private static final EasyLogger LOGGER = EasyLogger.getLogger(ServiceWorker.class);

	//============================================================================
	// Constractor
	//============================================================================
	private ServiceWorker(final Queue<Connection> queue, final HttpRequestHandler handler){

		this.queue = queue;
		this.handler = handler;

	}

	//============================================================================
	// Public methods
	//============================================================================
	/**
	 * 要求に答える．
	 */
	@Override
	public synchronized void run() {
		LOGGER.entering("run");

		while(this.running){

			final Connection con = this.queue.poll();
			if(con != null){

				LOGGER.finest(Thread.currentThread().getName() + " begins to handle the connection; " + con);
				// ストリームの取得
				final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
				final OutputStream output = new BufferedOutputStream(new NoExceptionOutputStreamFilter(new NoCloseOutputStream(con.getOutputStream())));


				// 切断要求が来るまで持続接続する
				boolean keepAlive = true;
				String prefix = "";

				// リクエストオブジェクト
				final HttpRequest request = HttpRequest.create(input, prefix);
				if(request == null){

					LOGGER.finest(Thread.currentThread().getName() + " receives a null request");
					keepAlive = false;

				}else{

					LOGGER.finest(Thread.currentThread().getName() + " receives the " + request);

					// リクエストに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(request);

					// リクエストの実行
					final HttpResponse response = handler.doRequest(request);

					// レスポンスに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(response);

					try{

						LOGGER.info(request.getHeadLine() + " > " + response.getHeadLine() + " (" + response.getHeader().get(HeaderName.ContentLength) + " bytes)");

						// レスポンスの書き出し
						response.output(output);
						output.flush();

					}catch(final IOException e){

						e.printStackTrace();
						System.out.println(response.getHeadLine());
						System.out.println(response.getHeader().toString());

						keepAlive = false;

					}

				}

				if(keepAlive){

					LOGGER.finest(Thread.currentThread().getName() + " requeues the " + con);
					this.queue.add(con);

				}else{

					LOGGER.finest(Thread.currentThread().getName() + " finishes to handle and closes the " + con);
					try {

						con.close();

					} catch (final IOException e) {

						LOGGER.warning(e.getMessage());


					}

				}

			}else{

				this.running = false;

			}


		}
		ServiceWorker.decrease();
		LOGGER.exiting("run");
	}

	@Override
	public void close(){

		this.running = false;

	}


	//============================================================================
	// Private methods
	//============================================================================
	private boolean isKeepingAlive(final HttpMessage msg){

		return !msg.getHeader().containsValue(Connection, "close");

	}


	//============================================================================
	// Public static methods
	//============================================================================
	public static synchronized ServiceWorker create(final Queue<Connection> queue, final HttpRequestHandler handler){

		++ServiceWorker.nThreads;
		return new ServiceWorker(queue, handler);

	}

	public static synchronized int nThreads(){

		return ServiceWorker.nThreads;

	}

	private static synchronized void decrease(){

		--ServiceWorker.nThreads;

	}


}
