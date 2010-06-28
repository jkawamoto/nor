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
package nor.core.proxy.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReadonlyPatternMatchingFilter extends ReadonlyStringFilterAdapter{

	private final Map<Pattern, List<MatchingEventListener>> listeners = new HashMap<Pattern, List<MatchingEventListener>>();

	//============================================================================
	//  Public methods
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.ReadonlyStringFilter#update(java.lang.String)
	 */
	@Override
	public final void update(final String in) {

		for(final Pattern pat : this.listeners.keySet()){

			final Matcher m = pat.matcher(in);
			while(m.find()){

				for(final MatchingEventListener l : this.listeners.get(pat)){

					l.update(m.toMatchResult());

				}

			}

		}

	}

	public void addEventListener(final Pattern pat, final MatchingEventListener l){

		if(this.listeners.containsKey(pat)){

			this.listeners.get(pat).add(l);

		}else{

			final List<MatchingEventListener> list = new ArrayList<MatchingEventListener>();
			list.add(l);
			this.listeners.put(pat, list);

		}

	}

	public void addEventListener(final String regex, final MatchingEventListener l){

		this.addEventListener(Pattern.compile(regex), l);

	}

	public void removeEventListener(final MatchingEventListener l){

		for(final Pattern pat : this.listeners.keySet()){

			this.listeners.get(pat).remove(l);

		}

	}

	public interface MatchingEventListener{

		public void update(final MatchResult result);

	}

}
