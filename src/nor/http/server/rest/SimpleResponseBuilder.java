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
package nor.http.server.rest;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nor.http.ContentType;
import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;

public class SimpleResponseBuilder {

	// ロガー
	// private static final Logger LOGGER = Logger.getLogger(SimpleResponseBuilder.class.getName());

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	public static HttpResponse create(final HttpRequest request, final String body, final ContentType contentType){

		final HttpResponse ret = request.createResponse(Status.OK, body);

		final HttpHeader header = ret.getHeader();
		header.add(HeaderName.ContentType, contentType.toString());
		header.add(HeaderName.LastModified, DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.add(HeaderName.Server, System.getProperty("app.name"));
		header.add(HeaderName.ContentLength, Integer.toString(body.getBytes().length));
		header.add(HeaderName.Date, DATE_FORMAT.format(Calendar.getInstance().getTime()));

		return ret;

	}

	public static HttpResponse create(final HttpRequest request){

		final HttpResponse ret = request.createResponse(Status.OK);

		final HttpHeader header = ret.getHeader();
		header.add(HeaderName.LastModified, DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.add(HeaderName.Server, System.getProperty("app.name"));
		header.add(HeaderName.Date, DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.add(HeaderName.Connection, "close");

		return ret;

	}

}


