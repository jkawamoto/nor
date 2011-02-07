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
package nor.http.server.ssl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;

public class DecryptHandler implements ConnectHandler{

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(DecryptHandler.class.getName());

	public Result doConnect(final HttpRequest request, final InputStream input, final OutputStream output) throws IOException {
		LOGGER.entering(DecryptHandler.class.getName(), "doConnect");

		Result result = null;

		// final String prefix = "https://" + request.getHeader().get(HttpHeader.HeaderName.Host, 0);
		final String prefix = "https://" + request.getPath();

		final HttpResponse ret = request.createResponse(Status.OK);
		final HttpHeader header = ret.getHeader();
		header.add("Proxy-agent", "nor/1.0");


		ret.writeTo(output);

		// なぜかこいつを使いまわすと落ちる
		SecureManager m = new SecureManager();

		SecureManager.SecureSession s = m.createSession(input, output);

		result = new Result(prefix, s.getInputStream(), s.getOutputStream());


		LOGGER.exiting(DecryptHandler.class.getName(), "doConnect", result);
		return result;

	}

}


