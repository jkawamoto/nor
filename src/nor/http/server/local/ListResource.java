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
package nor.http.server.local;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.error.HttpException;

public class ListResource extends Resource implements List<Resource>{

	private final List<Resource> children = Collections.synchronizedList(new ArrayList<Resource>());

	//============================================================================
	//  Constructor
	//============================================================================
	protected ListResource(final String name) {
		super(name);
	}

	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public HttpResponse doDelete(final String path, final HttpRequest request) 	throws HttpException {

		HttpResponse ret = null;
		final String next = path.substring(this.getName().length() + 1);
		for(final Resource r : this.children){

			ret = r.doDelete(next, request);

		}

		return ret;

	}

	@Override
	public HttpResponse doGet(String path, HttpRequest request) 	throws HttpException {

		HttpResponse ret = null;
		final String next = path.substring(this.getName().length() + 1);
		for(final Resource r : this.children){

			ret = r.doGet(next, request);

		}

		return ret;

	}

	@Override
	public HttpResponse doPost(String path, HttpRequest request) throws HttpException {

		HttpResponse ret = null;
		final String next = path.substring(this.getName().length() + 1);
		for(final Resource r : this.children){

			ret = r.doPost(next, request);

		}

		return ret;

	}

	@Override
	public HttpResponse doPut(String path, HttpRequest request) throws HttpException {

		HttpResponse ret = null;
		final String next = path.substring(this.getName().length() + 1);
		for(final Resource r : this.children){

			ret = r.doPut(next, request);

		}

		return ret;

	}

	//---------------------------------------------------------------
	//  Delegated methods
	//---------------------------------------------------------------
	public void add(int index, Resource element) {
		children.add(index, element);
	}

	public boolean add(Resource e) {
		return children.add(e);
	}

	public boolean addAll(Collection<? extends Resource> c) {
		return children.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Resource> c) {
		return children.addAll(index, c);
	}

	public void clear() {
		children.clear();
	}

	public boolean contains(Object o) {
		return children.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return children.containsAll(c);
	}

	public boolean equals(Object o) {
		return children.equals(o);
	}

	public Resource get(int index) {
		return children.get(index);
	}

	public int hashCode() {
		return children.hashCode();
	}

	public int indexOf(Object o) {
		return children.indexOf(o);
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	public Iterator<Resource> iterator() {
		return children.iterator();
	}

	public int lastIndexOf(Object o) {
		return children.lastIndexOf(o);
	}

	public ListIterator<Resource> listIterator() {
		return children.listIterator();
	}

	public ListIterator<Resource> listIterator(int index) {
		return children.listIterator(index);
	}

	public Resource remove(int index) {
		return children.remove(index);
	}

	public boolean remove(Object o) {
		return children.remove(o);
	}

	public boolean removeAll(Collection<?> c) {
		return children.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return children.retainAll(c);
	}

	public Resource set(int index, Resource element) {
		return children.set(index, element);
	}

	public int size() {
		return children.size();
	}

	public List<Resource> subList(int fromIndex, int toIndex) {
		return children.subList(fromIndex, toIndex);
	}

	public Object[] toArray() {
		return children.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return children.toArray(a);
	}

}
