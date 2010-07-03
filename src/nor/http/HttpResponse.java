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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import nor.http.error.HttpException;
import nor.http.error.InternalServerErrorException;
import nor.http.io.HeaderInputStream;
import nor.util.log.EasyLogger;

/**
 * HTTP レスポンスオブジェクト．
 * 一つのオブジェクトで一つのHttpレスポンスを表す．
 *
 * @author Junpei Kawamoto
 * @since 0.1
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

	/**
	 * Http ヘッダ．
	 */
	private final HttpHeader header;

	/**
	 * Http ボディ．
	 */
	private final HttpBody body;

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
	HttpResponse(final HttpRequest request, final InputStream input) throws HttpException{
		LOGGER.entering("<init>", request, input);
		assert request != null;
		assert input != null;

		try{

			// ヘッドラインの読み取り
			final BufferedReader in = new BufferedReader(new InputStreamReader(new HeaderInputStream(input)));
			String buf;
			while((buf = in.readLine()) != null){

				// ステータスコードの取得
				final Matcher m = Http.RESONSE_LINE_PATTERN.matcher(buf);
				if(m.matches()){

					this.version = m.group(1);
					this.code = Integer.parseInt(m.group(2));
					this.message = m.group(3);

					break;

				}

			}
			if(this.code == 0){

				throw new InternalServerErrorException();

			}

			// リクエストの保存
			this.request = request;

			// ヘッダの読み取り
			this.header = new HttpHeader(in);

			// ボディの読み取り
			if(Method.HEAD.equals(this.getRequest().getMethod())){

				// HEADリクエストへのレスポンスはメッセージボディを含んではならない
				this.getHeader().set(HeaderName.ContentLength, "0");

			}else if((100 <= this.code && this.code < 200) || this.code == 204 || this.code == 304){

				// 100番台，204, 304のレスポンスはメッセージボディを含んではならない
				this.getHeader().set(HeaderName.ContentLength, "0");

			}
			this.body = new HttpBody(input, this.header);


		}catch(final IOException e){

			throw new InternalServerErrorException();

		}

		LOGGER.exiting("<init>");
	}

	/**
	 *
	 * @param request
	 * @param status
	 * @param body 内容コーディング，転送コーディングともに解決済みの入力ストリーム
	 */
	HttpResponse(final HttpRequest request, final Status status, final InputStream body){

		this.request = request;
		this.code = status.getCode();
		this.message = status.getMessage();
		this.version = Http.VERSION;

		this.header = new HttpHeader();
		this.body = new HttpBody(body);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * このレスポンスの元となったリクエストを取得します．
	 *
	 * @return このレスポンスの元となったリクエスト
	 */
	public HttpRequest getRequest(){
		LOGGER.entering("getRequest");

		final HttpRequest ret = this.request;

		LOGGER.exiting("getRequest", ret);
		return ret;

	}

	/**
	 * レスポンスコードを取得します．
	 *
	 * @return レスポンスコード
	 */
	public int getCode(){
		LOGGER.entering("getCode");

		final int ret = this.code;

		LOGGER.exiting("getCode", ret);
		return ret;
	}

	/**
	 * レスポンスステータスを取得します．
	 *
	 * @return レスポンスステータス
	 */
	public Status getStatus(){
		LOGGER.entering("getStatus");

		final Status ret = Status.valueOf(this.getCode());

		LOGGER.exiting("getStatus", ret);
		return ret;
	}

	/**
	 * レスポンスメッセージを取得する．
	 *
	 * @return レスポンスメッセージ
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
	public String getPath() {

		return this.getRequest().getPath();

	}

	/* (非 Javadoc)
	 * @see nor.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {

		return String.format(Http.RESONSE_LINE_TEMPLATE, version, code, message);

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

}
