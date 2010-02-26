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

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.logging.Logger;

public class Parameter{

	private final Map<String, String> _param = new TreeMap<String, String>();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Parameter.class.getName());

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * コンストラクタ．
	 * 引数で渡されたクエリの解析を行う．
	 * 
	 * @param query クエリ
	 */
	public Parameter(final String query){
		LOGGER.entering(Parameter.class.getName(), "<init>", query);
		assert query != null;
		
		for(final String pair : query.split("&")){

			final int s = pair.indexOf("=");
			if(s != -1){

				final String key = pair.substring(0, s);
				final String value = pair.substring(s+1);
				
				this._param.put(key, value);

			}

		}

		LOGGER.exiting(Parameter.class.getName(), "<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	public boolean containsKey(Object key) {
		return _param.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return _param.containsValue(value);
	}

	public Set<Entry<String, String>> entrySet() {
		return _param.entrySet();
	}

	public String get(Object key) {
		return _param.get(key);
	}

	public boolean isEmpty() {
		return _param.isEmpty();
	}

	public Set<String> keySet() {
		return _param.keySet();
	}

	public int size() {
		return _param.size();
	}

	public Collection<String> values() {
		return _param.values();
	}
	
	

}
