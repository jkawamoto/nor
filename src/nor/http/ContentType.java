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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTPメッセージのコンテンツタイプ．
 * MIMEタイプは text/html の様に type/subtype の形で表される．
 * また，文字コードが指定されている場合，
 * <pre>
 * type/subtype; charset=enctype
 * </pre>
 * の形で表される．このクラスはMIMEタイプと文字コードに対するアクセッサを提供する．
 *
 * @author KAWAMOTO Junpei
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
	 * MIME type
	 */
	private String _type = UNKNOWN;

	/**
	 * MIME sub-type
	 */
	private String _subtype = UNKNOWN;

	/**
	 * 文字コード
	 */
	private String _charset = UNKNOWN;

	/**
	 * 「不明」文字列定数.
	 */
	public static final String UNKNOWN = "unknown";

	/**
	 * ContentType解析ための正規表現
	 * ([^/]+)/([^\s^;]+)\s*;?(\s*charset\s*=\s*(.+))?
	 */
	private static final Pattern ContentType = Pattern.compile("([^/]+)/([^\\s^;]+)\\s*;?(\\s*charset\\s*=\\s*\"+([^\"]+)\"+)?");

	/**
	 * 文字コード指定無しのテンプレート
	 */
	private static final String TemplateNoCharset = "%s/%s";

	/**
	 * 文字コード指定ありのテンプレート
	 */
	private static final String Template = "%s/%s; charset=%s";

	//====================================================================
	//	コンストラクタ
	//====================================================================
	/**
	 * 情報不明のコンテンツタイプを作成する．
	 */
	public ContentType(){
	}

	/**
	 * 文字列strからコンテンツタイプを作成する．
	 *
	 * @param str コンテンツタイプを表す文字列
	 */
	public ContentType(final String str){

		this.setContentType(str);

	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * MIMEタイプの取得．
	 *
	 * @return MIMEタイプ
	 */
	public String getMIMEType(){

		return this._type;

	}

	/**
	 * MIMEサブタイプの取得．
	 *
	 * @return MIMEサブタイプ
	 */
	public String getMIMESubtype(){

		return this._subtype;

	}

	/**
	 * 文字コードの取得．
	 *
	 * @return 文字コード
	 */
	public String getCharset(){

		return this._charset;

	}

	/**
	 * ContentTyppヘッダフィールド値の取得．
	 * ヘッダフィールド用にフォーマットされた文字列を返す．
	 * ただし，ContentTypeが指定されていない場合は空文字を返す．
	 *
	 * @return ContentTypeを表す文字列
	 */
	@Override
	public String toString(){

		String ret = "";
		if(!UNKNOWN.equals(this.getMIMEType()) && !UNKNOWN.equals(this.getMIMESubtype())){

			if(UNKNOWN.equals(this.getCharset())){

				ret = String.format(TemplateNoCharset, this.getMIMEType(), this.getMIMESubtype());

			}else{

				ret = String.format(Template, this.getMIMEType(), this.getMIMESubtype(), this.getCharset());

			}

		}

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
		assert str != null;

		final Matcher m = ContentType.matcher(str);
		if(m.find()){

			this._type = m.group(1);
			this._subtype = m.group(2);

			final String charset = m.group(4);
			if(charset != null){

				this._charset = charset.toLowerCase();

			}

		}

	}

	/**
	 * MIMEタイプを設定する．
	 *
	 * @param type 新しいMIMEタイプ
	 */
	void setMIMEType(final String type){
		assert type != null;

		this._type = type;

	}

	/**
	 * MIMEサブタイプを設定する．
	 *
	 * @param subtype 新しいMIMEサブタイプ
	 */
	void setMIMESubtype(final String subtype){
		assert subtype != null;

		this._subtype = subtype;

	}

	/**
	 * 文字コードを設定する．
	 *
	 * @param charset 新しい文字コード
	 */
	void setCharset(final String charset){
		assert charset != null;

		this._charset = charset.toLowerCase();

	}

	/**
	 * 設定されているContentTypeをクリアする．
	 *
	 */
	void clear(){

		this._type = UNKNOWN;
		this._subtype = UNKNOWN;
		this._charset = UNKNOWN;

	}
}
