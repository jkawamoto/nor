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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.log.Logger;


/**
 * HTTP ヘッダを表すクラス．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 *
 */
public class HttpHeader{

	/**
	 * ヘッダ解析用の正規表現
	 */
	private static final Pattern HEADER = Pattern.compile("^([^:]+):\\s*(.*)$");

	/**
	 * ヘッダの要素コレクション
	 */
	private final Map<String, String> elements = new HashMap<String, String>();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpHeader.class);

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * 空のHTTPヘッダオブジェクトを作成する．
	 *
	 */
	HttpHeader(){
		LOGGER.entering("<init>");

		LOGGER.exiting("<init>");
	}

	/**
	 * 与えられたストリームを解析してヘッダオブジェクトを構築する．
	 *
	 * @param parent
	 * @param reader ヘッダ情報を読み取るストリーム
	 * @throws IOException ストリーム処理中にI/Oエラーが発生した場合
	 */
	HttpHeader(final BufferedReader reader) throws IOException{
		LOGGER.entering("<init>", reader);
		assert reader != null;

		for(String line = reader.readLine(); line != null; line = reader.readLine()){

			if(line.length() != 0){

				// ヘッダ記述のシンタクスに合致するか
				final Matcher m = HEADER.matcher(line);
				if(m.matches()){

					final String key = m.group(1).toLowerCase();
					if(key != null){

						final String value = m.group(2);
						this.add(key, value);

					}

				}else{

					// TODO:
					LOGGER.fine("<init>", line);

				}

			}

		}

		LOGGER.exiting("<init>");
	}


	/**
	 * 引数で指定されたコレクションを持つHTTPヘッダオブジェクトを作成する．
	 * オブジェクトはコレクションのソフトコピーを所持する．
	 *
	 * @param parent
	 * @param elements このオブジェクトに持たせるヘッダ要素
	 */
	HttpHeader(final Map<String, String> elements){
		LOGGER.entering("<init>", new Object[]{elements});
		assert elements != null;

		for(final String key : elements.keySet()){

			// なぜかConnectionが返すヘッダにはキーがnullのものが含まれる
			if(key != null){

				this.add(key.toLowerCase(), elements.get(key));

			}

		}

		LOGGER.exiting("<init>");
	}

	//============================================================================
	//  Public methods
	//============================================================================
	/**
	 * ヘッダを設定する．
	 * すでに同じキーを持つヘッダが存在する場合は上書きされます．
	 *
	 * @param key 設定するヘッダのキー
	 * @param value 設定するする値
	 */
	public void set(final String key, final String value){
		LOGGER.entering("set", key, value);
		assert key != null;
		assert value != null;

		final String skey = key.toLowerCase();
		if(this.elements.containsKey(skey)){

			this.elements.remove(skey);

		}
		this.elements.put(skey, value);

		LOGGER.exiting("set");
	}


	/**
	 * ヘッダに値を追加する．
	 * すでに同じキーを持つヘッダが存在する場合は，値が追加されます．
	 *
	 * @param key 追加するkey
	 * @param value 追加する値
	 */
	public void add(final String key, final String value){
		LOGGER.entering("add", key, value);

		final String skey = key.toLowerCase();
		if(this.elements.containsKey(skey)){

			if(HeaderName.SetCookie.equals(skey)){

				final String nvalue = String.format("%s\n  %s", this.elements.get(skey), value);
				this.elements.remove(skey);
				this.elements.put(skey, nvalue);

			}else{

				final String nvalue = String.format("%s, %s", this.elements.get(skey), value);
				this.elements.remove(skey);
				this.elements.put(skey, nvalue);

			}

		}else{

			this.elements.put(skey, value);

		}

		LOGGER.exiting("add");
	}


	/**
	 * 指定されたキーに関連付けられている値をすべて削除します．
	 *
	 * @param key 削除するヘッダキー
	 */
	public void remove(final String key){
		LOGGER.entering("remove", key);
		assert key != null;

		final String skey = key.toLowerCase();
		this.elements.remove(skey);

		LOGGER.exiting("remove");

	}


	/**
	 * すべてのヘッダを削除します．
	 *
	 */
	public void clear(){
		LOGGER.entering("clear");

		this.elements.clear();

		LOGGER.exiting("clear");
	}


	/**
	 * 指定したキーに関連付けられた値を取得します．
	 *
	 * @param key 値を取得するヘッダのキー
	 * @return キーに関連付けられている値
	 */
	public String get(final String key){
		LOGGER.entering("getValue", key);
		assert key != null;

		final String skey = key.toLowerCase();
		final String ret = this.elements.get(skey);

		LOGGER.exiting("getValues", ret);
		return ret;
	}

	/**
	 * 指定したキーに関連付けられた値を取得します．
	 *
	 * @param key 値を取得するヘッダのキー
	 * @return キーに関連付けられている値
	 */
	public boolean containsKey(final String key){
		LOGGER.entering("containsKey", key);
		assert key != null;

		final String skey = key.toLowerCase();
		final boolean ret = this.elements.containsKey(skey);

		LOGGER.exiting("containsKey", ret);
		return ret;
	}

	/**
	 * 指定したキーに指定した値が含まれているか調べます．
	 *
	 * @param key 調査するキー
	 * @param value 調査する値
	 * @return キーに値が含まれている場合 true
	 */
	public boolean containsValue(final String key, final String value){

		final String skey = key.toLowerCase();
		final String v = this.get(skey);
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
	 * このヘッダに登録されているキーの集合を取得する．
	 *
	 * @return このヘッダオブジェクトに登録されているキーの集合
	 */
	public Set<String> keySet(){
		LOGGER.entering("keySet");

		final Set<String> ret = this.elements.keySet();

		LOGGER.exiting("keySet", ret);
		return ret;

	}


	/**
	 * ヘッダに登録されているキーの数を取得する．
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
	 * このメソッドは書き出し先のストリームを閉じないので，呼び出し側で処理する必要があります．
	 *
	 * @param writer 書き出し先のストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	public void output(final BufferedWriter writer) throws IOException{
		LOGGER.entering("output", writer);
		assert writer != null;

		for(final String key : this.elements.keySet()){

			if(HeaderName.SetCookie.equals(key)){

				for(final String v : this.get(key).split("\n")){

					writer.append(key);
					writer.append(": ");
					writer.append(v.trim());
					writer.append("\r\n");

					LOGGER.finer("output", "Write[{0}: {1}]", key, v);

				}

			}else{

				writer.append(key);
				writer.append(": ");
				writer.append(this.get(key));
				writer.append("\r\n");

				LOGGER.finer("output", "Write[{0}: {1}]", key, this.get(key));

			}

		}

		writer.flush();

		LOGGER.exiting("output");

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

			this.output(new BufferedWriter(buffer));
			ret = buffer.toString();

		} catch (final IOException e) {

			LOGGER.warning("toString", e.toString());
			LOGGER.catched(Level.FINER, "toString", e);

		}

		LOGGER.exiting("toString", ret);
		return ret;

	}

	//----------------------------------------------------------------------------
	//  HeaderName用のアダプタメソッド
	//----------------------------------------------------------------------------
	/**
	 * ヘッダを設定する．
	 * すでに同じキーを持つヘッダが存在する場合は上書きされます．
	 *
	 * @param key 設定するヘッダのキー
	 * @param value 設定するする値
	 */
	public void set(final HeaderName key, final String value){

		this.set(key.toString(), value);

	}


	/**
	 * ヘッダに値を追加する．
	 * すでに同じキーを持つヘッダが存在する場合は，値が追加されます．
	 *
	 * @param key 追加するkey
	 * @param value 追加する値
	 */
	public void add(final HeaderName key, final String value){

		this.add(key.toString(), value);

	}


	/**
	 * 指定されたキーに関連付けられている値をすべて削除します．
	 *
	 * @param key 削除するヘッダキー
	 */
	public void remove(final HeaderName key){

		this.remove(key.toString());

	}


	/**
	 * 指定したキーに関連付けられた値を取得します．
	 *
	 * @param key 値を取得するヘッダのキー
	 * @return キーに関連付けられている値
	 */
	public String get(final HeaderName key){

		return this.get(key.toString());

	}


	/**
	 * 指定したキーに関連付けられた値を取得します．
	 *
	 * @param key 値を取得するヘッダのキー
	 * @return キーに関連付けられている値
	 */
	public boolean containsKey(final HeaderName key){

		return this.containsKey(key.toString());

	}


	/**
	 * 指定したキーに指定した値が含まれているか調べます．
	 *
	 * @param key 調査するキー
	 * @param value 調査する値
	 * @return キーに値が含まれている場合 true
	 */
	public boolean containsValue(final HeaderName key, final String value){

		return this.containsValue(key.toString(), value);

	}

}
