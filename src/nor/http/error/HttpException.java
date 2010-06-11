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
package nor.http.error;

import nor.http.HeaderName;
import nor.http.Http;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;

/**
 * @author KAWAMOTO Junpei
 *
 */
public class HttpException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected final int code;
	protected final String reason;

	public HttpException(final int code, final String reason){

		this.code = code;
		this.reason = reason;

	}

	public HttpResponse createResponse(final HttpRequest request){

		return CreateResponse(request, this);

	}


	/**
	 * 例外インスタンスに対応するレスポンスオブジェクトを生成する．
	 *
	 * @param request エラーの原因となったリクエスト
	 * @param exception 例外インスタンス
	 * @return 生成されたエラーレスポンス
	 */
	public static HttpResponse CreateResponse(final HttpRequest request, final HttpException exception){
		assert request != null;
		assert exception != null;

		final HttpResponse ret = request.createResponse(Status.valueOf(exception.code));

		final HttpHeader header = ret.getHeader();
		header.add(HeaderName.ContentLength, "0");
		header.add(HeaderName.Server, Http.SERVERNAME);

		assert ret != null;
		return ret;

	}


}
