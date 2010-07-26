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
package nor.core.proxy;

import java.util.regex.MatchResult;

import nor.core.proxy.filter.MessageHandlerAdapter;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.local.ListResource;
import nor.http.server.local.LocalContentsHandler;

/**
 * Message handler for local resources.
 *
 * @author Junpei Kawamoto
 *
 */
class LocalHandler extends MessageHandlerAdapter{

	private final LocalContentsHandler impl = new LocalContentsHandler();

	//============================================================================
	//  Constructor
	//============================================================================
	public LocalHandler() {
		super("^/.*");
	}

	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public HttpResponse doRequest(final HttpRequest request, final MatchResult url) {

		final HttpResponse ret = impl.doRequest(request);

		return ret;

	}

	public ListResource getRoot() {

		return impl.getRoot();

	}

}
