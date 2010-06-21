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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Cookieヘッダを表すクラス．
 * HTTPヘッダ中のCookieヘッダを解析してキーやサブキー，そしてそれらに関連付けられた値を
 * 取りだし管理します．また，そうしたエントリに対するアクセッサも提供します．
 *
 * @author KAWAMOTO Junpei
 */
public class Cookie{

	private final HttpHeader header;
	private final Map<String, List<String>> entries = new HashMap<String, List<String>>();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Cookie.class.getName());

	//============================================================================
	//  コンストラクタ
	//============================================================================
	private Cookie(final HttpHeader header){

		this.header = header;

		if(this.header.containsKey(HeaderName.Cookie)){

			for(final String entry : this.header.get(HeaderName.Cookie)){

				final int index = entry.indexOf("=");
				if(index != -1){

					final String key = entry.substring(0, index);
					final String value = entry.substring(index+1);

					if(this.entries.containsKey(key)){

						this.entries.get(key).add(value);

					}else{

						final List<String> list = new ArrayList<String>();
						list.add(value);
						this.entries.put(key, list);

					}

				}

			}

		}

	}


	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * すべての値を削除する．
	 *
	 */
	public void clear(){
		LOGGER.entering(Cookie.class.getName(), "clear");

		this.entries.clear();
		this.header.remove(HeaderName.Cookie);

		LOGGER.exiting(Cookie.class.getName(), "clear");

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

		if(this.entries.containsKey(key)){

			this.entries.get(key).add(value);

		}else{

			final List<String> list = new ArrayList<String>();
			list.add(value);
			this.entries.put(key, list);

		}

		this.header.set(HeaderName.Cookie, this.toString());

		LOGGER.exiting(Cookie.class.getName(), "add");
	}

	public void set(final String key, final String value){
		LOGGER.entering(Cookie.class.getName(), "set", new Object[]{key, value});
		assert key != null;
		assert value != null;

		this.entries.remove(key);
		this.add(key, value);

		LOGGER.exiting(Cookie.class.getName(), "set");
	}

	/**
	 * 要素を削除する
	 *
	 * @param key 削除対象キー
	 */
	public void remove(final String key){
		LOGGER.entering(Cookie.class.getName(), "remove", key);
		assert key != null;

		this.entries.remove(key);
		this.header.set(HeaderName.Cookie, this.toString());

		LOGGER.exiting(Cookie.class.getName(), "remove");
	}

	/**
	 * 要素を取得する．
	 *
	 * @param key 取得するキー
	 * @return キーに関連付けられている値
	 */
	public String[] get(final String key){
		LOGGER.entering(Cookie.class.getName(), "get", key);
		assert key != null;

		final String[] ret = this.entries.get(key).toArray(new String[0]);

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

		final String[] ret = this.entries.keySet().toArray(new String[0]);

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
		for(final String key : this.entries.keySet()){

			for(final String value : this.entries.get(key)){

				builder.append(key);
				builder.append("=");
				builder.append(value);
				builder.append(";");

			}

		}

		if(builder.length() != 0){

			builder.setLength(builder.length()-2);

		}

		final String ret = builder.toString();
		LOGGER.config(String.format("Cookieエントリ[%s]", ret));

		LOGGER.exiting(Cookie.class.getName(), "toString", ret);
		return ret;

	}

	//====================================================================
	//  public static メソッド
	//====================================================================
	public static Cookie get(final HttpHeader header){

		return new Cookie(header);

	}


}
