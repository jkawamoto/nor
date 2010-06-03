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
package nor.core.proxy.filter;

import java.util.regex.Pattern;

public abstract class RequestFilterAdapter implements RequestFilter{

	final private Pattern url;
	final private Pattern cType;

	public RequestFilterAdapter(final Pattern url, final Pattern cType){

		this.url = url;
		this.cType = cType;

	}

	public RequestFilterAdapter(final String urlRegex, final String cTypeRegex){

		this.url = Pattern.compile(urlRegex);
		this.cType = Pattern.compile(cTypeRegex);

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.MessageFilter#getFilteringURL()
	 */
	@Override
	public Pattern getFilteringURL() {

		return this.url;

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.MessageFilter#getFilteringContentType()
	 */
	@Override
	public Pattern getFilteringContentType() {

		return this.cType;

	}

}
