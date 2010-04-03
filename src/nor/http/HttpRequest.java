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
// $Id: HttpRequest.java 471 2010-04-03 10:25:20Z kawamoto $
package nor.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.io.HeaderInputStream;
import nor.util.log.EasyLogger;

/**
 * 一つのHttpリクエストを表すクラス．
 * このクラスのインスタンス一つが，一つのHttpリクエストに対応している．
 * このクラスは，テキストフォーマットのHttpリクエストを元に，それをオブジェクトに変換する．
 *
 * また，このクラス自体は不変オブジェクトである．しかし，ヘッダ及びボディに関しては，
 * getterで取得して内部を変更することは可能である．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HttpRequest extends HttpMessage{

	/**
	 * HTTPリクエストライン解析のための正規表現
	 */
	private static final Pattern Command = Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");

	private static final String HeadLine = "%s %s HTTP/%s";

	/**
	 * HTTPリクエストメソッド
	 */
	private String method;

	/**
	 * 要求パス
	 */
	private String path;

	/**
	 * プロトコルバージョン
	 */
	private String version;

	private final HttpHeader header;
	private final HttpBody body;


	/**
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpRequest.class);

	//====================================================================
	//	コンストラクタ
	//====================================================================
	/**
	 * ストリームからHttpリクエストを読み込む．
	 * このコンストラクタは引数で指定されたストリームを読み込み同じ内容の
	 * Httpリクエストオブジェクトを構築する．読み込みに使用したストリームは
	 * 閉じない．そのため，ストリームの終了処理は呼び出し側が行う必要がある．
	 *
	 * @param input 文字列のHttpリクエストが入っているストリーム
	 * @throws IOException
	 * @throws HttpError
	 * @throws SocketTimeoutException
	 * @throws InvalidRequest リクエストが不正な場合
	 * @throws IOException ストリーム処理にエラーが起きた場合
	 * @throws InvalidRequest
	 */
	private HttpRequest(final InputStream input, final String prefix) throws IOException{
		LOGGER.entering("<init>", input, prefix);
		assert input != null;

		// ヘッドラインの読み取り
		final BufferedReader in = new BufferedReader(new InputStreamReader(new HeaderInputStream(input)));
		String buf;
		while((buf = in.readLine()) != null){

			final Matcher m = Command.matcher(buf);
			if(m.matches()){

				this.method = m.group(1);
				this.path = prefix + m.group(2);
				this.version = m.group(3);

				break;

			}

		}

		// ヘッダの読み取り
		this.header = new HttpHeader(this, in);

		// ボディの読み取り
		this.body = this.readBody(input);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * このHTTPリクエストのメソッド名を返す．
	 *
	 * @return このHTTPリクエストのメソッド名
	 */
	public String getMethod(){
		LOGGER.entering("getMethod");

		final String ret = this.method;

		LOGGER.exiting("getMethod", ret);
		return ret;

	}


	/**
	 * このHTTPリクエストにおける要求パスを設定する．
	 *
	 * @param path このHTTPリクエストの新しい要求パス
	 */
	public void setPath(final String path){
		LOGGER.entering("setPath", path);

		this.path = path;

		LOGGER.exiting("setPath");
	}

	//	/**
	//	 * このHTTPリクエストに含まれるクエリを返す．
	//	 *
	//	 * @return クエリオブジェクト
	//	 */
	//	public Query getQuery(){
	//		LOGGER.entering("getQuery");
	//
	//		Query ret = null;
	//		try{
	//
	//			final URL url = new URL(this.getPath());
	//			final String q = url.getQuery();
	//
	//			if(q != null){
	//
	//				ret = new Query(q);
	//
	//			}else{
	//
	//				ret = new Query();
	//
	//			}
	//
	//		}catch(MalformedURLException e){
	//
	//			LOGGER.warning("URL解析不能：" + this.getPath());
	//			ret = new Query();
	//
	//		}
	//
	//		LOGGER.exiting("getQuery", ret);
	//		return ret;
	//
	//	}
	//
	//	/**
	//	 * このHTTPリクエストに新しいクエリを設定する．
	//	 * 今まで設定されていたクエリは破棄される．
	//	 *
	//	 * @param query 新たに設定するクエリ
	//	 */
	//	public void setQuery(final Query query){
	//		LOGGER.entering("setQuery", query);
	//		assert query != null;
	//
	//		try{
	//
	//			final URL url = new URL(this.getPath());
	//			final String ref = url.getRef();
	//
	//			if(ref != null){
	//
	//				this.path = String.format("%s://%s%s?%s#%s", url.getProtocol(), url.getHost(), url.getPath(), query.toString(), ref);
	//
	//			}else{
	//
	//				this.path = String.format("%s://%s%s?%s", url.getProtocol(), url.getHost(), url.getPath(), query.toString());
	//
	//			}
	//
	//		} catch (MalformedURLException e) {
	//
	//			LOGGER.warning(e.getLocalizedMessage());
	//
	//		}
	//
	//		LOGGER.exiting("setQuery");
	//	}


	/**
	 * レスポンスオブジェクトを作成する．
	 *
	 * @param input レスポンスを含むストリーム
	 * @throws HttpError
	 */
	public HttpResponse createResponse(final InputStream input) throws HttpError{
		LOGGER.entering("createResponse", input);
		assert input != null;

		final HttpResponse ret = new HttpResponse(this, input);

		LOGGER.exiting("createResponse", ret);
		return ret;
	}

	public HttpResponse createResponse(final HttpURLConnection con){
		LOGGER.entering("createResponse", con);
		assert con != null;

		HttpResponse ret;

		// ヘッダの登録
		final HttpHeader header = this.getHeader();
		for(final String key : header.keySet()){

			con.addRequestProperty(key, header.get(key));

		}

		try {

			try{

				con.setConnectTimeout(60000);

				// ボディがある場合は送信
				if(header.containsKey(HeaderName.ContentLength)){

					final int length = Integer.parseInt(header.get(HeaderName.ContentLength));
					con.setFixedLengthStreamingMode(length);
					con.setDoOutput(true);
					con.connect();

					this.getBody().writeOut(new BufferedOutputStream(con.getOutputStream()));

				}else{

					con.connect();

				}

				// レスポンスの作成
				ret = new HttpResponse(this, con);

			} catch (final HttpError e) {

				final InputStream err = con.getErrorStream();
				if(err != null){

					ret = new HttpResponse(this, con, con.getErrorStream());

				}else{

					throw e;

				}

			}

		}catch(final IOException e){

			final StringWriter body = new StringWriter();
			e.printStackTrace(new PrintWriter(body));
			ret = ErrorResponseBuilder.create(this, ErrorStatus.InternalServerError, body.toString());

		}catch(final HttpError e){

			final StringWriter body = new StringWriter();
			e.printStackTrace(new PrintWriter(body));
			ret = ErrorResponseBuilder.create(this, e.getStatus(), body.toString());

		}


		LOGGER.exiting("createResponse", ret);
		return ret;
	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#toString()
	 */
	public String toString(){
		LOGGER.entering("toString");

		final String ret = String.format("Request[method=%s, path=%s]", this.getMethod(), this.getPath());

//		final StringBuilder ret = new StringBuilder();
//
//		ret.append(this.getHeadLine());
//		ret.append("\n");
//		ret.append(this.getHeader().toString());

		LOGGER.exiting("toString", ret);
		return ret.toString();

	}

	//--------------------------------------------------------------------
	//	HttpMessage のオーバーライド
	//--------------------------------------------------------------------
	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getVersion()
	 */
	@Override
	public String getVersion(){
		LOGGER.entering("getVersion");

		final String ret = this.version;

		LOGGER.exiting("getVersion", ret);
		return ret;

	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getPath()
	 */
	@Override
	public String getPath(){
		LOGGER.entering("getPath");

		final String ret = this.path;

		LOGGER.exiting("getPath", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {
		LOGGER.entering("getHeadLine");

		final String ret = String.format(HttpRequest.HeadLine, this.method, this.path, this.version);

		LOGGER.exiting("getHeadLine", ret);
		return ret;
	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getHeader()
	 */
	@Override
	public HttpHeader getHeader() {

		return this.header;

	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getBody()
	 */
	@Override
	public HttpBody getBody() {

		return this.body;

	}

	//====================================================================
	//	public static メソッド
	//====================================================================
	public static HttpRequest create(final InputStream input, final String prefix){

		try{

			final HttpRequest ret = new HttpRequest(input, prefix);
			if(ret.getMethod() == null){

				return null;

			}else{

				return ret;

			}

		}catch(final IOException e){

			return null;

		}

	}

	//====================================================================
	//	private メソッド
	//====================================================================
	private HttpBody readBody(final InputStream input) throws IOException{

		if(Method.TRACE.equalsIgnoreCase(this.getMethod())){

			this.getHeader().set(HeaderName.ContentLength, "0");

		}

		return new HttpBody(this, input);


		/*
		 * Max-Forwards リクエストヘッダフィールドは、リクエスト連鎖中で特定のプロクシを目標に使われるであろう。プロクシは、転送が許可されたリクエストのための absoluteURI と共に OPTIONS リクエストを受けとった時には、Max-Forwards フィールドをチェックしなければならない。もし、Max-Forwards フィールドの値がゼロ ("0") ならば、プロクシはそのメッセージを転送してはならない。その代わりに、プロクシ自身のコミュニケーションオプションを返すべきである。もし、Max-Forwards フィールドの値がゼロより大きな整数値ならば、プロクシはリクエストを転送する際にその値を一つ減らさなければならない。リクエストの中に Max-Forwards フィールドが存在していなければ、転送するリクエストに Max-Forwards フィールドを含めてはならない。
		 *
		 */


		//	    * GET
		//	    * HEAD
		//	    * POST
		//	    * PUT
		//	    * DELETE
		//	    * OPTIONS
		//	    * TRACE
		//	    * CONNECT
		//	    * PATCH
		//	    * LINK, UNLINK

		// テキストボディ -> ノーマル or チャンク, バイナリボディ -> ノーマル or チャンク


		// TODO: コネクトについてはまた考える
		//		if("CONNECT".equalsIgnoreCase(this.getMethod())){
		//
		//			return new BinaryBody(this, input);
		//
		//		}else{
		//
		//			return BodyBuilder.getInstance().create(this, input);
		//
		//		}

	}

}
