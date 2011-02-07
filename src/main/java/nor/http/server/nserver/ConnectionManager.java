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

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nor.http.server.HttpRequestHandler;
import nor.network.Connection;
import nor.util.log.Logger;

/**
 *
 * @author Junpei Kawamoto
 * @since 0.2
 */
class ConnectionManager implements Closeable{

	private final HttpRequestHandler handler;
	private final ExecutorService pool;

	private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class);

	//============================================================================
	// Constructor
	//============================================================================
	public ConnectionManager(final HttpRequestHandler handler, final int maxThreads, final int timeout){

		if(maxThreads == 0){

			this.pool = Executors.newCachedThreadPool();

		}else{

			this.pool = Executors.newFixedThreadPool(maxThreads);

		}
		this.handler = handler;

	}

	//============================================================================
	// Public methods
	//============================================================================
	public boolean offer(final Connection e) {
		assert e != null;

		if(e.closed()){

			return false;

		}else{

			final RequestHandleWorker worker = new RequestHandleWorker(e, this.handler);
			this.pool.execute(worker);

			LOGGER.finer("offer", "Offer a new connection and send notify message.");
			return true;

		}

	}

	@Override
	public void close(){

		this.pool.shutdownNow();
		try {

			if (!this.pool.awaitTermination(120, TimeUnit.SECONDS)){

				LOGGER.warning("close", "Thread pool did not terminate.");

			}

		} catch (final InterruptedException ie) {

			this.pool.shutdownNow();
			Thread.currentThread().interrupt();

		}

	}

}