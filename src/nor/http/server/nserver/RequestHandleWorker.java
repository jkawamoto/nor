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
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;
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

	private final List<ServiceEventListener> listeners = new ArrayList<ServiceEventListener>();

	private static final Logger LOGGER = Logger.getLogger(RequestHandleWorker.class);

	//============================================================================
	// Constractor
	//============================================================================
	public RequestHandleWorker(final Queue<Connection> queue, final HttpRequestHandler handler){

		this.queue = queue;
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

		while(this.running){

			final Connection con = this.queue.poll();
			if(con != null){

				LOGGER.finer("run", "Begin to handle the connection; {0}", con);

				try{

					// 切断要求が来るまで持続接続する
					while(this.running){

						String prefix = "";

						// ストリームの取得
						final InputStream input = new BufferedInputStream(new NoCloseInputStream(con.getInputStream()));
						final NoExceptionOutputStreamFilter output = new NoExceptionOutputStreamFilter(new BufferedOutputStream(new NoCloseOutputStream(con.getOutputStream())));

						// リクエストオブジェクト
						final HttpRequest request = HttpRequest.create(input, prefix);
						if(request == null){

							LOGGER.fine("run", "Receive a null request");
							break;

						}else if(request.getMethod() == Method.CONNECT){

							LOGGER.fine("run", "Receive a connect request: {0}", request);

							final Pattern pat = Pattern.compile("(.+):(\\d+)");
							final Matcher m = pat.matcher(request.getPath());

							if(m.find()){

								final String host = m.group(1);
								final int port = Integer.valueOf(m.group(2));
								final InetSocketAddress addr = new InetSocketAddress(host, port);
								final SocketChannel ch = SocketChannel.open(addr);

								/*
								 * Notify the client of a connection established.
								 */
								final HttpResponse response = request.createResponse(Status.OK);
								LOGGER.info("run", "{0} > {1} (unknown length)", request.getHeadLine(), response.getHeadLine());
								response.output(output);
								output.flush();

								con.requestDelegation(ch);
								break;

							}

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
							response.output(output);
							output.flush();

							if(!output.alive()){

								break;

							}else if(!this.isKeepingAlive(request) || !this.isKeepingAlive(response)){

								break;

							}

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
