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

	protected Pattern pattern;

	public RequestFilterAdapter(final Pattern pat){

		this.pattern = pat;

	}

	public RequestFilterAdapter(final String regex){

		this.pattern = Pattern.compile(regex);

	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.arthra.lotte.Matchable#getPattern()
	 */
	@Override
	public Pattern pattern() {

		return this.pattern;

	}

}
