/*
 *  Copyright (C) 2009, 2010 KAWAMOTO Junpei
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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * 記憶量に上限付きのマップ．
 *
 * @param <Key> マップのキーとなるクラス
 * @param <Value> マップの値となるクラス
 *
 * @author Junpei Kawamoto
 * @since 0.1.20100629
 *
 */
public class FixedSizeMap<Key, Value> implements Map<Key, Value>{

	private final int limit;
	private final LinkedList<Map.Entry<Key, Value>> elem = new LinkedList<Map.Entry<Key, Value>>();

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * 記憶量を指定して FixedSizeMap を作成する．
	 *
	 * @param limit 記憶量の増減
	 */
	public FixedSizeMap(final int limit){

		this.limit = limit;

	}


	//============================================================================
	//  Public methods
	//============================================================================
	/**
	 * キーにマッチするマップエントリを取得する．
	 *
	 * @param key 検索するキー
	 * @return キーにマッチしたマップエントリ
	 */
	public Map.Entry<Key, Value> find(final Object key){

		if(key == null){

			return null;

		}

		for(final Map.Entry<Key, Value> e : this.elem){

			if(key.equals(e.getKey())){

				return e;

			}

		}

		return null;

	}

	/* (非 Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public Value put(final Key key, final Value value) {

		if(this.size() == this.limit){

			this.elem.removeLast();

		}

		this.elem.addFirst(new AbstractMap.SimpleEntry<Key, Value>(key, value));
		return value;

	}


	@Override
	public Value get(final Object key) {

		final Map.Entry<Key, Value> res = this.find(key);
		if(res != null){

			return res.getValue();

		}else{

			return null;

		}

	}

	@Override
	public Value remove(final Object key) {

		final Map.Entry<Key, Value> res = this.find(key);
		if(res != null){

			this.elem.remove(res);
			return res.getValue();

		}else{

			return null;

		}

	}

	@Override
	public void putAll(final Map<? extends Key, ? extends Value> m) {

		for(final Key key : m.keySet()){

			this.put(key, m.get(key));

		}

	}

	@Override
	public boolean containsKey(Object key) {

		return this.find(key) != null;

	}

	@Override
	public boolean containsValue(Object value) {

		for(final Map.Entry<Key, Value> e : this.elem){

			if(value.equals(e.getValue())){

				return true;

			}

		}

		return false;

	}

	@Override
	public int size() {

		return this.elem.size();

	}

	@Override
	public boolean isEmpty() {

		return this.elem.isEmpty();

	}

	@Override
	public void clear() {

		this.elem.clear();

	}

	@Override
	public Set<Key> keySet() {

		final Set<Key> ret = new HashSet<Key>();
		for(final Map.Entry<Key, Value> e : this.elem){

			ret.add(e.getKey());

		}

		return ret;

	}

	@Override
	public Collection<Value> values() {

		final Collection<Value> ret = new ArrayList<Value>();
		for(final Map.Entry<Key, Value> e : this.elem){

			ret.add(e.getValue());

		}

		return ret;

	}

	@Override
	public Set<Map.Entry<Key, Value>> entrySet() {

		final Set<Map.Entry<Key, Value>> ret = new HashSet<Map.Entry<Key, Value>>();
		for(final Map.Entry<Key, Value> e : this.elem){

			ret.add(e);

		}

		return ret;

	}

}
