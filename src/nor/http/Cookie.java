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
package nor.http;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cookieヘッダを表すクラス．
 * HTTPヘッダ中のCookieヘッダを解析してキーやサブキー，そしてそれらに関連付けられた値を
 * 取りだし管理します．また，そうしたエントリに対するアクセッサも提供します．
 *
 * @author KAWAMOTO Junpei
 */
public class Cookie{

	/**
	 * Cookieのペアを表すクラス
	 *
	 * @author KAWAMOTO Junpei
	 *
	 */
	private class Pair{

		/**
		 * データのキー
		 */
		private String _key;

		/**
		 * データの値
		 */
		private String _value;

		public Pair(final String key, final String value){

			this._key = key;
			this._value = value;

		}

		public String getKey(){

			return this._key;

		}

		public String getValue(){

			return this._value;

		}

//		public void setValue(final String value){
//
//			this._value = value;
//
//		}

	}

	private final List<Pair> _pairs = new ArrayList<Pair>();

	private static final String EQ = "=";
	private static final String SP = ";";

	/**
	 * Cookie解析のための正規表現
	 */
	private static final Pattern ENTRY = Pattern.compile("([^=^;^\\s^,]+)=([^;^,]+)");

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Cookie.class.getName());

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * すべての値を削除する．
	 *
	 */
	public void clear(){
		LOGGER.entering(Cookie.class.getName(), "clear");

		this._pairs.clear();

		LOGGER.exiting(Cookie.class.getName(), "clear");

	}

	/**
	 * SetCookieヘッダが有効かどうか．
	 *
	 * @return 有効な場合trueを返す
	 */
	public boolean isAvailable() {
		LOGGER.entering(Cookie.class.getName(), "isAvailable");

		final boolean ret = !this._pairs.isEmpty();

		LOGGER.exiting(Cookie.class.getName(), "isAvailable", ret);
		return ret;

	}

	/**
	 * 要素を追加する．
	 *
	 * @param key キー
	 * @param value 値
	 */
	public void add(final String key, final String value){
		LOGGER.entering(Cookie.class.getName(), "add", new Object[]{key, value});
		assert key != null;
		assert value != null;

		this._pairs.add(new Pair(key, value));

		LOGGER.exiting(Cookie.class.getName(), "add");
	}

	/**
	 * 要素を削除する
	 *
	 * @param key 削除対象キー
	 */
	public void remove(final String key){
		LOGGER.entering(Cookie.class.getName(), "remove", key);
		assert key != null;

		final Iterator<Pair> iter = this._pairs.iterator();
		while(iter.hasNext()){

			final Pair pair = iter.next();
			if(key.equals(pair.getKey())){

				iter.remove();

			}

		}

		LOGGER.exiting(Cookie.class.getName(), "remove");
	}

	/**
	 * 要素を取得する．
	 *
	 * @param key 取得するキー
	 * @return キーに関連付けられている値
	 */
	public String get(final String key){
		LOGGER.entering(Cookie.class.getName(), "get", key);
		assert key != null;

		String ret = null;
		for(final Pair pair : this._pairs){

			if(key.equals(pair.getKey())){

				ret = pair.getValue();

			}

		}

		LOGGER.exiting(Cookie.class.getName(), "get", ret);
		return ret;
	}

	/**
	 * キー集合の取得
	 *
	 * @return キー集合
	 */
	public String[] keys() {
		LOGGER.entering(Cookie.class.getName(), "keys");

		final String[] ret = new String[this._pairs.size()];

		int i = 0;
		for(final Pair pair : this._pairs){

			ret[i++] = pair.getKey();

		}

		LOGGER.exiting(Cookie.class.getName(), "keys", ret);
		return ret;
	}

	/**
	 * Cookie情報を一つの文字列に変換する．
	 *
	 * @return Cookie情報を表す文字列
	 */
	@Override
	public String toString(){
		LOGGER.entering(Cookie.class.getName(), "toString");

		final StringBuilder builder = new StringBuilder();
		for(final Pair pair : this._pairs){

			builder.append(pair.getKey());
			builder.append(EQ);
			builder.append(pair.getValue());
			builder.append(SP);
			builder.append(" ");

		}

		final String ret = builder.toString();
		LOGGER.config(String.format("Cookieエントリ[%s]", ret));

		LOGGER.exiting(Cookie.class.getName(), "toString", ret);
		return ret;

	}

	//====================================================================
	//  package private メソッド
	//====================================================================
	/**
	 * Cookieの各エントリを解析する．
	 *
	 * @param cookie 解析するCookie文字列
	 */
	void parse(final String cookie){
		LOGGER.entering(Cookie.class.getName(), "parse", cookie);
		assert cookie != null;
		LOGGER.config(String.format("解析対象Cookieの受信[%s]", cookie));

		// 初期化
		this._pairs.clear();

		final Matcher m = ENTRY.matcher(cookie);
		while(m.find()){

			final String key = m.group(1);
			final String value = m.group(2);

			this._pairs.add(new Pair(key, value));

		}

		LOGGER.exiting(Cookie.class.getName(), "parse");

	}

}
