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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;

/**
 * Resourceインタフェースの実装を補助するクラス．
 * Resoueceインタフェースのうち，必要なメソッドのみを実装する場合に用いるアダプタクラス．
 * 未実装のメソッドが呼ばれた場合，405 Method Not Allowedエラーを返す．
 *
 * @author KAWAMOTO Junpei
 *
 */
public abstract class Resource{

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Resource.class.getName());

	private static final String TO_DELETE = "toDelete";
	private static final String TO_GET = "toGet";
	private static final String TO_POST = "toPost";
	private static final String TO_PUT = "toPut";

	/**
	 * オーバーライドされたメソッド
	 */
	private final Method[] allowd;


	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * リソースを作成する．
	 *
	 */
	public Resource(){
		LOGGER.entering(Resource.class.getName(), "<init>");

		// オーバーライドされているメソッドを調べる
		final Class<? extends Resource> c = this.getClass();
		final List<Method> allowd = new ArrayList<Method>();
		try {

			final java.lang.reflect.Method toDelete = c.getMethod(TO_DELETE, String.class, HttpRequest.class);
			if(!toDelete.equals(Resource.class.getMethod(TO_DELETE, String.class, HttpRequest.class))){

				allowd.add(Method.DELETE);

			}

			final java.lang.reflect.Method toGet = c.getMethod(TO_GET, String.class, HttpRequest.class);
			if(!toGet.equals(Resource.class.getMethod(TO_GET, String.class, HttpRequest.class))){

				allowd.add(Method.GET);

			}

			final java.lang.reflect.Method toPost = c.getMethod(TO_POST, String.class, HttpRequest.class);
			if(!toPost.equals(Resource.class.getMethod(TO_POST, String.class, HttpRequest.class))){

				allowd.add(Method.POST);

			}

			final java.lang.reflect.Method toPut = c.getMethod(TO_PUT, String.class, HttpRequest.class);
			if(!toPut.equals(Resource.class.getMethod(TO_PUT, String.class, HttpRequest.class))){

				allowd.add(Method.PUT);

			}

		} catch (final NoSuchMethodException e) {

		}

		this.allowd = new Method[allowd.size()];
		allowd.toArray(this.allowd);

		LOGGER.exiting(Resource.class.getName(), "<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * リソースの名前を取得する．
	 *
	 * @return このリソースの名前
	 */
	public String getName() {
		LOGGER.entering(Resource.class.getName(), "getName");

		final String ret = this.getClass().getSimpleName();

		LOGGER.exiting(Resource.class.getName(), "getName", ret);
		return ret;

	}

	/**
	 * DELETEメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse toDelete(String path, HttpRequest request) {
		LOGGER.entering(Resource.class.getName(), "toDelete", new Object[]{path, request});

		final HttpResponse ret = createErrorResponse(request);

		LOGGER.exiting(Resource.class.getName(), "toDelete", ret);
		return ret;

	}

	/**
	 * GETメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse toGet(String path, HttpRequest request) {
		LOGGER.entering(Resource.class.getName(), "toGet", new Object[]{path, request});

		final HttpResponse ret = createErrorResponse(request);

		LOGGER.exiting(Resource.class.getName(), "toGet", ret);
		return ret;

	}

	/**
	 * POSTメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse toPost(String path, HttpRequest request) {
		LOGGER.entering(Resource.class.getName(), "toPost", new Object[]{path, request});

		final HttpResponse ret = createErrorResponse(request);

		LOGGER.exiting(Resource.class.getName(), "toPost", ret);
		return ret;

	}

	/**
	 * PUTメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse toPut(String path, HttpRequest request) {
		LOGGER.entering(Resource.class.getName(), "toPut", new Object[]{path, request});

		final HttpResponse ret = createErrorResponse(request);

		LOGGER.exiting(Resource.class.getName(), "toPut", ret);
		return ret;

	}

	//====================================================================
	//  private メソッド
	//====================================================================
	/**
	 * 405 Method Not Allowedエラーを返す．
	 *
	 * @param request リクエストオブジェクト
	 * @return エラーレスポンスオブジェクト
	 */
	private HttpResponse createErrorResponse(final HttpRequest request){

		final String body = "405 Method Not Allowed";
		final HttpResponse ret = request.createResponse(Status.MethodNotAllowed, body);

		final HttpHeader header = ret.getHeader();
		header.add(HeaderName.ContentType, "text/javascript; charset=utf-8\n");
		header.add(HeaderName.Server, System.getProperty("app.name"));
		header.add(HeaderName.ContentLength, Integer.toString(body.getBytes().length));

		if(this.allowd.length != 0){

			String allow = new String();
			for(final Method m : this.allowd){

				allow += m.toString();
				allow += ", ";

			}
			header.add(HeaderName.Allow, allow.substring(0, allow.length()-2));

		}

		return ret;

	}

}


