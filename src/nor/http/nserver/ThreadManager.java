/**
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
package nor.http.nserver;


import java.io.Closeable;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nor.http.server.HttpRequestHandler;

/**
 * @author tcowan
 */
class ThreadManager implements Closeable{

	private int threads = 0;

	private final int minThreads;
	private final int queueSize;
	private final int waitTime;

	private final Queue<Connection> queue = new LinkedList<Connection>();

	private final ExecutorService pool;

	private final HttpRequestHandler handler;

	public ThreadManager(final HttpRequestHandler handler, final int minThreads, final int queueSize, final int waitTime){

		this.pool = Executors.newCachedThreadPool();
		this.handler = handler;

		this.minThreads = minThreads;
		this.queueSize = queueSize;
		this.waitTime = waitTime;

	}

	public synchronized boolean offer(final Connection e) {

		final boolean ret = queue.offer(e);
		if(this.threads < this.minThreads || this.size() > this.queueSize){

			this.pool.execute(new ServiceWorker(this, this.handler));
			++this.threads;

		}

		notify();
		return ret;

	}

	public synchronized Connection poll() {

		try{

			if(this.isEmpty()){

				wait(this.waitTime);

			}
			return queue.poll();

		}catch(final InterruptedException e){

			return null;

		}

	}

	@Override
	public void close(){

		this.pool.shutdown();

	}

	public synchronized void endRunning(final ServiceWorker w){

		--this.threads;

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

}
