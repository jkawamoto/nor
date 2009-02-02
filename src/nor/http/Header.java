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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTPメッセージヘッダを表すクラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class Header{

	/**
	 * 親HTTPメッセージ
	 */
	protected final Message _parent;

	/**
	 * ヘッダの要素コレクション
	 */
	private final Map<String, String> _elements = new HashMap<String, String>();

	/**
	 * Cookieヘッダフィールド
	 */
	private final Cookie _cookie = new Cookie();

	/**
	 * Set-Cookieヘッダフィールド
	 */
	private final List<SetCookie> _setCookies = new ArrayList<SetCookie>();

	/**
	 * ContentTypeヘッダフィールド
	 */
	private final ContentType _contentType = new ContentType();

	/**
	 * ヘッダ解析用の正規表現
	 */
	private static final Pattern Header = Pattern.compile("^([^:]+):\\s*(.*)$");

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Header.class.getName());

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * 空のヘッダフィールドから成るHeaderを作成する．
	 *
	 * @param parent このヘッダを持つ親HTTPメッセージ
	 */
	public Header(final Message parent){
		assert parent != null;

		this._parent = parent;

	}

	/**
	 * 入力バッファリーダreaderを元にHeaderを作成する．
	 *
	 * @param parent このヘッダを持つ親HTTPメッセージ
	 * @param reader ヘッダ情報を読み取るストリームリーダ
	 * @throws IOException ストリーム処理中にI/Oエラーが発生した場合
	 */
	public Header(final Message parent, final BufferedReader reader) throws IOException{
		this(parent);
		assert reader != null;

		this.readHeader(reader);

	}


	/**
	 * マップelementsをヘッダフィールドとするHeaderを作成する．
	 * 作成されたHeaderは，elementsのソフトコピーを持つ
	 *
	 * @param parent このヘッダを持つ親HTTPメッセージ
	 * @param elements このヘッダに持たせる要素マップ
	 */
	public Header(final Message parent, final Map<String, String> elements){
		this(parent);
		assert elements != null;

		this.setAll(elements);

	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * ヘッダフィールドを追加する．
	 * keyをヘッダ名とし，valueをその値とするヘッダフィールドを追加する．
	 * 同名のヘッダフィールドが既に存在する場合は新しい値で上書きされる．
	 *
	 * @param key 追加するヘッダフィールド名
	 * @param value keyに設定する値
	 */
	public void set(final String key, final String value){
		assert key != null;
		assert value != null;

		if(HeaderName.Cookie.equalsIgnoreCase(key)){

			// Cookieヘッダの場合
			this._cookie.parse(value);

		}else if(HeaderName.ContentType.equalsIgnoreCase(key)){

			// Content-Typeヘッダの場合
			this._contentType.setContentType(value);

		}else if(HeaderName.SetCookie.equalsIgnoreCase(key)){

			// Set-Cookieヘッダの場合
			final SetCookie c = new SetCookie();
			c.parse(value);

			this._setCookies.add(c);

		}else{

			if(this._elements.containsKey(key)){

				this._elements.remove(key);

			}
			this._elements.put(key.toLowerCase(), value);

			LOGGER.fine(String.format("ヘッダ項目を追加[%s : %s]", key, value));

		}

	}

	/**
	 * elementsに含まれるヘッダフィールドすべて追加する．
	 *
	 * @param elements このヘッダに追加するマップ
	 */
	public void setAll(final Map<String, String> elements){
		assert elements != null;

		for(final String key : elements.keySet()){

			if(key != null){

				this.set(key.toLowerCase(), elements.get(key));

			}

		}

	}

	/**
	 * ヘッダフィールドを削除する．
	 * ヘッダ名keyとその値を削除する．
	 *
	 * @param key 削除するヘッダフィールド名
	 */
	public void remove(final String key){
		assert key != null;

		if(HeaderName.Cookie.equalsIgnoreCase(key)){

			// Cookieヘッダの場合
			this._cookie.clear();

		}else if(HeaderName.ContentType.equalsIgnoreCase(key)){

			// Content-Typeヘッダの場合
			this._contentType.clear();

		}else if(HeaderName.SetCookie.equalsIgnoreCase(key)){

			// Set-Cookieヘッダの場合
			this._setCookies.clear();

		}else{

			this._elements.remove(key);

		}

	}

	/**
	 * すべてのヘッダフィールドを削除する．
	 * このヘッダに登録されているすべてのヘッダフィールドを削除する．
	 * 必須ヘッダフィールドであっても削除されるため，有効なHTTPメッセージとするためには，
	 * 必須項目を追加しなければならない．
	 *
	 */
	public void clear(){

		this._cookie.clear();
		this._setCookies.clear();
		this._contentType.clear();
		this._elements.clear();

	}

	/**
	 * ヘッダフィールド値を取得する．
	 * ヘッダ名keyに関連付けられた値を返す．
	 * 登録されていないヘッダフィールドを要求した場合は，nullが返される．
	 *
	 * @param key 取得するヘッダフィールド名
	 * @return ヘッダ名keyに関連付けられている値
	 */
	public String get(final String key){
		assert key != null;

		String ret = null;
		if(HeaderName.Cookie.equalsIgnoreCase(key)){

			// Cookieヘッダの場合
			ret = this._cookie.toString();

		}else if(HeaderName.ContentType.equalsIgnoreCase(key)){

			// Content-Typeヘッダの場合
			ret = this._contentType.toString();

		}else if(HeaderName.SetCookie.equalsIgnoreCase(key)){

			// Set-Cookieヘッダの場合
			ret = this._setCookies.toString();

		}else{

			ret = this._elements.get(key);

		}

		return ret;
	}

	/**
	 * ヘッダフィールド名keyが含まれているか調べる．
	 *
	 * @param key 調べるヘッダフィールド名
	 * @return フィールドkeyが含まれている場合ture
	 */
	public boolean containsKey(final String key){
		assert key != null;

		// TODO: Cookieの場合
		final boolean ret = this._elements.containsKey(key);

		return ret;
	}


	/**
	 * ヘッダフィールドにvalueが設定されているか調べる．
	 *
	 * @param key 調べるヘッダフィールド名
	 * @param value 調べるヘッダフィールド値
	 * @return valueが設定されている場合true
	 */
	public boolean containsValue(final String key, final String value){

		return this._elements.containsKey(key) && this._elements.get(key).contains(value);

	}

	/**
	 * このヘッダに登録されているキー集合の取得．
	 *
	 * @return このヘッダに登録されているキー集合
	 */
	public Set<String> keySet(){

		final Set<String> ret = this._elements.keySet();

		return ret;
	}

	/**
	 * 登録されているヘッダ項目数の取得．
	 *
	 * @return このヘッダに登録されている項目数
	 */
	public int size(){

		// TODO: Cookieの場合
		final int ret = this._elements.size();

		return ret;

	}

	/**
	 * ストリームライタへの書き出し．
	 * このメソッドは書き出し先のストリームを閉じないので，呼び出し側で処理する必要がある．
	 *
	 * @param writer 書き出し先のストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	public void writeHeader(final BufferedWriter writer) throws IOException{
		assert writer != null;

		// ヘッダを追加
		for(final String key : this._elements.keySet()){

			writer.append(key);
			writer.append(": ");
			writer.append(this.get(key));
			writer.newLine();

		}

		// Cookieを追加
		if(!this._cookie.isEmpty()){

			writer.append(HeaderName.Cookie);
			writer.append(": ");
			writer.append(this._cookie.toString());
			writer.newLine();

		}

		// Set-Cookieを追加
		for(final SetCookie c : this._setCookies){

			if(c.isAvailable()){

				writer.append(HeaderName.SetCookie);
				writer.append(": ");
				writer.append(c.toString());
				writer.newLine();

			}

		}

		// ContentTypeを追加
		if(!this._contentType.getMIMEType().equals(ContentType.UNKNOWN)){

			writer.append(HeaderName.ContentType);
			writer.append(": ");
			writer.append(this._contentType.toString());
			writer.newLine();

		}

		writer.flush();

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){

		final StringWriter buffer = new StringWriter();
		String ret = "";
		try {

			this.writeHeader(new BufferedWriter(buffer));
			ret = buffer.toString();

		} catch (IOException e) {

			LOGGER.warning(e.getLocalizedMessage());

		}

		return ret;
	}

	/**
	 * Cookieヘッダフィールドの取得．
	 *
	 * @return このオブジェクトに関連するCookie
	 */
	public Cookie getCookie(){

		return this._cookie;

	}

	/**
	 * Set-Cookieヘッダフィールドの取得．
	 *
	 * @return Set-Cookieヘッダの値
	 */
	public List<SetCookie> getSetCookies(){

		final List<SetCookie> ret = this._setCookies;

		return ret;
	}

	/**
	 * ContentTypeヘッダフィールドの取得．
	 * ヘッダにContentTypeが指定されていない場合nullを返す．
	 *
	 * @return ContentTypeオブジェクト
	 */
	public ContentType getContentType(){

		final ContentType ret = this._contentType;

		return ret;

	}

	//============================================================================
	//  private メソッド
	//============================================================================
	/**
	 * ストリームリーダからヘッダフィールドを読み取る．
	 * 入力ストリームリーダreaderからヘッダ情報を読み取る．
	 * このメソッドはストリームを閉じない．
	 *
	 * @param reader 解析するバッファリングされたストリーム
	 * @throws IOException ストリーム読み込み時にI/Oエラーが発生した場合
	 */
	private void readHeader(final BufferedReader reader) throws IOException{
		assert reader != null;

		for(String line = reader.readLine(); line != null; line = reader.readLine()){

			// ヘッダ記述のシンタクスに合致するか
			final Matcher m = Header.matcher(line);
			if(m.matches()){

				final String key = m.group(1).toLowerCase();
				if(key != null){

					final String value = m.group(2);
					this.set(key, value);

				}

			}

		}

	}

}
