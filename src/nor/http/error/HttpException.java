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

import java.io.ByteArrayInputStream;
import java.io.IOException;

import nor.http.HttpError;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

/**
 * @author KAWAMOTO Junpei
 *
 */
public class HttpException extends RuntimeException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private static final String StatusLine = "HTTP/1.1 %d %s";

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

		HttpResponse ret = null;

		final StringBuilder header = new StringBuilder();
		header.append(String.format(StatusLine, exception.code , exception.reason));
		header.append("\n");
		header.append("Content-Type: text/html; charset=utf-8\n");
		header.append("Content-Length: 0\n");
		header.append("Server: nor\n");
		header.append("\n");


		try {

			ret = request.createResponse(new ByteArrayInputStream(header.toString().getBytes()));

		} catch (HttpError e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}


		assert ret != null;
		return ret;

	}


}
