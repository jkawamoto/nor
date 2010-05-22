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
//$Id: HttpHeader.java 471 2010-04-03 10:25:20Z kawamoto $
package nor.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.log.EasyLogger;


/**
 * Httpヘッダを表すクラス．
 * Httpヘッダは，
 * <pre>
 *   key:value1;value2;value3・・・
 * </pre>
 * というシンタクスで記述される．つまり，一つのkeyに対して複数の値が存在する．
 * このクラスはこれらのヘッダに関してアクセッサを提供するとともに，ストリームへの
 * 入出力メソッドも提供する．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HttpHeader{

	/**
	 * 親HTTPメッセージ
	 */
	@SuppressWarnings("unused")
	private final HttpMessage parent;

	/**
	 * ヘッダ解析用の正規表現
	 */
	private static final Pattern HEADER = Pattern.compile("^([^:]+):\\s*(.*)$");

	/**
	 * ヘッダの要素コレクション
	 */
	private final Map<String, String> elements = new HashMap<String, String>();

	// TODO: ヘッダ要素はリストを使用する：ヘッダ要素のtoStringはマージした文字列を返す

	/**
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpHeader.class);

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * 空のHTTPヘッダオブジェクトを作成する．
	 *
	 * @param parent
	 */
	HttpHeader(final HttpMessage parent){
		LOGGER.entering("<init>", parent);
		assert parent != null;

		this.parent = parent;

		LOGGER.exiting("<init>");
	}

	/**
	 * 与えられたストリームを解析してヘッダオブジェクトを構築する．
	 *
	 * @param parent
	 * @param reader ヘッダ情報を読み取るストリーム
	 * @throws IOException ストリーム処理中にI/Oエラーが発生した場合
	 */
	HttpHeader(final HttpMessage parent, final BufferedReader reader) throws IOException{
		LOGGER.entering("<init>", parent, reader);
		assert parent != null;
		assert reader != null;

		this.parent = parent;
		this.readHeader(reader);

		LOGGER.exiting("<init>");
	}


	/**
	 * 引数で指定されたコレクションを持つHTTPヘッダオブジェクトを作成する．
	 * オブジェクトはコレクションのソフトコピーを所持する．
	 *
	 * @param parent
	 * @param elements このオブジェクトに持たせるヘッダ要素
	 */
	HttpHeader(final HttpMessage parent, final Map<String, String> elements){
		LOGGER.entering("<init>", parent, elements);
		assert parent != null;
		assert elements != null;

		this.parent = parent;
		this.setAll(elements);

		LOGGER.exiting("<init>");
	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 *
	 * @param key 追加するkey
	 * @param value 追加する値
	 */
	public void set(final String key, final String value){
		LOGGER.entering("set", key, value);
		assert key != null;
		assert value != null;

			if(this.elements.containsKey(key)){

				this.elements.remove(key);

			}
			this.elements.put(key.toLowerCase(), value);

			LOGGER.fine(String.format("ヘッダ項目を追加[%s : %s]", key, value));

		LOGGER.exiting("set");
	}


	/**
	 *
	 * @param key 追加するkey
	 * @param value 追加する値
	 */
	public void add(final String key, final String value){

		if(this.elements.containsKey(key)){

			final String nvalue = String.format("%s, %s", this.elements.get(key), value);
			this.elements.remove(key);
			this.elements.put(key, nvalue);

		}else{

			this.elements.put(key, value);

		}

	}


	/**
	 * HTTPヘッダオブジェクトからkeyに対する値をすべて削除する．
	 * 指定されたkeyに関連付けられている値をすべて削除します．
	 *
	 * @param key 削除するkey
	 */
	public void remove(final String key){
		LOGGER.entering("remove", key);
		assert key != null;

		this.elements.remove(key);

		LOGGER.exiting("remove");

	}

	/**
	 * コレクションを初期化する．
	 * このオブジェクトが保持していたすべてのkey，valueを削除する．
	 *
	 */
	public void clear(){
		LOGGER.entering("clear");

		this.elements.clear();

		LOGGER.exiting("clear");
	}


	/**
	 * keyに関連付けられた値のリストを返す．
	 *
	 * @param key 値リストを取得するkey
	 * @return keyに関連付けられている値のコレクション
	 */
	public String get(final String key){
		LOGGER.entering("getValue", key);
		assert key != null;

		final String ret = this.elements.get(key);

		LOGGER.exiting("getValues", ret);
		return ret;

	}

	/**
	 * ヘッダコレクションに指定したkeyが含まれているか調べる．
	 *
	 * @param key 含まれているか調べるkey
	 * @return keyが含まれている場合tureを返す
	 */
	public boolean containsKey(final String key){
		LOGGER.entering("containsKey", key);
		assert key != null;

		// TODO: Cookieの場合
		final boolean ret = this.elements.containsKey(key);

		LOGGER.exiting("containsKey", ret);
		return ret;

	}

	public boolean containsValue(final String key, final String value){

		final String v = this.get(key);
		if(v != null){

			for(final String s : v.split(",")){

				if(s.equals(value)){

					return true;

				}

			}

		}

		return false;

	}

	/**
	 * このヘッダに登録されているキーの集合を返す．
	 *
	 * @return このヘッダオブジェクトに登録されているキー集合
	 */
	public Set<String> keySet(){
		LOGGER.entering("keySet");

		final Set<String> ret = this.elements.keySet();

		LOGGER.exiting("keySet", ret);
		return ret;

	}

	/**
	 * このヘッダに登録されているキーの数を返す．
	 *
	 * @return このヘッダに登録されているキーの数
	 */
	public int getKeySize(){
		LOGGER.entering("getKeySize");

		final int ret = this.elements.size();

		LOGGER.exiting("getKeySize", ret);
		return ret;

	}

	/**
	 * ヘッダをストリームに書き出す．
	 * このメソッドは書き出し先のストリームを閉じないので，呼び出し側で処理する必要がある．
	 *
	 * @param writer 書き出し先のストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	public void writeHeader(final BufferedWriter writer) throws IOException{
		LOGGER.entering("writeHeader", writer);
		assert writer != null;

		// ヘッダを追加
		for(final String key : this.elements.keySet()){

			writer.append(key);
			writer.append(": ");
			writer.append(this.get(key));
			writer.newLine();

		}

		writer.flush();

		LOGGER.exiting("writeHeader");

	}

	/**
	 * ヘッダ情報を一つの文字列にして返す．
	 *
	 * @return このヘッダオブジェクトの文字列表現
	 */
	@Override
	public String toString(){
		LOGGER.entering("toString");

		final StringWriter buffer = new StringWriter();
		String ret = "";
		try {

			this.writeHeader(new BufferedWriter(buffer));
			ret = buffer.toString();

		} catch (IOException e) {

			LOGGER.warning(e.getLocalizedMessage());

		}

		LOGGER.exiting("toString", ret);
		return ret;

	}

	//----------------------------------------------------------------------------
	//  HeaderName用のアダプタメソッド
	//----------------------------------------------------------------------------
	public void set(final HeaderName key, final String value){

		this.set(key.toString(), value);

	}

	public void add(final HeaderName key, final String value){

		this.add(key.toString(), value);

	}

	public void remove(final HeaderName key){

		this.remove(key.toString());

	}

	public String get(final HeaderName key){

		return this.get(key.toString());

	}

	public boolean containsKey(final HeaderName key){

		return this.containsKey(key.toString());

	}

	public boolean containsValue(final HeaderName key, final String value){

		return this.containsValue(key.toString(), value);

	}

	//============================================================================
	//  private メソッド
	//============================================================================
	/**
	 * ストリームからヘッダ情報を読み取る．
	 * 引数で指定されたストリームからHttpリクエストのヘッダ情報を読み取る．
	 * また，このメソッドはストリームを閉じない．
	 *
	 * @param reader 解析するバッファリングされたストリーム
	 * @throws IOException ストリーム読み込み時にI/Oエラーが発生した場合
	 */
	private void readHeader(final BufferedReader reader) throws IOException{
		LOGGER.entering("readHeader", reader);
		assert reader != null;

		for(String line = reader.readLine(); line != null; line = reader.readLine()){

			// ヘッダ記述のシンタクスに合致するか
			final Matcher m = HEADER.matcher(line);
			if(m.matches()){

				final String key = m.group(1).toLowerCase();
				if(key != null){

					final String value = m.group(2);
					this.set(key, value);

				}

			}

		}

		LOGGER.exiting("readHeader");
	}

	/**
	 * コレクションに含まれるヘッダ情報をすべて追加する．
	 * 既に共通項目がヘッダに存在する場合は上書きする．
	 *
	 * @param elements このオブジェクトに追加するコレクション
	 */
	private void setAll(final Map<String, String> elements){
		LOGGER.entering("setAll", (Object)elements);
		assert elements != null;

		for(final String key : elements.keySet()){

			// なぜかConnectionが返すヘッダにはキーがnullのものが含まれる
			if(key != null){

				this.set(key.toLowerCase(), elements.get(key));

			}

		}

		LOGGER.exiting("setAll");
	}

}
