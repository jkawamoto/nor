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
package nor.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.io.HeaderInputStream;
import nor.util.log.EasyLogger;

/**
 * Httpレスポンスオブジェクト．
 * 一つのオブジェクトで一つのHttpレスポンスを表す．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HttpResponse extends HttpMessage{

	// ロガー
	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpResponse.class);

	/**
	 * このレスポンスの基になった要求
	 */
	private final HttpRequest request;

	/**
	 * レスポンス情報
	 */
	private int code;
	private String message;
	private String version;

	private final HttpHeader header;
	private final HttpBody body;


	/**
	 *
	 */
	private static final Pattern StatusLine = Pattern.compile("^HTTP/(\\S{3})\\s+(\\d{3})\\s*(.*)$");

	private static final String HeadLine = "HTTP/%s %d %s";

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * レスポンスオブジェクトを作成する．
	 *
	 * @param request このレスポンスの元になったHTTPリクエスト
	 * @param input レスポンスを含むストリーム
	 * @throws HttpError
	 * @throws IOException I/Oエラーが発生した場合
	 * @throws InvalidRequest 無効なレスポンスの場合
	 */
	HttpResponse(final HttpRequest request, final InputStream input) throws HttpError{
		LOGGER.entering("<init>", request, input);
		assert request != null;
		assert input != null;

		try{

			// ヘッドラインの読み取り
			final BufferedReader in = new BufferedReader(new InputStreamReader(new HeaderInputStream(input)));
			String buf;
			while((buf = in.readLine()) != null){

				// ステータスコードの取得
				final Matcher m = StatusLine.matcher(buf);
				if(m.matches()){

					this.version = m.group(1);
					this.code = Integer.parseInt(m.group(2));
					this.message = m.group(3);

					break;

				}

			}
			if(this.code == 0){

				throw new HttpError(Status.InternalServerError);

			}

			// リクエストの保存
			this.request = request;

			// ヘッダの読み取り
			this.header = new HttpHeader(this, in);

			// ボディの読み取り
			this.body = this.readBody(input);

		}catch(final IOException e){

			throw new HttpError(Status.InternalServerError, e);

		}

		LOGGER.exiting("<init>");
	}

	HttpResponse(final HttpRequest request, final Status status){

		this(request, status, null);

	}

	HttpResponse(final HttpRequest request, final Status status, final InputStream input){

		this.request = request;
		this.code = status.getCode();
		this.message = status.getMessage();
		this.version = HttpVersion.VERSION;

		this.header = new HttpHeader(this);

		HttpBody b = null;
		try {

			if(input != null){

				b = this.readBody(input);

			}else{

				b = this.readBody(new ByteArrayInputStream(new byte[0]));

			}

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		this.body = b;


	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * レスポンスの元となったリクエストを返す．
	 *
	 * @return リクエスト
	 */
	public HttpRequest getRequest(){
		LOGGER.entering("getRequest");

		final HttpRequest ret = this.request;

		LOGGER.exiting("getRequest", ret);
		return ret;

	}

	/**
	 * HTTPレスポンスコードを返す．
	 *
	 * @return HTTPレスポンスコード
	 */
	public int getCode(){
		LOGGER.entering("getCode");

		final int ret = this.code;

		LOGGER.exiting("getCode", ret);
		return ret;

	}

	/**
	 * HTTPレスポンスメッセージを返す．
	 *
	 * @return HTTPレスポンスメッセージ
	 */
	public String getMessage(){
		LOGGER.entering("getMessage");

		final String ret = this.message;

		LOGGER.exiting("getMessage", ret);
		return ret;

	}

	//--------------------------------------------------------------------
	//	HttpMessage インタフェースの実装
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
	public String getPath() {

		return this.getRequest().getPath();

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {

		return String.format(HttpResponse.HeadLine, version, code, message);

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
	//	private メソッド
	//====================================================================
	private HttpBody readBody(final InputStream input) throws IOException {

		// HEADリクエストへのレスポンスはメッセージボディを含んではならない
		if(Method.HEAD.equalsIgnoreCase(this.getRequest().getMethod())){

			this.getHeader().set(HeaderName.ContentLength, "0");

		}

		// 100番台，204, 304のレスポンスはメッセージボディを含んではならない
		if((100 <= this.code && this.code < 200) || this.code == 204 || this.code == 304){

			this.getHeader().set(HeaderName.ContentLength, "0");

		}

		return new HttpBody(this, input);

	}

}
