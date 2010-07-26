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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.error.HttpException;
import nor.util.log.Logger;

/**
 * リソースディレクトリを表すクラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class DirResource extends Resource implements Map<String, Resource>{

	/**
	 * 子リソース
	 */
	private final Map<String, Resource> children = Collections.synchronizedMap(new HashMap<String, Resource>());

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(DirResource.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * ディレクトリリソースを作成する．
	 *
	 * @param name ディレクトリの名前
	 */
	public DirResource(final String name){
		super(name);
		LOGGER.entering("<init>", name);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	@Override
	public HttpResponse doDelete(final String path, final HttpRequest request) throws HttpException{
		LOGGER.entering("doDelete", path, request);

		HttpResponse ret = null;
		if(path.startsWith(this.getName())){

			final String next = path.substring(this.getName().length() + 1);
			final String dirname = next.substring(1, next.indexOf("/"));

			if(this.children.containsKey(dirname)){

				final Resource r = this.children.get(dirname);
				ret = r.doDelete(next, request);

			}

		}

		LOGGER.exiting("doDelete", ret);
		return ret;
	}

	@Override
	public HttpResponse doGet(final String path, final HttpRequest request) throws HttpException{
		LOGGER.entering("doGet", path, request);

		HttpResponse ret = null;
		if(path.startsWith(this.getName())){

			final String next = path.substring(this.getName().length() + 1);
			final String dirname = next.substring(1, next.indexOf("/"));

			if(this.children.containsKey(dirname)){

				final Resource r = this.children.get(dirname);
				ret = r.doDelete(next, request);

			}

		}

		LOGGER.exiting("doGet", ret);
		return ret;

	}

	@Override
	public HttpResponse doPost(final String path, final HttpRequest request) throws HttpException{
		LOGGER.entering("doPost", path, request);

		HttpResponse ret = null;
		if(path.startsWith(this.getName())){

			final String next = path.substring(this.getName().length() + 1);
			final String dirname = next.substring(1, next.indexOf("/"));

			if(this.children.containsKey(dirname)){

				final Resource r = this.children.get(dirname);
				ret = r.doDelete(next, request);

			}

		}

		LOGGER.exiting("doPost", ret);
		return ret;

	}

	@Override
	public HttpResponse doPut(final String path, final HttpRequest request) throws HttpException{
		LOGGER.entering("doPut", path, request);

		HttpResponse ret = null;
		if(path.startsWith(this.getName())){

			final String next = path.substring(this.getName().length() + 1);
			final String dirname = next.substring(1, next.indexOf("/"));

			if(this.children.containsKey(dirname)){

				final Resource r = this.children.get(dirname);
				ret = r.doDelete(next, request);

			}

		}

		LOGGER.exiting("doPut", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){

		return "DirResource : " + this.getName();

	}

	//---------------------------------------------------------------
	//  public メソッド
	//---------------------------------------------------------------
	public void clear() {
		children.clear();
	}

	public boolean containsKey(Object key) {
		return children.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return children.containsValue(value);
	}

	public Set<java.util.Map.Entry<String, Resource>> entrySet() {
		return children.entrySet();
	}

	public boolean equals(Object o) {
		return children.equals(o);
	}

	public Resource get(Object key) {
		return children.get(key);
	}

	public int hashCode() {
		return children.hashCode();
	}

	public boolean isEmpty() {
		return children.isEmpty();
	}

	public Set<String> keySet() {
		return children.keySet();
	}

	public Resource put(String key, Resource value) {
		return children.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Resource> m) {
		children.putAll(m);
	}

	public Resource remove(Object key) {
		return children.remove(key);
	}

	public int size() {
		return children.size();
	}

	public Collection<Resource> values() {
		return children.values();
	}

}


