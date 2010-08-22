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
package nor.http.server.local;

import java.nio.channels.SocketChannel;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.server.HttpRequestHandler;
import nor.util.log.Logger;

public class LocalContentsHandler implements HttpRequestHandler{

	/**
	 * ルートリソース
	 */
	private final ListResource root;

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(LocalContentsHandler.class);

	public LocalContentsHandler(){

		this.root = new ListResource("");

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	@Override
	public HttpResponse doRequest(final HttpRequest request) {
		LOGGER.entering("doRequest", request);

		HttpResponse ret = null;

		// ローカルホストへのリクエストを処理する
		final String path = request.getPath();
		if(path.startsWith("/")){

			try{
				switch(request.getMethod()){

				case GET:

					ret = this.root.doGet(path, request);
					break;

				case POST:

					ret = this.root.doPost(path, request);
					break;

				case PUT:

					ret = this.root.doPut(path, request);
					break;

				case DELETE:

					ret = this.root.doDelete(path, request);
					break;

					// TODO: Other methods

				}

			}catch(final HttpException e){

				ret = e.createResponse(request);

			}

		}

		if(ret == null){

			ret = HttpException.createResponse(request, Status.NotFound);

		}

		LOGGER.exiting("doRequest", ret);
		return ret;
	}

	@Override
	public SocketChannel doConnectRequest(HttpRequest request) throws HttpException {

		throw new HttpException(Status.MethodNotAllowed);

	}


	/**
	 * ルートリソースを取得する．
	 *
	 * @return ルートリソース
	 */
	public ListResource getRoot(){
		LOGGER.entering("getRoot");

		final ListResource ret = this.root;

		LOGGER.exiting("getRoot", ret);
		return ret;
	}

}


