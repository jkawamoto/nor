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

import java.util.ArrayList;
import java.util.List;

import nor.http.Request;
import nor.http.Response;

/**
 * @author KAWAMOTO Junpei
 *
 */
public interface ResponseFilter extends MessageFilter<ResponseFilter.Info>{

	@Override
	public void update(final Info register);


	public class Info extends MessageFilter.Info{

		private final Response _response;

		private final List<TransferredListener> _postFilters = new ArrayList<TransferredListener>();

		Info(final Response response){
			super(response);

			this._response = response;

		}

		public void addPostTransferListener(final TransferredListener listener){
			assert listener != null;

			this._postFilters.add(listener);

		}

		public Request getRequest(){

			return this._response.getRequest();

		}

		List<TransferredListener> getPostTransferListeners(){

			return this._postFilters;

		}

	}


}