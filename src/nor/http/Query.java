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
package nor.http;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.Codec;

/**
 * URLにおけるQuery部分を表すクラス．
 * URLは一般的に，
 * <pre>
 * shema://host:port/path?query#fragment
 * </pre>
 * の形で表わされる．本クラスが扱うのは上記のうち，queryに当たる部分である．
 *
 * このquery部分は通常URLエンコードされているが，本クラスで扱うものは，エンコード前の文字列である．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 *
 */
public class Query implements Map<String, String>{

	/**
	 * エントリ
	 */
	private final Map<String, String> _entry = new HashMap<String, String>();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Query.class.getName());

	/**
	 * Query解析用正規表現
	 */
	private static final Pattern QPAT = Pattern.compile("([^=^&]+)=([^&]+)");

	/**
	 * 空文字列
	 */
	private static final String NONE = "";


	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * 空のクエリを作成する．
	 */
	public Query(){
		LOGGER.entering(Query.class.getName(), "<init>");

		LOGGER.exiting(Query.class.getName(), "<init>");
	}

	/**
	 * Query文字列を解析して，エントリ毎に格納する．
	 * @param query 解析するquery文字列
	 */
	public Query(final String query){
		LOGGER.entering(Query.class.getName(), "<init>", query);
		assert query != null;

		final Matcher m = QPAT.matcher(query);
		while(m.find()){

			final String key = m.group(1);

			// URLデコード
			final String value = Codec.urlDecode(m.group(2));

			LOGGER.fine(String.format("Queryエントリを追加[%s : %s]", key, value));
			this.put(key, value);

		}

		if(this._entry.size() == 0){

			this.put(query, NONE);

		}

		LOGGER.exiting(Query.class.getName(), "<init>");

	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * このインスタンスが保持するQueryの文字列表現を返す．
	 * このメソッドが返す文字列表現は，URLエンコードされる．
	 *
	 * @return このインスタンスが保持するQueryの文字列表現
	 */
	@Override
	public String toString(){
		LOGGER.entering(Query.class.getName(), "toString");

		final StringBuilder builder = new StringBuilder();
		for(String key : this.keySet()){

			String value = this.get(key);
			if(NONE.equals(value)){

				builder.append(key);

			}else{

				// URLエンコード
				value = Codec.urlEncode(value);

				builder.append(key);
				builder.append("=");
				builder.append(value);
				builder.append('&');

			}

		}

		// 最後のアンパサンドを削除する
		final int last = builder.length() - 1;
		if((last >= 0) && (builder.charAt(last) == '&')){

			builder.deleteCharAt(last);

		}

		final String ret = builder.toString();

		LOGGER.exiting(Query.class.getName(), "toString", ret);
		return ret;

	}


	//============================================================================
	//  Map<String, String> の実装
	//============================================================================
	/* (non-Javadoc)
	 * @see java.util.Map#clear()
	 */
	public void clear() {
		LOGGER.entering(Query.class.getName(), "clear");

		this._entry.clear();

		LOGGER.exiting(Query.class.getName(), "clear");
	}


	/* (non-Javadoc)
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		LOGGER.entering(Query.class.getName(), "containsKey", key);
		assert key != null;

		final boolean ret = this._entry.containsKey(key);

		LOGGER.exiting(Query.class.getName(), "containsKey", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value) {
		LOGGER.entering(Query.class.getName(), "containsValue", value);

		final boolean ret = this._entry.containsValue(value);

		LOGGER.exiting(Query.class.getName(), "containsValue", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<String, String>> entrySet() {
		LOGGER.entering(Query.class.getName(), "entrySet");

		final Set<Entry<String, String>> ret = this._entry.entrySet();

		LOGGER.exiting(Query.class.getName(), "entrySet", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public final String get(Object key) {
		LOGGER.entering(Query.class.getName(), "get", key);

		final String ret = this._entry.get(key);

		LOGGER.exiting(Query.class.getName(), "get", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty() {
		LOGGER.entering(Query.class.getName(), "isEmpty");

		final boolean ret = this._entry.isEmpty();

		LOGGER.exiting(Query.class.getName(), "<init>");
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet() {
		LOGGER.entering(Query.class.getName(), "keySet");

		final Set<String> ret = this._entry.keySet();

		LOGGER.exiting(Query.class.getName(), "keySet", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public String remove(Object key) {
		LOGGER.entering(Query.class.getName(), "remove", key);

		final String ret = this._entry.remove(key);

		LOGGER.exiting(Query.class.getName(), "remove", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#values()
	 */
	public Collection<String> values() {
		LOGGER.entering(Query.class.getName(), "values");

		final Collection<String> ret = this._entry.values();

		LOGGER.exiting(Query.class.getName(), "values", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public String put(String key, String value) {
		LOGGER.entering(Query.class.getName(), "put", new Object[]{key, value});

		final String ret = this._entry.put(key, value);

		LOGGER.exiting(Query.class.getName(), "put", ret);
		return ret;
	}


	/* (non-Javadoc)
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends String> m) {
		LOGGER.entering(Query.class.getName(), "putAll", m);

		this._entry.putAll(m);

		LOGGER.exiting(Query.class.getName(), "putAll");
	}


	/* (non-Javadoc)
	 * @see java.util.Map#size()
	 */
	public int size() {
		LOGGER.entering(Query.class.getName(), "size");

		final int ret = this._entry.size();

		LOGGER.exiting(Query.class.getName(), "size", ret);
		return ret;
	}

}


