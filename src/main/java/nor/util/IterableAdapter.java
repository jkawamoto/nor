/*
 *  Copyright (C) 2010 KAWAMOTO Junpei
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
package nor.util;

import java.util.Enumeration;
import java.util.Iterator;

public class IterableAdapter<T> implements Iterable<T>{

	private final Enumeration<T> e;

	public IterableAdapter(final Enumeration<T> e){

		this.e = e;

	}

	@Override
	public Iterator<T> iterator() {

		return new Iterator<T>(){

			@Override
			public boolean hasNext() {

				return IterableAdapter.this.e.hasMoreElements();

			}

			@Override
			public T next() {

				return IterableAdapter.this.e.nextElement();

			}

			@Override
			public void remove() {

				throw new UnsupportedOperationException();

			}

		};

	}

}
