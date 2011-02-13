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
import java.util.logging.Level;

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
	public ConnectionManager(final HttpRequestHandler handler, final int maxThreads){
		LOGGER.entering("<init>", handler, maxThreads);
		assert handler != null;

		if(maxThreads == 0){

			this.pool = Executors.newCachedThreadPool();

		}else{

			this.pool = Executors.newFixedThreadPool(maxThreads);

		}
		this.handler = handler;

		LOGGER.exiting("<init>");
	}

	//============================================================================
	// Public methods
	//============================================================================
	public boolean offer(final Connection e) {
		LOGGER.entering("offer", e);
		assert e != null;

		boolean res;
		if(e.closed()){

			res = false;

		}else{

			final RequestHandleWorker worker = new RequestHandleWorker(e, this.handler);
			this.pool.execute(worker);

			LOGGER.finer("offer", "Offer a new connection and send notify message.");
			res = true;

		}

		LOGGER.exiting("offer", res);
		return res;
	}

	/* (非 Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close(){
		LOGGER.entering("close");

		this.pool.shutdownNow();
		try {

			if (!this.pool.awaitTermination(120, TimeUnit.SECONDS)){

				LOGGER.warning("close", "Thread pool did not terminate.");

			}

		} catch (final InterruptedException e) {

			LOGGER.catched(Level.WARNING, "close", e);
			this.pool.shutdownNow();
			Thread.currentThread().interrupt();

		}

		LOGGER.exiting("close");
	}

}