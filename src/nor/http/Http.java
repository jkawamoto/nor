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

import java.util.regex.Pattern;

public class Http {

	/**
	 * プロトコルバージョン．
	 */
	public static final String VERSION = "1.1";

	/**
	 * サーバ名．
	 */
	public static final String SERVERNAME = "Nor";

	static final String CHUNKED = "chunked";
	static final String GZIP = "gzip";
	static final String DEFLATE = "deflate";

	static final String REQUEST_LINE_TEMPLATE = "%s %s HTTP/%s";
	static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");

	static final String RESONSE_LINE_TEMPLATE = "HTTP/%s %d %s";
	static final Pattern RESONSE_LINE_PATTERN = Pattern.compile("^HTTP/(\\S{3})\\s+(\\d{3})\\s*(.*)$");

	private Http(){

	}

}
