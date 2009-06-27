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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nor.http.Header;
import nor.http.Request;
import nor.http.Response;
import nor.http.Body2.IOStreams;
import nor.util.observer.Observer;

/**
 * @author KAWAMOTO Junpei
 *
 */
public interface ResponseFilter extends Observer<ResponseFilter.ResponseInfo>{

	@Override
	public void update(final ResponseInfo register);


	public class ResponseInfo {

		private final Response _response;

		private final List<TransferListener> _postFilters = new ArrayList<TransferListener>();

		ResponseInfo(final Response response){

			this._response = response;

		}

		public void addPreTransferListener(final TransferListener listener){
			assert listener != null;

			try {

				final IOStreams s = _response.getBody().getIOStreams();
				final Thread th = new Thread(new Runnable(){

					@Override
					public void run() {


						listener.update(s);

						s.close();


					}

				});
				th.start();

			} catch (IOException e) {

				// TODO 自動生成された catch ブロック
				e.printStackTrace();

			}

		}

		public void addPostTransferListener(final TransferListener listener){
			assert listener != null;

			this._postFilters.add(listener);

		}

		public Request getRequest(){

			return this._response.getRequest();

		}

		public String getHeadline(){

			return this._response.getHeadLine();

		}

		public Header getHeader(){

			return this._response.getHeader();

		}

	}


}