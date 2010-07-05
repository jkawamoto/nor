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
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.util.io.NoCloseInputStream;
import nor.util.io.NoCloseOutputStream;
import nor.util.log.Logger;

class ServiceWorker implements Runnable, Closeable{

	private boolean running = true;

	private final Queue<Connection> queue;
	private final HttpRequestHandler handler;

	private final List<EndEventListener> listeners = new ArrayList<EndEventListener>();

	private static final Logger LOGGER = Logger.getLogger(ServiceWorker.class);

	//============================================================================
	// Constractor
	//============================================================================
	public ServiceWorker(final Queue<Connection> queue, final HttpRequestHandler handler){

		this.queue = queue;
		this.handler = handler;

	}

	public void addListener(final EndEventListener listener){

		this.listeners.add(listener);

	}

	public void removeListener(final EndEventListener listener){

		this.listeners.remove(listener);

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

				LOGGER.finest("%s begins to handle the connection; %s", Thread.currentThread().getName(), con);
				// ストリームの取得
				final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
				final OutputStream output = new BufferedOutputStream(new NoExceptionOutputStreamFilter(new NoCloseOutputStream(con.getOutputStream())));


				// 切断要求が来るまで持続接続する
				boolean keepAlive = true;
				while(keepAlive && this.running){

					String prefix = "";

					// リクエストオブジェクト
					final HttpRequest request = HttpRequest.create(input, prefix);
					if(request == null){

						LOGGER.finest("%s receives a null request", Thread.currentThread().getName());
						keepAlive = false;

					}else{

						LOGGER.finest("%s receives the %s", Thread.currentThread().getName(), request);

						// リクエストに切断要求が含まれているか
						keepAlive &= this.isKeepingAlive(request);

						// リクエストの実行
						final HttpResponse response = handler.doRequest(request);

						// レスポンスに切断要求が含まれているか
						keepAlive &= this.isKeepingAlive(response);

						try{

							final HttpHeader header = response.getHeader();
							if(header.containsKey(HeaderName.ContentLength)){

								LOGGER.info("%s > %s (%s bytes)", request.getHeadLine(), response.getHeadLine(), header.get(HeaderName.ContentLength));

							}else{

								LOGGER.info("%s > %s (unknown length)", request.getHeadLine(), response.getHeadLine());

							}

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

				}

				LOGGER.finest("%s finishes to handle and closes the connection; %s", Thread.currentThread().getName(), con);
				try {

					con.close();

				} catch (final IOException e) {

					LOGGER.warning(e.getMessage());

				}

			}else{

				this.running = false;

			}


		}

		// Notify the end event to all listeners
		for(final EndEventListener listener : this.listeners){

			listener.update(this);

		}

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
	// Public interface
	//============================================================================
	public interface EndEventListener{

		public void update(final ServiceWorker from);

	}

}
