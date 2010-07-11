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
import java.util.logging.Level;

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

				LOGGER.finer("run", "Begin to handle the connection; {0}", con);

				try{

					// 切断要求が来るまで持続接続する
					boolean keepAlive = true;
					while(keepAlive && this.running){

						String prefix = "";

						// ストリームの取得
						final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
						final OutputStream output = new BufferedOutputStream(new NoExceptionOutputStreamFilter(new NoCloseOutputStream(con.getOutputStream())));

						// リクエストオブジェクト
						final HttpRequest request = HttpRequest.create(input, prefix);
						if(request == null){

							LOGGER.fine("run", "Receive a null request");
							keepAlive = false;

						}else{

							LOGGER.fine("run", "Receive a {0}", request);

							// リクエストに切断要求が含まれているか
							keepAlive &= this.isKeepingAlive(request);

							// リクエストの実行
							final HttpResponse response = handler.doRequest(request);

							// レスポンスに切断要求が含まれているか
							keepAlive &= this.isKeepingAlive(response);


							final HttpHeader header = response.getHeader();
							if(header.containsKey(HeaderName.ContentLength)){

								LOGGER.info("run", "{0} > {1} ({2} bytes)", request.getHeadLine(), response.getHeadLine(), header.get(HeaderName.ContentLength));

							}else{

								LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());

							}

							// レスポンスの書き出し
							LOGGER.fine("run", "Return the {0}", response);
							response.output(output);
							output.flush();

						}

					}

				}catch(final IOException e){

					LOGGER.warning("run", e.toString());
					LOGGER.catched(Level.FINE, "run", e);

				}

				LOGGER.finer("run", "Finish to handle and closes the connection; {0}", con);
				try {

					con.close();

				} catch (final IOException e) {

					LOGGER.warning("run", e.getMessage());
					LOGGER.catched(Level.FINE, "run", e);

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
