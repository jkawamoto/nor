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

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SetCookieヘッダフィールドをラップするクラス．
 * SetCookieヘッダは以下のようなフォーマットになっている．
 * <p>
 * Set-Cookie: num=123456; expires=Sun, 10-Jun-2001 12:00:00 GMT; path=/HTTP/
 * </p>
 *
 * @author KAWAMOTO Junpei
 *
 */
public class SetCookie {

	private String _key = null;
	private String _value = null;
	private String _date = null;
	private String _path = null;
	private String _domain = null;
	private boolean _isSecure = false;

	private static String SP = ";";
	private static String EQ = "=";
	private static String SECURE = "secure";
	private static String EXPIRES = "expires";
	private static String PATH = "path";
	private static String DOMAIN = "domain";

	private static Pattern ENTRYP_ATTERN = Pattern.compile("\\s*([^=]+)=([^;]+)");

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(SetCookie.class.getName());

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * SetCookieヘッダが有効か調べる．
	 *
	 * @return 有効な場合trueを返す
	 */
	public boolean isAvailable(){

		return this._key != null;

	}

	/**
	 * 内容をクリアする．
	 */
	public void clear(){

		this._key = null;
		this._value = null;
		this._date = null;
		this._path = null;
		this._domain = null;
		this._isSecure = false;

	}

	/**
	 * キーを取得する．
	 *
	 * @return キー
	 */
	public String getKey(){
		LOGGER.entering(SetCookie.class.getName(), "getKey");

		final String ret = this._key;

		LOGGER.exiting(SetCookie.class.getName(), "getKey", ret);
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		LOGGER.entering(SetCookie.class.getName(), "toString");

		final StringBuffer buf = new StringBuffer();

		buf.append(this._key);
		buf.append(EQ);
		buf.append(this._value);

		if(this._date != null){

			buf.append(SP);

			buf.append(EXPIRES);
			buf.append(EQ);
			buf.append(this._date);

		}

		if(this._path != null){

			buf.append(SP);

			buf.append(PATH);
			buf.append(EQ);
			buf.append(this._path);

		}

		if(this._domain != null){

			buf.append(SP);

			buf.append(DOMAIN);
			buf.append(EQ);
			buf.append(this._domain);

		}

		if(this._isSecure){

			buf.append(SP);

			buf.append(SECURE);

		}

		final String ret = buf.toString();
		LOGGER.exiting(SetCookie.class.getName(), "toString", ret);
		return ret;

	}

	//============================================================================
	//  package private メソッド
	//============================================================================
	/**
	 * SetCookieヘッダをパースする．
	 *
	 * @param src 解析対象の文字列
	 */
	void parse(final String src){
		LOGGER.entering(SetCookie.class.getName(), "parse", src);
		assert src != null;

		// 初期設定
		this._isSecure = false;

		for(final String e : src.split(SP)){


			if(SECURE.equalsIgnoreCase(e.trim())){

				this._isSecure = true;

			}else{

				final Matcher m = ENTRYP_ATTERN.matcher(e);
				if(m.find()){

					final String key = m.group(1);
					final String value = m.group(2);

					if(EXPIRES.equalsIgnoreCase(key)){

						this._date = value;

					}else if(PATH.equalsIgnoreCase(key)){

						this._date = value;

					}else if(DOMAIN.equalsIgnoreCase(key)){

						this._domain = value;

					}else{

						this._key = key;
						this._value = value;

					}

				}

			}

		}

		LOGGER.exiting(SetCookie.class.getName(), "parse");
	}

}


