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
package nor.http.server.proxyserver;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Router {

	private final Map<Pattern, Proxy> routs = new HashMap<Pattern, Proxy>();


	public void add(final Pattern pattern, final Proxy proxy){

		this.routs.put(pattern, proxy);

	}

	public void remove(final Pattern pat){

		this.routs.remove(pat);

	}

	public Proxy query(final String url){

		for(final Pattern p : this.routs.keySet()){

			final Matcher m = p.matcher(url);
			if(m.matches()){

				return this.routs.get(p);

			}
		}

		return Proxy.NO_PROXY;

	}

}
