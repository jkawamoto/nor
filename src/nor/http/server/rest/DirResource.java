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
package nor.http.server.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import nor.http.ErrorResponseBuilder;
import nor.http.Status;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

/**
 * リソースディレクトリを表すクラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class DirResource extends Resource implements Collection<Resource>{

	/**
	 * リソースの名前
	 */
	private final String name;

	/**
	 * 子リソース
	 */
	private final List<Resource> children = Collections.synchronizedList(new ArrayList<Resource>());

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(DirResource.class.getName());

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * ディレクトリリソースを作成する．
	 *
	 * @param name ディレクトリの名前
	 */
	public DirResource(final String name){
		LOGGER.entering(DirResource.class.getName(), "<init>", name);
		assert name != null;

		this.name = name;

		LOGGER.exiting(DirResource.class.getName(), "<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#getName()
	 */
	@Override
	public String getName() {
		LOGGER.entering(DirResource.class.getName(), "getName");

		final String ret = this.name;

		LOGGER.exiting(DirResource.class.getName(), "getName", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#toDelete(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toDelete(String path, HttpRequest request) {
		LOGGER.entering(DirResource.class.getName(), "toDelete");

		HttpResponse ret = null;
		final String name = path.substring(this.name.length() + 1);

		synchronized(this.children){

			for(final Resource r : this.children){

				if(name.startsWith(r.getName())){

					ret = r.toDelete(name, request);
					if(ret != null){

						break;

					}

				}

			}

		}

		if(ret == null){

			ret = ErrorResponseBuilder.create(request, Status.NotFound);

		}

		LOGGER.exiting(DirResource.class.getName(), "toDelete", ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#toGet(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toGet(String path, HttpRequest request) {
		LOGGER.entering(DirResource.class.getName(), "toGet");

		HttpResponse ret = null;
		final String name = path.substring(this.name.length() + 1);

		synchronized(this.children){

			for(final Resource r : this.children){

				if(name.startsWith(r.getName())){

					ret = r.toGet(name, request);
					if(ret != null){

						break;

					}

				}

			}

		}

		if(ret == null){

			ret = ErrorResponseBuilder.create(request, Status.NotFound);

		}

		LOGGER.exiting(DirResource.class.getName(), "toGet", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#toPost(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toPost(String path, HttpRequest request) {
		LOGGER.entering(DirResource.class.getName(), "toPost");

		HttpResponse ret = null;
		final String name = path.substring(this.name.length() + 1);

		synchronized(this.children){

			for(final Resource r : this.children){

				if(name.startsWith(r.getName())){

					ret = r.toPost(name, request);
					if(ret != null){

						break;

					}

				}

			}

		}

		if(ret == null){

			ret = ErrorResponseBuilder.create(request, Status.NotFound);

		}

		LOGGER.exiting(DirResource.class.getName(), "toPost", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#toPut(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toPut(String path, HttpRequest request) {
		LOGGER.entering(DirResource.class.getName(), "toPut");

		HttpResponse ret = null;
		final String name = path.substring(this.name.length() + 1);

		synchronized(this.children){

			for(final Resource r : this.children){

				if(name.startsWith(r.getName())){

					ret = r.toPut(name, request);
					if(ret != null){

						break;

					}

				}

			}

		}

		if(ret == null){

			ret = ErrorResponseBuilder.create(request, Status.NotFound);

		}

		LOGGER.exiting(DirResource.class.getName(), "toPut", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){

		return "DirResource : " + this.name;

	}


	//--------------------------------------------------------------------
	//  Collection の実装
	//--------------------------------------------------------------------
	/* (non-Javadoc)
	 * @see java.util.Collection#add(java.lang.Object)
	 */
	@Override
	public boolean add(final Resource r){

		return this.children.add(r);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {

		return children.remove(o);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends Resource> c) {

		return children.addAll(c);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {

		children.clear();

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {

		return children.contains(o);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {

		return children.containsAll(c);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {

		return children.isEmpty();

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#iterator()
	 */
	@Override
	public Iterator<Resource> iterator() {

		return children.iterator();

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {

		return children.removeAll(c);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {

		return children.retainAll(c);

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#size()
	 */
	@Override
	public int size() {
		return children.size();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {

		return children.toArray();

	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {

		return children.toArray(a);

	}

}


