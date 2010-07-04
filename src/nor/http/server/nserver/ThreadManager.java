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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nor.http.server.HttpRequestHandler;
import nor.util.log.EasyLogger;

class ThreadManager implements Closeable, Queue<Connection>{

	private int waiting = 0;
	private boolean running = true;

	private final int minThreads;
	private final int timeout;

	private final Queue<Connection> queue = new LinkedList<Connection>();
	private final ExecutorService pool;
	private final HttpRequestHandler handler;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(ThreadManager.class);

	public ThreadManager(final HttpRequestHandler handler, final int minThreads, final int timeout){

		this.pool = Executors.newCachedThreadPool();
		this.handler = handler;

		this.minThreads = minThreads;
		this.timeout = timeout;

	}

	public synchronized boolean offer(final Connection e) {

		final boolean ret = queue.offer(e);
		if(ret){

			if(ServiceWorker.nThreads() < this.minThreads || this.waiting == 0){

				LOGGER.fine("Create a new worker thread.");
				this.pool.execute(ServiceWorker.create(this, this.handler));

			}

			LOGGER.fine("Offer a new connection and send notify message.");
			this.notify();

		}
		return ret;

	}

	public synchronized Connection poll() {

		Connection ret = null;
		if(this.running){

			try{

				LOGGER.finer("Waiting = " + this.waiting + ", working = " + ServiceWorker.nThreads() + ", connection = " + this.size());
				while(this.isEmpty() && this.waiting <= this.minThreads){

					LOGGER.finest("Going to wait.");

					++this.waiting;
					this.wait(this.timeout);
					--this.waiting;

					LOGGER.finest("Wake up.");

				}
				LOGGER.finer("Waiting = " + this.waiting + ", working = " + ServiceWorker.nThreads() + ", connection = " + this.size());

				ret = queue.poll();

			}catch(final InterruptedException e){

				LOGGER.warning(e.getMessage());

			}

		}

		return ret;

	}

	@Override
	public void close(){

		this.running = false;
		this.pool.shutdown();

	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean add(final Connection e){

		return this.offer(e);

	}

	public Connection remove(){

		final Connection ret = this.poll();
		if(ret == null){

			throw new NoSuchElementException();

		}

		return ret;

	}

	public boolean isEmpty(){

		return this.queue.isEmpty();

	}

	public int size() {

		return this.queue.size();

	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////

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

	/////////////////////////////////////////////////////////////////////////////////////////////////////////

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
