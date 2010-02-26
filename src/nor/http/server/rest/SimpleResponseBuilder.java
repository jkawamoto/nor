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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

import nor.http.HeaderName;
import nor.http.HttpError;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

public class SimpleResponseBuilder {

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(SimpleResponseBuilder.class.getName());

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	public static HttpResponse create(final HttpRequest request, final String body, final String contentType){

		HttpResponse ret = null;

		final StringBuilder header = new StringBuilder();
		header.append("HTTP/1.1 200 OK\n");

		// コンテンツタイプ
		header.append("Content-Type: ");
		header.append(contentType);
		header.append("\n");

		header.append("Last-Modified: ");
		header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.append("\n");

		// アプリケーション名
		header.append("Server: ");
		header.append(System.getProperty("app.name"));
		header.append("\n");

		header.append("Content-Length: ");
		header.append(body.getBytes().length);
		header.append("\n");
		header.append("Date: ");
		header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.append("\n\n");

		try {

			final InputStream in = new SequenceInputStream(new ByteArrayInputStream(header.toString().getBytes()), new ByteArrayInputStream(body.getBytes()));

			ret = request.createResponse(in);
			in.close();

		} catch (FileNotFoundException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (IOException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (HttpError e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return ret;

	}

	public static HttpResponse create(final HttpRequest request){

		HttpResponse ret = null;

		final StringBuilder header = new StringBuilder();
		header.append("HTTP/1.1 200 OK\n");
		header.append("Last-Modified: ");
		header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.append("\n");

		// アプリケーション名
		header.append("Server: ");
		header.append(System.getProperty("app.name"));
		header.append("\n");

		header.append("Date: ");
		header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.append("\n\n");

		header.append(String.format("%s:%s", HeaderName.Connection, "close"));

		try {

			final InputStream in = new ByteArrayInputStream(header.toString().getBytes());

			ret = request.createResponse(in);
			in.close();

		} catch (FileNotFoundException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (IOException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (HttpError e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

		return ret;

	}

}


