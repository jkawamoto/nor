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

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTPリクエストを表すクラス．
 * このクラスのインスタンス一つが，一つのHTTPリクエストに対応している．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class Request extends Message{

	/**
	 * HTTPリクエストメソッド
	 */
	private String _method;

	/**
	 * 要求パス
	 */
	private String _path;

	/**
	 * HTTPリクエストライン解析のための正規表現
	 */
	private static final Pattern Command = Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");

	/**
	 * HTTPリクエストライン出力のためのフォーマット
	 */
	private static final String HeadLine = "%s %s HTTP/%s";

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Request.class.getName());

	//====================================================================
	//	コンストラクタ
	//====================================================================
	/**
	 * 入力ストリームからHTTPリクエストを読み込み，Requestを作成する．
	 *
	 * @param input HTTPリクエストが入っている入力ストリーム
	 * @throws IOException ストリーム処理にエラーが起きた場合
	 */
	public Request(final InputStream input) throws IOException{
		super(input);

		// 読み込んだリクエストに関する情報を出力
		LOGGER.info(this.toString());

	}

	/**
	 * 入力ストリームからHTTPリクエストを読み込み，Requestを作成する．
	 * prefixで指定した文字列を要求パスのプレフィクスとして用いる．
	 *
	 * @param input HTTPリクエストが入っている入力ストリーム
	 * @param prefix 要求パスへのプレフィクス
	 * @throws IOException ストリーム処理にエラーが起きた場合
	 */
	public Request(final InputStream input, final String prefix) throws IOException{
		super(input);

		assert prefix != null;
		this._path = prefix + this._path;

		// 読み込んだリクエストに関する情報を出力
		LOGGER.info(this.toString());

	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * メソッドの取得．
	 *
	 * @return このHTTPリクエストのメソッド名
	 */
	public String getMethod(){

		return this._method;

	}

	/**
	 * 要求パスの取得．
	 *
	 * @return このHTTPリクエストの要求パス
	 */
	public String getPath(){

		final String ret = this._path;

		return ret;

	}

	/**
	 * 要求パスを設定する．
	 *
	 * @param path このHTTPリクエストの新しい要求パス
	 */
	public void setPath(final String path){

		this._path = path;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {

		return String.format(HeadLine, this._method, this._path, this.getVersion());

	}

	/**
	 * レスポンスを作成する．
	 * このリクエストに対応するHTTPレスポンスを作成する．
	 *
	 * @param input レスポンスデータを含む入力ストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 * @throws BadMessageException 無効なレスポンスの場合
	 */
	public Response createResponse(final InputStream input) throws IOException, BadMessageException{
		assert input != null;

		return new Response(this, input);

	}

	//====================================================================
	//	protected メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#readHeadLine(java.lang.String)
	 */
	@Override
	protected boolean readHeadLine(String line) {

		final Matcher m = Command.matcher(line);
		if(m.matches()){

			this._method = m.group(1);
			this._path = m.group(2);
			this.setVersion(m.group(3));

			return true;

		}

		return false;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#readBody(java.io.InputStream)
	 */
	@Override
	protected Body readBody(final InputStream input) throws IOException{

		// ボディがあれば読み取る

		// TODO: メソッド毎にどのクラスを用いるか判定
		// GET
		// HEAD
		// POST
		// PUT
		// DELETE
		// OPTIONS
		// TRACE
		// CONNECT
		// PATCH
		// LINK, UNLINK

		// TODO: Length Requierdに対処する＞新しいボディクラス
		if("POST".equals(this.getMethod())){

			if("text".equalsIgnoreCase(this.getHeader().getContentType().getMIMEType()) || "xhtml+xml".equalsIgnoreCase(this.getHeader().getContentType().getMIMESubtype())){

				return new TextBody(this, input);


			}else{

				return new TextBody(this, input);

			}

		}

		return new EmptyBody(this);

	}


}
