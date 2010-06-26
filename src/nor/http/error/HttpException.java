/*
 *  Copyright (C) 2009, 2010 KAWAMOTO Junpei
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
package nor.http.error;

import java.io.ByteArrayInputStream;

import nor.http.HeaderName;
import nor.http.Http;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;

/**
 *	HTTP/1.1 プロトコルにおけるエラーを表す例外クラス．
 *	開発者は，このクラスとそのサブクラスを用いて簡単にエラーレスポンスを返すことができます．
 *	例えば，404 Not Found レスポンスを返す場合，コードは次のようになります．
 *	<pre>
 *	  throw new HttpException(Status.NotFound);
 *	</pre>
 *	ただし，404 Not Found レスポンスのように頻繁に利用されるレスポンスについては，
 *	サブクラスを用いたショートカットが用意されています．
 *	<pre>
 *	  throw new NotFoundException();
 *	</pre>
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public class HttpException extends Exception{

	private static final long serialVersionUID = 1L;

	private final Status status;
	private final String message;
	protected final Throwable cause;

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * エラーステータスを指定して HttpException を作成する．
	 *
	 * @param status エラー内容を表すステータス
	 */
	public HttpException(final Status status){
		this(status, null, null);
	}

	/**
	 * エラーステータスとメッセージを指定して HttpException を作成する．
	 *
	 * @param status エラー内容を表すステータス
	 * @param message 追加の文字列情報
	 */
	public HttpException(final Status status, final String message){
		this(status, message, null);
	}

	/**
	 * エラーステータスとエラーの原因となった例外オブジェクトを指定して HttpException を作成する．
	 *
	 * @param status エラー内容を表すステータス
	 * @param cause エラーの例外となった Throwable オブジェクト
	 */
	public HttpException(final Status status, final Throwable cause){
		this(status, null, cause);
	}

	/**
	 * エラーステータスと文字列情報，原因オブジェクトを指定して HttpException を作成する．
	 *
	 * @param status エラー内容を表すステータス
	 * @param message 追加の文字列情報
	 * @param cause エラーの例外となった Throwable オブジェクト
	 */
	public HttpException(final Status status, final String message, final Throwable cause){

		this.status = status;
		this.message = message;
		this.cause = cause;

	}

	//============================================================================
	//  Public methods
	//============================================================================
	/**
	 * レスポンスの作成．
	 * この例外オブジェクトからレスポンスを作成します．
	 *
	 * @param request レスポンス元のリクエスト
	 * @return エラーレスポンス
	 */
	public HttpResponse createResponse(final HttpRequest request){

		// TODO: Throwableからスタックトレースをメッセージに追加する
		final HttpResponse ret = request.createResponse(Status.valueOf(this.status.getCode()));

		final HttpHeader header = ret.getHeader();

		if(this.message != null){

			final byte[] msg = this.message.getBytes();

			header.add(HeaderName.ContentLength, Integer.toString(msg.length));
			header.add(HeaderName.Server, Http.SERVERNAME);

			ret.getBody().setStream(new ByteArrayInputStream(msg));

		}else{

			header.add(HeaderName.ContentLength, "0");
			header.add(HeaderName.Server, Http.SERVERNAME);

		}

		assert ret != null;
		return ret;

	}

	//============================================================================
	//  Public static methods
	//============================================================================
	public static HttpResponse createResponse(final HttpRequest request, final Status status){

		final HttpException e = new HttpException(status);
		return e.createResponse(request);

	}

	public static HttpResponse createResponse(final HttpRequest request, final Status status, final String message){

		final HttpException e = new HttpException(status, message);
		return e.createResponse(request);

	}

	public static HttpResponse createResponse(final HttpRequest request, final Status status, final Throwable cause){

		final HttpException e = new HttpException(status, cause);
		return e.createResponse(request);

	}

	public static HttpResponse createResponse(final HttpRequest request, final Status status, final String message, final Throwable cause){

		final HttpException e = new HttpException(status, message, cause);
		return e.createResponse(request);

	}

}
