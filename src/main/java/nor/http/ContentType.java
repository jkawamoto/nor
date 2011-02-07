/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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
 * コンテンツタイプ．
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
	 * 「不明」文字列定数.
	 */
	public static final String UNDEFINED = "undefined";

	/**
	 * MIMEタイプ．
	 */
	private String type = UNDEFINED;

	/**
	 * MIMEサブタイプ．
	 */
	private String subtype = UNDEFINED;

	private final Map<String, String> parameters = new HashMap<String, String>();


	/**
	 * ロガー．
	 */
	private static final Logger LOGGER = Logger.getLogger(ContentType.class.getName());

	/**
	 * ContentType解析ための正規表現．
	 */
	private static final Pattern TypePatterm = Pattern.compile("([\\w\\-]+)/([\\w\\-]+)");
	private static final Pattern ParameterPattern = Pattern.compile(";\\s*([\\w\\-]+)\\s*=\\s*([\\w\\-]+)");


	//====================================================================
	// Constructors
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
	// Public methods
	//====================================================================
	/**
	 * MIMEタイプを得る．
	 *
	 * @return MIMEタイプ
	 */
	public String getType(){
		LOGGER.entering(ContentType.class.getName(), "getMIMEType");

		final String ret = this.type;

		LOGGER.exiting(ContentType.class.getName(), "getMIMEType", ret);
		return ret;

	}

	/**
	 * MIMEサブタイプを得る．
	 *
	 * @return MIMEサブタイプ
	 */
	public String getSubtype(){
		LOGGER.entering(ContentType.class.getName(), "getMIMESubtype");

		final String ret = this.subtype;

		LOGGER.exiting(ContentType.class.getName(), "getMIMESubtype", ret);
		return ret;

	}

	public int getParameterSize(){

		return this.parameters.size();

	}

	public Set<String> getParameterKeys(){

		return this.parameters.keySet();

	}

	public boolean containsParameterKey(final String key){

		return this.parameters.containsKey(key);

	}

	public String getParameterValue(final String key){

		if(key == null){

			throw new NullPointerException("key is null");

		}

		return this.parameters.get(key);

	}

	public boolean containsParameterValue(final String value){

		return this.parameters.containsValue(value);

	}


	// syntactic sugar
	/**
	 * 文字コードを得る．
	 *
	 * @return 文字コード
	 */
	public String getCharset(){

		if(this.containsParameterKey("charset")){

			return this.getParameterValue("charset");

		}else{

			return null;

		}

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

		final StringBuilder ret = new StringBuilder();
		ret.append(this.getType());
		ret.append("/");
		ret.append(this.getSubtype());

		for(final String key : this.getParameterKeys()){

			ret.append(";");
			ret.append(key);
			ret.append("=");
			ret.append(this.getParameterValue(key));

		}

		LOGGER.exiting(ContentType.class.getName(), "toString", ret);
		return ret.toString();

	}

	@Override
	public boolean equals(final Object obj) {

		if(obj instanceof ContentType){

			final ContentType that = (ContentType)obj;
			return this.type.equals(that.type) && this.subtype.equals(that.subtype) && this.parameters.equals(that.parameters);

		}else{

			return super.equals(obj);

		}

	}

	@Override
	public int hashCode() {

		return this.type.hashCode() + this.subtype.hashCode() + this.parameters.hashCode();

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

		final Matcher mt = TypePatterm.matcher(str);
		if(mt.find()){

			this.type = mt.group(1);
			this.subtype = mt.group(2);

			final Matcher mp = ParameterPattern.matcher(str);
			while(mp.find()){

				this.parameters.put(mp.group(1), mp.group(2));

			}

		}

		LOGGER.exiting(ContentType.class.getName(), "setContentType");
	}
	//
	//	/**
	//	 * MIMEタイプを設定する．
	//	 *
	//	 * @param type 新しいMIMEタイプ
	//	 */
	//	void setMIMEType(final String type){
	//		LOGGER.entering(ContentType.class.getName(), "setMIMETypr", type);
	//		assert type != null;
	//
	//		this._type = type;
	//
	//		LOGGER.exiting(ContentType.class.getName(), "setMIMEType");
	//	}
	//
	//	/**
	//	 * MIMEサブタイプを設定する．
	//	 *
	//	 * @param subtype 新しいMIMEサブタイプ
	//	 */
	//	void setMIMESubtype(final String subtype){
	//		LOGGER.entering(ContentType.class.getName(), "setMIMESubtype", subtype);
	//		assert subtype != null;
	//
	//		this._subtype = subtype;
	//
	//		LOGGER.exiting(ContentType.class.getName(), "setMIMESubtype");
	//	}
	//
	//	/**
	//	 * 文字コードを設定する．
	//	 *
	//	 * @param charset 新しい文字コード
	//	 */
	//	// TEST: パッケージプライベートからパブリックに変更
	//	public void setCharset(final String charset){
	//		LOGGER.entering(ContentType.class.getName(), "setCharset", charset);
	//		assert charset != null;
	//
	//		this._charset = charset.toLowerCase();
	//
	//		LOGGER.exiting(ContentType.class.getName(), "setCharset");
	//	}
	//
	//	/**
	//	 * 設定されているContentTypeをクリアする．
	//	 *
	//	 */
	//	void clear(){
	//		LOGGER.entering(ContentType.class.getName(), "clear");
	//
	//		this._type = UNKNOWN;
	//		this._subtype = UNKNOWN;
	//		this._charset = UNKNOWN;
	//
	//		LOGGER.exiting(ContentType.class.getName(), "clear");
	//	}

}


