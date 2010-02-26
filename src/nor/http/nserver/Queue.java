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


import java.util.LinkedList;

/**
 * @author tcowan
 */
class Queue<Type>{

	private int waitingThreads = 0;

	LinkedList<Type> impl = new LinkedList<Type>();

	public synchronized void insert(final Type obj){

		impl.addLast(obj);
		notify();

	}

	public synchronized Type remove() throws InterruptedException{

		if(this.isEmpty()){

			waitingThreads++;
			wait();
			waitingThreads--;

		}
		return impl.removeFirst();

	}

	public boolean isEmpty(){

		return 	(impl.size() - waitingThreads <= 0);

	}

}
