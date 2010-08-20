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
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.server.HttpConnectRequestHandler;
import nor.http.server.HttpRequestHandler;
import nor.util.io.NoCloseInputStream;
import nor.util.io.NoCloseOutputStream;
import nor.util.io.NoExceptionOutputStreamFilter;
import nor.util.log.Logger;

/**
 *
 * @author Junpei Kawamoto
 * @since 0.2
 */
class RequestHandleWorker implements Runnable, Closeable{

	private boolean running = true;

	private final Queue<Connection> queue;
	private final HttpRequestHandler handler;
	private final HttpConnectRequestHandler connecter;

	private final List<ServiceEventListener> listeners = new ArrayList<ServiceEventListener>();

	private static final Logger LOGGER = Logger.getLogger(RequestHandleWorker.class);

	//============================================================================
	// Constractor
	//============================================================================
	public RequestHandleWorker(final Queue<Connection> queue, final HttpRequestHandler handler, final HttpConnectRequestHandler connecter){

		this.queue = queue;
		this.handler = handler;
		this.connecter = connecter;

	}

	public void addListener(final ServiceEventListener listener){

		this.listeners.add(listener);

	}

	public void removeListener(final ServiceEventListener listener){

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
					while(this.running){

						// ストリームの取得
						final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
						final NoExceptionOutputStreamFilter output = new NoExceptionOutputStreamFilter(new BufferedOutputStream(new NoCloseOutputStream(con.getOutputStream())));

						// リクエストオブジェクト
						final HttpRequest request = HttpRequest.create(input);
						if(request == null){

							LOGGER.fine("run", "Receive a null request");
							break;

						}else if(request.getMethod() == Method.CONNECT){

							LOGGER.fine("run", "Receive a connect request: {0}", request);

							try{

								final SelectableChannel ch = this.connecter.doRequest(request);

								/*
								 * Notify the client of a connection established.
								 */
								final HttpResponse response = request.createResponse(Status.ConnectionEstablished);
								response.writeTo(output);
								output.flush();

								con.requestDelegation(ch);

								LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());

							}catch(final HttpException e){

								final HttpResponse response = e.createResponse(request);
								response.writeTo(output);
								output.flush();

							}

							break;

						}else{

							LOGGER.fine("run", "Receive a {0}", request);

							// リクエストの実行
							final HttpResponse response = handler.doRequest(request);

							final HttpHeader header = response.getHeader();
							if(header.containsKey(HeaderName.ContentLength)){

								LOGGER.info("run", "{0} > {1} ({2} bytes)", request.getHeadLine(), response.getHeadLine(), header.get(HeaderName.ContentLength));

							}else{

								LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());

							}

							// レスポンスの書き出し
							LOGGER.fine("run", "Return the {0}", response);
							response.writeTo(output);
							response.close();
							output.flush();

							if(!output.alive()){

								break;

							}else if(!this.isKeepingAlive(request) || !this.isKeepingAlive(response)){

								break;

							}

						}

					}

				}catch(final IOException e){

					if(this.running){

						LOGGER.warning("run", e.toString());
						LOGGER.catched(Level.FINE, "run", e);

					}

				}catch(final VirtualMachineError e){

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
		for(final ServiceEventListener listener : this.listeners){

			listener.onEnd(this);

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
	public interface ServiceEventListener{

		public void onEnd(final RequestHandleWorker from);

	}

}
