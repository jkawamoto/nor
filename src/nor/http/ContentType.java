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
// $Id: ContentType.java 411 2010-01-11 09:51:02Z kawamoto $
package nor.http;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * コンテンツタイプを表すオブジェクト．
 * MIMEタイプは text/html の様に type/subtype の形で表される．
 * また，文字コードが指定されている場合，
 * <pre>
 * type/subtype; charset=enctype
 * </pre>
 * の形で表される．このオブジェクトはMIMEタイプと文字コードに対するアクセッサを提供する．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 *
 */
public class ContentType {

	/**
	 * 定義済みMIMEタイプ．
	 *
	 * @author KAWAMOTO Junpei
	 *
	 */
	public final class MIMEType{
		public static final String TEXT = "text";
		public static final String MESSAGE = "message";
		public static final String APPLICATION = "application";
		public static final String IMAGE = "image";
		public static final String AUDIO = "audio";
		public static final String VIDEO = "video";
		public static final String MODEL = "model";
		public static final String MULTIPART = "multipart";
	}

	public final class MIMESubType{
		public static final String HTML = "html";
	}

	/**
	 * MIMEタイプ．
	 */
	private String _type = UNKNOWN;

	/**
	 * MIMEサブタイプ．
	 */
	private String _subtype = UNKNOWN;

	/**
	 * 文字コード．
	 */
	private String _charset = UNKNOWN;

	/**
	 * 「不明」文字列定数.
	 */
	public static final String UNKNOWN = "unknown";

	/**
	 * ロガー．
	 */
	private static final Logger LOGGER = Logger.getLogger(ContentType.class.getName());

	/**
	 * ContentType解析ための正規表現．
	 * ([^/]+)/([^\s^;]+)\s*;?(\s*charset\s*=\s*(.+))?
	 */
	private static final Pattern CONTENT_TYPE = Pattern.compile("([^/]+)/([^\\s^;]+)\\s*;?(\\s*charset\\s*=\\s*\"+([^\"]+)\"+)?");


	//====================================================================
	//	コンストラクタ
	//====================================================================
	/**
	 * 情報不明のコンテンツタイプを作成する．
	 */
	public ContentType(){
		LOGGER.entering(ContentType.class.getName(), "<init>");

		LOGGER.exiting(ContentType.class.getName(), "<init>");
	}

	/**
	 * コンテンツタイプを表す文字列からコンテンツタイプを作成する．
	 *
	 * @param str コンテンツタイプを表す文字列
	 */
	public ContentType(final String str){
		LOGGER.entering(ContentType.class.getName(), "<init>", str);

		this.setContentType(str);

		LOGGER.exiting(ContentType.class.getName(), "<init>");

	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * MIMEタイプを得る．
	 *
	 * @return MIMEタイプ
	 */
	public String getMIMEType(){
		LOGGER.entering(ContentType.class.getName(), "getMIMEType");

		final String ret = this._type;

		LOGGER.exiting(ContentType.class.getName(), "getMIMEType", ret);
		return ret;

	}

	/**
	 * MIMEサブタイプを得る．
	 *
	 * @return MIMEサブタイプ
	 */
	public String getMIMESubtype(){
		LOGGER.entering(ContentType.class.getName(), "getMIMESubtype");

		final String ret = this._subtype;

		LOGGER.exiting(ContentType.class.getName(), "getMIMESubtype", ret);
		return ret;

	}

	/**
	 * 文字コードを得る．
	 *
	 * @return 文字コード
	 */
	public String getCharset(){
		LOGGER.entering(ContentType.class.getName(), "getCharset");

		final String ret = this._charset;

		LOGGER.exiting(ContentType.class.getName(), "getCharset", ret);
		return ret;

	}

	/**
	 * ContentTypeが利用可能かどうか．
	 * 少なくともMIMETypeが指定されている時，利用可能であるとみなす．
	 *
	 * @return ContentTypeが利用可能な場合trueを返す．
	 */
	public boolean isAvailable(){
		LOGGER.entering(ContentType.class.getName(), "isUnknown");

		final boolean ret = !UNKNOWN.equals(this.getMIMEType());

		LOGGER.exiting(ContentType.class.getName(), "isUnknown", ret);
		return ret;
	}

	/**
	 * ContentType文字列を得る．
	 * ContentTypeが指定されていない場合は，空文字を返す．
	 *
	 * @return ContentTypeを表す文字列
	 */
	@Override
	public String toString(){
		LOGGER.entering(ContentType.class.getName(), "toString");

		String ret = "";
		if(!UNKNOWN.equals(this.getMIMEType()) && !UNKNOWN.equals(this.getMIMESubtype())){

			if(UNKNOWN.equals(this.getCharset())){

				ret = String.format("%s/%s;", this.getMIMEType(), this.getMIMESubtype());

			}else{

				ret = String.format("%s/%s; charset=%s", this.getMIMEType(), this.getMIMESubtype(), this.getCharset());

			}

		}

		LOGGER.exiting(ContentType.class.getName(), "toString", ret);
		return ret;

	}

	//====================================================================
	//	package private メソッド
	//====================================================================
	/**
	 * ContentTypeをパースし設定する．
	 * @param str このオブジェクトに設定するContentType文字列．
	 */
	void setContentType(final String str){
		LOGGER.entering(ContentType.class.getName(), "setContentType", str);
		assert str != null;

		final Matcher m = CONTENT_TYPE.matcher(str);
		if(m.find()){

			this._type = m.group(1);
			this._subtype = m.group(2);

			final String charset = m.group(4);
			if(charset != null){

				this._charset = charset.toLowerCase();

			}

		}

		LOGGER.exiting(ContentType.class.getName(), "setContentType");
	}

	/**
	 * MIMEタイプを設定する．
	 *
	 * @param type 新しいMIMEタイプ
	 */
	void setMIMEType(final String type){
		LOGGER.entering(ContentType.class.getName(), "setMIMETypr", type);
		assert type != null;

		this._type = type;

		LOGGER.exiting(ContentType.class.getName(), "setMIMEType");
	}

	/**
	 * MIMEサブタイプを設定する．
	 *
	 * @param subtype 新しいMIMEサブタイプ
	 */
	void setMIMESubtype(final String subtype){
		LOGGER.entering(ContentType.class.getName(), "setMIMESubtype", subtype);
		assert subtype != null;

		this._subtype = subtype;

		LOGGER.exiting(ContentType.class.getName(), "setMIMESubtype");
	}

	/**
	 * 文字コードを設定する．
	 *
	 * @param charset 新しい文字コード
	 */
	// TEST: パッケージプライベートからパブリックに変更
	public void setCharset(final String charset){
		LOGGER.entering(ContentType.class.getName(), "setCharset", charset);
		assert charset != null;

		this._charset = charset.toLowerCase();

		LOGGER.exiting(ContentType.class.getName(), "setCharset");
	}

	/**
	 * 設定されているContentTypeをクリアする．
	 *
	 */
	void clear(){
		LOGGER.entering(ContentType.class.getName(), "clear");

		this._type = UNKNOWN;
		this._subtype = UNKNOWN;
		this._charset = UNKNOWN;

		LOGGER.exiting(ContentType.class.getName(), "clear");
	}

}


