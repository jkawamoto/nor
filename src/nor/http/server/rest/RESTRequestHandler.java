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
package nor.http.server.rest;

import java.util.logging.Logger;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.error.HttpException;
import nor.http.error.NotFoundException;
import nor.http.server.HttpRequestHandler;

/**
 * RESTアーキテクチャを実装するリクエストハンドラクラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class RESTRequestHandler implements HttpRequestHandler{

	/**
	 * ルートリソース
	 */
	private final RootResource root = new RootResource();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(RESTRequestHandler.class.getName());

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * ルートリソースを取得する．
	 *
	 * @return ルートリソース
	 */
	public DirResource getRoot(){
		LOGGER.entering(RESTRequestHandler.class.getName(), "getRoot");

		final DirResource ret = this.root;

		LOGGER.exiting(RESTRequestHandler.class.getName(), "getRoot", ret);
		return ret;
	}

	public HttpResponse doRequest(final HttpRequest request) {
		LOGGER.entering(RESTRequestHandler.class.getName(), "doRequest", request);

		HttpResponse ret = null;

		try{

			// ローカルホストへのリクエストを処理する
			final String path = request.getPath();
			if(path.startsWith("/")){

				switch(Method.valueOf(request.getMethodString())){

				case GET:

					ret = this.root.toGet(path, request);
					break;

				case POST:

					ret = this.root.toPost(path, request);
					break;

				case PUT:

					ret = this.root.toPut(path, request);
					break;

				case DELETE:

					ret = this.root.toDelete(path, request);
					break;

				}

			}

		}catch(final HttpException e){

			ret = e.createResponse(request);

		}

		LOGGER.exiting(RESTRequestHandler.class.getName(), "doRequest", ret);
		return ret;

	}

	//====================================================================
	//  private クラス
	//====================================================================
	/**
	 * RESTアーキテクチャにおいて，Rootリソースを表すクラス．
	 *
	 * @author KAWAMOTO Junpei
	 *
	 */
	private class RootResource extends DirResource{

		/**
		 * コンストラクタ
		 */
		public RootResource() {
			super("");
			LOGGER.entering(RootResource.class.getName(), "<init>");

			LOGGER.exiting(RootResource.class.getName(), "<init>");
		}

		@Override
		public HttpResponse toDelete(String path, HttpRequest request) throws HttpException{
			LOGGER.entering(RootResource.class.getName(), "toDelete");

			HttpResponse ret = super.toDelete(path, request);
			if(ret == null){

				throw new NotFoundException();

			}

			LOGGER.exiting(RootResource.class.getName(), "toDelete", ret);
			return ret;
		}

		@Override
		public HttpResponse toGet(String path, HttpRequest request) throws HttpException{
			LOGGER.entering(RootResource.class.getName(), "toGet");

			HttpResponse ret =  super.toGet(path, request);
			if(ret == null){

				throw new NotFoundException();

			}

			LOGGER.exiting(RootResource.class.getName(), "toGet", ret);
			return ret;
		}

		@Override
		public HttpResponse toPost(String path, HttpRequest request) throws HttpException{
			LOGGER.entering(RootResource.class.getName(), "toPost");

			HttpResponse ret = super.toPost(path, request);
			if(ret == null){

				throw new NotFoundException();

			}

			LOGGER.exiting(RootResource.class.getName(), "toPost", ret);
			return ret;
		}

		@Override
		public HttpResponse toPut(String path, HttpRequest request) throws HttpException{
			LOGGER.entering(RootResource.class.getName(), "toPut");

			HttpResponse ret = super.toPut(path, request);
			if(ret == null){

				throw new NotFoundException();

			}

			LOGGER.exiting(RootResource.class.getName(), "toPut", ret);
			return ret;
		}

	}

}


