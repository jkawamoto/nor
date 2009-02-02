/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cookieヘッダフィールドをラップするクラス．
 *
 * @author KAWAMOTO Junpei
 */
public class Cookie{

	/**
	 * Cookieの項目名と値のマップ
	 */
	private final Map<String, String> _elements = new HashMap<String, String>();

	private static final String EQ = "=";
	private static final String SP = ";";

	/**
	 * Cookie解析のための正規表現
	 */
	private static final Pattern Entry = Pattern.compile("([^=^;^\\s^,]+)=([^;^,]+)");

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

		this._elements.clear();

	}

	/**
	 * Cookieヘッダ項目が空か調べる．
	 *
	 * @return ヘッダ項目が空の場合true
	 */
	public boolean isEmpty() {

		return this._elements.isEmpty();

	}

	/**
	 * 要素を追加する．
	 *
	 * @param key キー
	 * @param value 値
	 */
	public void add(final String key, final String value){
		assert key != null;
		assert value != null;

		this._elements.put(key, value);

	}

	/**
	 * 要素名keyを削除する．
	 *
	 * @param key 削除対象のキー
	 */
	public void remove(final String key){
		assert key != null;

		this._elements.remove(key);

	}

	/**
	 * 要素名keyに対応する値を取得する．
	 *
	 * @param key 取得するキー
	 * @return キーに関連付けられている値
	 */
	public String get(final String key){
		assert key != null;

		return this._elements.get(key);

	}

	/**
	 * キー集合の取得．
	 *
	 * @return キー集合
	 */
	public Set<String> keySet() {

		return this._elements.keySet();

	}

	/**
	 * Cookie情報を一つの文字列に変換する．
	 *
	 * @return Cookie情報を表す文字列
	 */
	@Override
	public String toString(){

		final StringBuilder builder = new StringBuilder();
		for(final String key : this._elements.keySet()){

			builder.append(key);
			builder.append(EQ);
			builder.append(this._elements.get(key));
			builder.append(SP);
			builder.append(" ");

		}

		final String ret = builder.toString();
		LOGGER.config(String.format("Cookieエントリ[%s]", ret));

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
		assert cookie != null;
		LOGGER.config(String.format("解析対象Cookieの受信[%s]", cookie));

		// 初期化
		this.clear();

		final Matcher m = Entry.matcher(cookie);
		while(m.find()){

			final String key = m.group(1);
			final String value = m.group(2);

			this.add(key, value);

		}

	}

}
