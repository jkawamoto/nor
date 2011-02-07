/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.SelectableChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.server.HttpRequestHandler;
import nor.network.Connection;
import nor.util.io.NoCloseInputStream;
import nor.util.io.NoCloseOutputStream;
import nor.util.io.NoExceptionOutputStreamFilter;
import nor.util.log.Logger;

/**
 *
 * @author Junpei Kawamoto
 * @since 0.2
 */
class RequestHandleWorker implements Runnable{

	private final Connection con;
	private final HttpRequestHandler handler;

	private final List<ServiceEventListener> listeners = new ArrayList<ServiceEventListener>();

	private static final Logger LOGGER = Logger.getLogger(RequestHandleWorker.class);

	//============================================================================
	// Constractor
	//============================================================================
	public RequestHandleWorker(final Connection con, final HttpRequestHandler handler){

		this.con = con;
		this.handler = handler;

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


		LOGGER.finer("run", "Begin to handle the connection; {0}", con);
		try{

			// 切断要求が来るまで持続接続する
			boolean keepAlive = true;
			while(keepAlive && !Thread.currentThread().isInterrupted()){

				// ストリームの取得
				final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
				final NoExceptionOutputStreamFilter output = new NoExceptionOutputStreamFilter(new BufferedOutputStream(new NoCloseOutputStream(con.getOutputStream())));

				// リクエストオブジェクト
				final HttpRequest request = HttpRequest.create(input);
				if(request == null){

					LOGGER.fine("run", "Receive a null request");
					keepAlive = false;

					break;

				}

				try{

					if(request.getMethod() == Method.CONNECT){

						LOGGER.fine("run", "Receive a connect request: {0}", request);

						final SelectableChannel ch = this.handler.doConnectRequest(request);

						/*
						 * Notify the client of a connection established.
						 */
						final HttpResponse response = request.createResponse(Status.ConnectionEstablished);
						response.writeTo(output);
						output.flush();

						con.requestDelegation(ch);

						LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());

						break;

					}else{

						LOGGER.fine("run", "Receive a {0}", request);

						// リクエストの実行
						final HttpResponse response = handler.doRequest(request);

						final HttpHeader header = response.getHeader();

						// レスポンスの書き出し
						LOGGER.fine("run", "Return the {0}", response);
						response.writeTo(output);
						response.close();
						output.flush();

						if(header.containsKey(HeaderName.ContentLength)){

							LOGGER.info("run", "{0} > {1} ({2} bytes)", request.getHeadLine(), response.getHeadLine(), header.get(HeaderName.ContentLength));

						}else{

							LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());

						}

						keepAlive = output.alive() && this.isKeepingAlive(request) && this.isKeepingAlive(response);


					}

				}catch(final HttpException e){

					final HttpResponse response = e.createResponse(request);
					response.writeTo(output);
					output.flush();

					keepAlive = false;

				}

			}

		}catch(final IOException e){

			LOGGER.warning("run", e.toString());
			LOGGER.catched(Level.FINE, "run", e);

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

		}finally{

			// Notify the end event to all listeners
			for(final ServiceEventListener listener : this.listeners){

				listener.onEnd(this);

			}

		}

		LOGGER.exiting("run");
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
