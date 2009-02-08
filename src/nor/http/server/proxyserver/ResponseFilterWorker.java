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

import java.io.Closeable;
import java.io.IOException;

import nor.http.Header;
import nor.http.Request;

/**
 * @author KAWAMOTO Junpei
 *
 */
class ResponseFilterWorker<Streams extends Closeable, Filter extends ResponseFilter<Streams>> implements Runnable {

	private final ResponseFilter<Streams> _filter;
	private final Request _request;
	private final Header _header;
	private final Streams _streams;

	public ResponseFilterWorker(final Filter filter, final Request request, final Header header, final Streams streams){

		this._filter = filter;
		this._request = request;
		this._header = header;
		this._streams = streams;

	}

	@Override
	public void run() {

		this._filter.update(this._request, this._header, this._streams);
		try {
			this._streams.close();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

}


