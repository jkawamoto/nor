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
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import nor.http.error.HttpException;
import nor.http.error.InternalServerErrorException;
import nor.http.io.HeaderInputStream;
import nor.util.log.EasyLogger;

/**
 * HTTP リクエストを表すクラス．
 * このクラスのインスタンス一つが，一つの HTTP リクエストに対応します．
 * また，HTTP レスポンスはこのクラスのインスタンスがなければ作成することができません．
 *
 * @author Junpei Kawamoto
 *
 */
public class HttpRequest extends HttpMessage{

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

			final Matcher m = Http.REQUEST_LINE_PATTERN.matcher(buf);
			if(m.matches()){

				this.method = m.group(1);
				this.path = prefix + m.group(2);
				this.version = m.group(3);

				break;

			}

		}

		// ヘッダの読み取り
		this.header = new HttpHeader(in);

		// ボディの読み取り
		this.body = new HttpBody(input, this.header);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * メソッド名を取得する．
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
	 * リクエストの要求パスを設定する．
	 *
	 * @param path この HTTP リクエストの新しい要求パス
	 */
	public void setPath(final String path){
		LOGGER.entering("setPath", path);

		this.path = path;

		LOGGER.exiting("setPath");
	}


//	@Override
//	public String toString(){
//		LOGGER.entering("toString");
//
//		final String ret = String.format("Request[method=%s, path=%s]", this.getMethod(), this.getPath());
//
//		//		final StringBuilder ret = new StringBuilder();
//		//
//		//		ret.append(this.getHeadLine());
//		//		ret.append("\n");
//		//		ret.append(this.getHeader().toString());
//
//		LOGGER.exiting("toString", ret);
//		return ret.toString();
//
//	}

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
	 * HttpURLConnection のインスタンスを指定してレスポンスを作成する．
	 * 指定された HttpURLConnection インスタンスからレスポンスを作成します．
	 *
	 * @param con HttpURLConnection インスタンス
	 * @return 作成されたレスポンス
	 * @throws HttpError 何らかのエラーが発生した場合
	 */
	public HttpResponse createResponse(final HttpURLConnection con) throws HttpException{
		LOGGER.entering("createResponse", con);
		assert con != null;


		// リクエストの処理
		// リクエストヘッダの登録
		final HttpHeader header = this.getHeader();
		for(final String key : header.keySet()){

			con.addRequestProperty(key, header.get(key));

		}

		con.setConnectTimeout(60000);

		try{

			// ボディがある場合は送信
			if(header.containsKey(HeaderName.ContentLength)){

				final int length = Integer.parseInt(header.get(HeaderName.ContentLength));
				con.setFixedLengthStreamingMode(length);
				con.setDoOutput(true);
				con.connect();

				this.getBody().output(con.getOutputStream(), this.header);

			}else if(header.containsKey(HeaderName.TransferEncoding)){

				con.setDoOutput(true);
				con.connect();

				this.getBody().output(con.getOutputStream(), this.header);

			}else{

				con.connect();

			}

		}catch(final IOException e){

			throw new InternalServerErrorException(e);

		}


		// レスポンスの作成
		HttpResponse ret;
		try {

			final int code = con.getResponseCode();
			InputStream resStream = null;
			if(code < 400){

				resStream = con.getInputStream();

				// 内容エンコーディングの解決
				final String encode = con.getHeaderField(HeaderName.ContentEncoding.toString());
				if(encode != null){

					if(Http.GZIP.equalsIgnoreCase(encode)){

						resStream = new GZIPInputStream(resStream);

					}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

						resStream = new DeflaterInputStream(resStream);

					}

				}

			}else{

				resStream = con.getErrorStream();

			}
			ret = new HttpResponse(this, Status.valueOf(code), resStream);


			// ヘッダの登録
			final HttpHeader resHeader = ret.getHeader();
			final Map<String, List<String>> fields = con.getHeaderFields();
			for(final String key : fields.keySet()){

				if(key != null){

					for(final String value : fields.get(key)){

						resHeader.add(key, value);

					}

				}

			}

		}catch(final IOException e){

			throw new InternalServerErrorException(e);

		}


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
		final HttpResponse ret = this.createResponse(status, new ByteArrayInputStream(b));

		final HttpHeader header = ret.getHeader();
		header.set(HeaderName.ContentLength, Integer.toString(b.length));

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

		final String ret = String.format(Http.REQUEST_LINE_TEMPLATE, this.method, this.path, this.version);

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

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getBody()
	 */
	@Override
	public HttpBody getBody() {

		return this.body;

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
	public static HttpRequest create(final InputStream input, final String prefix){

		try{

			final HttpRequest ret = new HttpRequest(input, prefix);
			if(ret.getMethod() == null){

				return null;

			}else{

				return ret;

			}

		}catch(final IOException e){

			e.printStackTrace();
			return null;

		}

	}

}
