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

import java.io.Closeable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nor.http.server.HttpRequestHandler;
import nor.http.server.nserver.RequestHandleWorker.ServiceEventListener;
import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class ConnectionManager implements Closeable, Queue<Connection>, ServiceEventListener{

	private int waiting = 0;
	private boolean running = true;

	private final int minThreads;
	private final int timeout;

	private final HttpRequestHandler handler;

	private final Queue<Connection> queue = new LinkedList<Connection>();

	private final ExecutorService pool;
	private final Set<RequestHandleWorker> workers = new HashSet<RequestHandleWorker>();

	private static final Logger LOGGER = Logger.getLogger(ConnectionManager.class);

	//============================================================================
	// Constructor
	//============================================================================
	public ConnectionManager(final HttpRequestHandler handler, final int minThreads, final int timeout){

		this.pool = Executors.newCachedThreadPool();
		this.handler = handler;

		this.minThreads = minThreads;
		this.timeout = timeout;

	}

	//============================================================================
	// Public methods
	//============================================================================
	@Override
	public synchronized boolean offer(final Connection e) {
		assert e != null;

		if(e.closed()){

			return false;

		}else{

			final boolean ret = queue.offer(e);
			if(ret){

				if(this.waiting == 0){

					final RequestHandleWorker w = new RequestHandleWorker(this, this.handler);
					this.workers.add(w);
					this.pool.execute(w);

					LOGGER.fine("offer", "Create a new worker thread (current: {0} threads).", this.workers.size());

				}

				LOGGER.finer("offer", "Offer a new connection and send notify message.");
				this.notify();

			}

			return ret;

		}

	}

	@Override
	public synchronized Connection poll() {

		Connection ret = null;
		if(this.running){

			try{

				do{

					LOGGER.finer("poll", "Waiting = {0}, working = {1}, connection = {2}", this.waiting, this.workers.size(), this.size());
					while(this.isEmpty() && this.waiting <= this.minThreads){

						LOGGER.finest("poll", "Be going to wait.");

						++this.waiting;
						this.wait(this.timeout);
						--this.waiting;

						LOGGER.finest("poll", "Wake up.");

					}
					LOGGER.finer("poll", "Waiting = {0}, working = {1}, connection = {2}", this.waiting, this.workers.size(), this.size());

					ret = queue.poll();

				}while(ret != null && ret.closed());

			}catch(final InterruptedException e){

				/*
				 * This thread is interrupted. It means this application is going to exit.
				 * Therefore, this method returns null to end this worker thread.
				 */
				ret = null;

			}

		}

		return ret;

	}

	@Override
	public void close(){

		this.running = false;
		this.pool.shutdownNow();

		try {

			if (!this.pool.awaitTermination(60, TimeUnit.SECONDS)){

				LOGGER.warning("close", "Thread pool did not terminate.");

			}

		} catch (final InterruptedException ie) {

			this.pool.shutdownNow();
			Thread.currentThread().interrupt();

		}


	}

	/**
	 * Receive a service-end event from service workers.
	 *
	 * @param from the service worker sending this event.
	 */
	@Override
	public void onEnd(final RequestHandleWorker from) {

		this.workers.remove(from);
		LOGGER.fine("update", "Remove a worker thread (current: {0} threads).", this.workers.size());

	}



	//----------------------------------------------------------------------------
	// Delegation methods
	//----------------------------------------------------------------------------
	@Override
	public boolean add(final Connection e){

		return this.offer(e);

	}

	@Override
	public Connection remove(){

		final Connection ret = this.poll();
		if(ret == null){

			throw new NoSuchElementException();

		}

		return ret;

	}

	@Override
	public boolean isEmpty(){

		return this.queue.isEmpty();

	}

	@Override
	public int size() {

		return this.queue.size();

	}

	@Override
	public Connection element() {

		return this.queue.element();

	}

	@Override
	public Connection peek() {

		return this.queue.peek();

	}

	@Override
	public boolean addAll(Collection<? extends Connection> c) {

		for(final Connection e : c){

			this.add(e);

		}
		return true;

	}

	@Override
	public void clear() {

		this.queue.clear();

	}

	@Override
	public boolean contains(Object o) {

		return this.queue.contains(o);

	}

	@Override
	public boolean containsAll(Collection<?> c) {

		return this.queue.containsAll(c);

	}

	@Override
	public Iterator<Connection> iterator() {

		return this.queue.iterator();

	}

	@Override
	public boolean remove(Object o) {

		return this.queue.remove(o);
	}

	@Override
	public boolean removeAll(Collection<?> c) {

		return this.removeAll(c);

	}

	@Override
	public synchronized boolean retainAll(Collection<?> c) {

		final boolean ret = this.queue.retainAll(c);
		this.notify();

		return ret;

	}

	@Override
	public Object[] toArray() {

		return this.queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {

		return this.queue.toArray(a);

	}

}