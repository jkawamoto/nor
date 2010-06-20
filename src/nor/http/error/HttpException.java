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
public abstract class HttpException extends Exception{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected final Status status;
	protected final String message;
	protected final Throwable cause;

	public HttpException(final Status status){
		this(status, null, null);
	}

	public HttpException(final Status status, final String message){
		this(status, message, null);
	}


	public HttpException(final Status status, final Throwable cause){
		this(status, null, cause);
	}

	public HttpException(final Status status, final String message, final Throwable cause){

		this.status = status;
		this.message = message;
		this.cause = cause;

	}

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

}
