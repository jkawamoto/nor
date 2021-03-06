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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.regex.Matcher;

import nor.http.error.HttpException;
import nor.http.io.HeaderInputStream;
import nor.util.io.LimitedInputStream;
import nor.util.log.Logger;

/**
 * HTTP リクエストを表すクラス．
 * このクラスのインスタンス一つが，一つの HTTP リクエストに対応します．
 * また，HTTP レスポンスはこのクラスのインスタンスがなければ作成することができません．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 *
 */
public class HttpRequest extends HttpMessage{

	/**
	 * Request method
	 */
	private String method;

	/**
	 * Request path (URL)
	 */
	private String path;

	/**
	 * プロトコルバージョン
	 */
	private String version;

	/**
	 * Header object
	 */
	private final HttpHeader header;

	/**
	 * Logger
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpRequest.class);

	//====================================================================
	//	コンストラクタ
	//====================================================================
	public HttpRequest(final String method, final URL url){
		this(method, url, "");
	}

	public HttpRequest(final String method, final URL url, final String body){
		this(method, url, new ByteArrayInputStream(body.getBytes()), body.getBytes().length);
	}

	public HttpRequest(final String method, final URL url, final InputStream body){

		this.method = method;
		this.path = url.toString();
		this.version = Http.Version;

		this.header = new HttpHeader();
		this.setBody(body);

	}

	public HttpRequest(final String method, final URL url, final InputStream body, final long length){

		this.method = method;
		this.path = url.toString();
		this.version = Http.Version;

		this.header = new HttpHeader();
		this.header.set(HeaderName.ContentLength, Long.toString(length));

		this.setBody(new LimitedInputStream(body, length));

	}

	public HttpRequest(final Method method, final URL url){
		this(method.toString(), url);
	}

	public HttpRequest(final Method method, final URL url, final String body){
		this(method.toString(), url, body);
	}

	public HttpRequest(final Method method, final URL url, final InputStream body){
		this(method.toString(), url, body);
	}

	//====================================================================
	//	Private constructors
	//====================================================================
	/**
	 * ストリームからHttpリクエストを読み込む．
	 * このコンストラクタは引数で指定されたストリームを読み込み同じ内容の
	 * Httpリクエストオブジェクトを構築する．読み込みに使用したストリームは
	 * 閉じない．そのため，ストリームの終了処理は呼び出し側が行う必要がある．
	 *
	 * @param input 文字列のHttpリクエストが入っているストリーム
	 * @throws IOException ストリーム処理にエラーが起きた場合
	 */
	private HttpRequest(final InputStream input) throws IOException{
		LOGGER.entering("<init>", input);
		assert input != null;

		// ヘッドラインの読み取り
		final BufferedReader in = new BufferedReader(new InputStreamReader(new HeaderInputStream(input)));
		String buf;
		while((buf = in.readLine()) != null){

			final Matcher m = Http.RequestLinePattern.matcher(buf);
			if(m.matches()){

				this.method = m.group(1);
				this.path = m.group(2);
				this.version = m.group(3);

				break;

			}

		}

		// Set headers
		this.header = new HttpHeader(in);

		// Set body
		this.setBody(HttpMessage.decodeStream(input, this.header));


		LOGGER.exiting("<init>");
	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * メソッド名を取得する．
	 * このメソッドはメソッド名を文字列で返します.
	 *
	 * @return このHTTPリクエストのメソッド名
	 */
	public String getMethodString(){
		LOGGER.entering("getMethodString");

		final String ret = this.method;

		LOGGER.exiting("getMethodString", ret);
		return ret;

	}

	/**
	 * メソッドを取得する．
	 * このメソッドはメソッド名を Method 列挙型で返します.
	 *
	 * @return このHTTPリクエストのメソッド
	 */
	public Method getMethod(){
		LOGGER.entering("getMethod");

		Method ret = null;
		try{

			ret = Method.valueOf(this.method);

		}catch(final IllegalArgumentException e){

			LOGGER.warning("getMethod", e.getMessage());

		}

		LOGGER.exiting("getMethod", ret);
		return ret;

	}


	/**
	 * リクエストの要求パスを設定する．
	 *
	 * @param path この HTTP リクエストの新しい要求パス
	 */
	public void setPath(final String path){
		LOGGER.entering("setPath", path);

		this.path = path;

		LOGGER.exiting("setPath");
	}

	public void setVersion(final String version){

		this.version = version;

	}

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#toString()
	 */
	@Override
	public String toString(){
		LOGGER.entering("toString");

		final String ret = String.format("Request[method=%s, path=%s]\r\n%s", this.getMethod(), this.getPath(), this.header.toString());

		LOGGER.exiting("toString", ret);
		return ret.toString();

	}

	//--------------------------------------------------------------------
	//	レスポンスの作成
	//--------------------------------------------------------------------
	/**
	 * 入力ストリームを指定してレスポンスを作成する．
	 * 指定された入力ストリームをもとに，このリクエストに対するレスポンスを作成します．
	 *
	 * 入力ストリームには，レスポンスライン，ヘッダそしてボディのすべてが含まれている必要があります．
	 * メッセージボディのみを渡してレスポンスを作成したい場合は，
	 * {@link  createResponse(Status, InputStream)}メソッドを使用します．
	 *
	 * @param input レスポンスが含まれている入力ストリーム
	 * @return 作成されたレスポンス
	 * @throws HttpError 何らかのエラーが発生した場合
	 */
	public HttpResponse createResponse(final InputStream input) throws HttpException{
		LOGGER.entering("createResponse", input);
		assert input != null;

		final HttpResponse ret = new HttpResponse(this, input);

		LOGGER.exiting("createResponse", ret);
		return ret;
	}

	/**
	 * 指定されたステータスを持ち，指定された入力ストリームをメッセージボディとするレスポンスを作成します．
	 * メッセージボディのみからなる入力ストリームを用いてレスポンスを作成する場合は，
	 * このメソッドを使用してください．
	 *
	 * 入力ストリームには，転送コーディング及び内容コーディングのどちらも施されていてはいけません．
	 * あらかじめデコードした上でこのメソッドを呼び出してください．
	 *
	 * このメソッドにより作成されたレスポンスは，空のメッセージヘッダを持ちます．
	 * 返されたレスポンスに対して適切なヘッダ値を設定してください．
	 * 少なくとも，Content-Length か Transfer-Encoding のいずれかは指定する必要があります．
	 *
	 * @param status レスポンスのステータス
	 * @param body メッセージボディとなる入力ストリーム
	 * @return 作成されたレスポンス
	 */
	public HttpResponse createResponse(final Status status, final InputStream body){

		final HttpResponse ret = new HttpResponse(this, status, body);
		return ret;

	}

	public HttpResponse createResponse(final Status status, final InputStream body, final long length){

		final HttpResponse ret = new HttpResponse(this, status, body, length);
		return ret;

	}

	/**
	 * ステータス情報のみからレスポンスを作成する．
	 * 空のヘッダとメッセージボディからなるレスポンスを作成します．
	 *
	 * @param status レスポンスのステータス
	 * @return 作成されたレスポンス
	 * @throws HttpError 何らかのエラーが発生した場合
	 */
	public HttpResponse createResponse(final Status status){

		return this.createResponse(status, "");

	}

	/**
	 * 指定されたステータスと文字列メッセージボディを持つレスポンスを作成します．
	 * 現在のバージョンでは，バイト列に変換した時に Integer の範囲を超える文字列は扱えません．
	 * そのような場合は，{@link createResponse(Status, InputStream)}を使用してください．
	 *
	 * このメソッドを用いて作成したレスポンスでは，Content-Length ヘッダのみ設定されます．
	 *
	 * @param status レスポンスのステータス
	 * @param body メッセージボディとなる文字列
	 * @return 作成されたレスポンス
	 */
	public HttpResponse createResponse(final Status status, final String body){

		final byte[] b = body.getBytes();
		final HttpResponse ret = this.createResponse(status, new ByteArrayInputStream(b), b.length);

		return ret;

	}

	//--------------------------------------------------------------------
	//	HttpMessage のオーバーライド
	//--------------------------------------------------------------------
	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getVersion()
	 */
	@Override
	public String getVersion(){
		LOGGER.entering("getVersion");

		final String ret = this.version;

		LOGGER.exiting("getVersion", ret);
		return ret;

	}

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getPath()
	 */
	@Override
	public String getPath(){
		LOGGER.entering("getPath");

		final String ret = this.path;

		LOGGER.exiting("getPath", ret);
		return ret;

	}

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {
		LOGGER.entering("getHeadLine");

		final String ret = String.format(Http.RequestLineTemplate, this.method, this.path, this.version);

		LOGGER.exiting("getHeadLine", ret);
		return ret;
	}

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getHeader()
	 */
	@Override
	public HttpHeader getHeader() {

		return this.header;

	}

	//====================================================================
	//	public static メソッド
	//====================================================================
	/**
	 * 入力ストリームを指定してリクエストを作成する．
	 *
	 * @param input リクエストが含まれている入力ストリーム
	 * @param prefix 将来のために予約されています
	 * @return 作成されたリクエスト，不正なリクエストの場合は null
	 */
	public static HttpRequest create(final InputStream input){

		try{

			final HttpRequest ret = new HttpRequest(input);
			if(ret.getMethodString() == null){

				return null;

			}else{

				return ret;

			}

		}catch(final IOException e){

			LOGGER.catched(Level.FINE, "create", e);

			return null;

		}

	}

}
