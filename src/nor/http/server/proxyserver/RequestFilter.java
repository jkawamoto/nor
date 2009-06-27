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
import java.util.logging.Logger;

import nor.http.Header;
import nor.http.Request;
import nor.http.Body2.IOStreams;
import nor.util.observer.Observer;

/**
 * HTTPリクエストに対するフィルタインタフェース．
 * フィルタリング機構はObserverパターンを採用しており，新たなリクエストが送信される前に，通知が行われる．
 * 通知を受け取るためには，このインタフェースを実装したオブジェクトをSubjectへ登録する必要がある．
 *
 * @author KAWAMOTO Junpei
 *
 */
public interface RequestFilter extends Observer<RequestFilter.RequestInfo>{

	/**
	 * 新たなHTTPリクエストが送信される前に呼ばれる．
	 *
	 * @param request 送信されようとしているHTTPリクエスト
	 */
	@Override
	public void update(final RequestInfo request);

	public class RequestInfo {

		private final Request _request;

		/**
		 * ロガー
		 */
		private static final Logger LOGGER = Logger.getLogger(RequestInfo.class.getName());

		RequestInfo(final Request request){

			this._request = request;

		}

		public void addPreTransferListener(final TransferListener listener){
			assert listener != null;

			try {

				final IOStreams s = _request.getBody().getIOStreams();
				final Thread th = new Thread(new Runnable(){

					@Override
					public void run() {

						listener.update(s.in, s.out);
						try {

							s.close();

						} catch (IOException e) {

							LOGGER.warning("Cannot close " + this + " caused by " + e.getLocalizedMessage());

						}

					}

				});
				th.start();

			} catch (IOException e) {

				LOGGER.severe("Cannot get IOStreams caused by " + e.getLocalizedMessage());

			}

		}

		public String getHeadline(){

			return this._request.getHeadLine();

		}

		public Header getHeader(){

			return this._request.getHeader();

		}

	}

}


