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
package nor.core.proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.Matchable;
import nor.util.Querable;


class MatchableSet<Type extends Matchable> implements Set<Type>, Querable<List<Type>>{

	private final List<Type> filters = new ArrayList<Type>();

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.Querable#query(java.lang.String)
	 */
	@Override
	public List<Type> query(final String cond){

		final List<Type> ret = new ArrayList<Type>();
		for(final Type f : this.filters){

			final Pattern pat = f.pattern();
			final Matcher m = pat.matcher(cond);
			if(m.matches()){

				ret.add(f);

			}

		}

		return ret;


	}

	//--------------------------------------------------------------------
	//  委譲メソッド
	//--------------------------------------------------------------------
	/* (非 Javadoc)
	 * @see java.util.Set#add(E)
	 */
	@Override
	public boolean add(final Type value){

		return this.filters.add(value);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#addAll(java.util.Collection<? extends E>)
	 */
	@Override
	public boolean addAll(final Collection<? extends Type> c) {

		return this.filters.addAll(c);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#clear()
	 */
	@Override
	public void clear() {

		this.filters.clear();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(final Object o) {

		return this.filters.contains(o);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#containsAll(java.util.Collection<?>)
	 */
	@Override
	public boolean containsAll(final Collection<?> c) {

		return this.filters.containsAll(c);

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object o) {

		return filters.equals(o);

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		return filters.hashCode();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#isEmpty()
	 */
	@Override
	public boolean isEmpty() {

		return filters.isEmpty();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#iterator()
	 */
	@Override
	public Iterator<Type> iterator() {

		return filters.iterator();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(final Object o) {

		return filters.remove(o);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#removeAll(java.util.Collection<?>)
	 */
	@Override
	public boolean removeAll(final Collection<?> c) {

		return filters.removeAll(c);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#retainAll(java.util.Collection<?>)
	 */
	@Override
	public boolean retainAll(final Collection<?> c) {

		return filters.retainAll(c);

	}

	/* (非 Javadoc)
	 * @see java.util.Set#size()
	 */
	@Override
	public int size() {

		return filters.size();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#toArray()
	 */
	@Override
	public Object[] toArray() {

		return filters.toArray();

	}

	/* (非 Javadoc)
	 * @see java.util.Set#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(final T[] a) {

		return filters.toArray(a);

	}

}
