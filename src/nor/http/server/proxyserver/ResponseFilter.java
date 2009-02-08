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
package nor.http.server.proxyserver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.Header;
import nor.http.Request;


/**
 * @author KAWAMOTO Junpei
 *
 */
public abstract class ResponseFilter<Streams>{

	private final Pattern _url;

	public ResponseFilter(final String url){

		this._url = Pattern.compile(url);

	}

	boolean isFiltering(final String url){

		final Matcher m = this._url.matcher(url);
		return m.matches();

	}

	public abstract void update(final Request request, final Header header, final Streams body);

}