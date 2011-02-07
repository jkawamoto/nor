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

import java.util.ArrayList;
import java.util.List;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.error.HttpException;
import nor.http.error.MethodNotAllowedException;
import nor.util.log.Logger;

/**
 * Resourceインタフェースの実装を補助するクラス．
 * Resoueceインタフェースのうち，必要なメソッドのみを実装する場合に用いるアダプタクラス．
 * 未実装のメソッドが呼ばれた場合，405 Method Not Allowedエラーを返す．
 *
 * @author KAWAMOTO Junpei
 *
 */
public abstract class Resource{

	private final String name;

	/**
	 * オーバーライドされたメソッド
	 */
	private final Method[] allowed;


	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Resource.class);

	private static final String DoDELETE = "doDelete";
	private static final String DoGET = "doGet";
	private static final String DoPOST = "doPost";
	private static final String DoPUT = "doPut";

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 *
	 * @param name
	 */
	protected Resource(final String name){
		LOGGER.entering("<init>");

		// Set the name
		this.name = name;

		// オーバーライドされているメソッドを調べる
		final Class<? extends Resource> c = this.getClass();
		final List<Method> allowd = new ArrayList<Method>();
		try {

			final java.lang.reflect.Method toDelete = c.getMethod(DoDELETE, String.class, HttpRequest.class);
			if(!toDelete.equals(Resource.class.getMethod(DoDELETE, String.class, HttpRequest.class))){

				allowd.add(Method.DELETE);

			}

			final java.lang.reflect.Method toGet = c.getMethod(DoGET, String.class, HttpRequest.class);
			if(!toGet.equals(Resource.class.getMethod(DoGET, String.class, HttpRequest.class))){

				allowd.add(Method.GET);

			}

			final java.lang.reflect.Method toPost = c.getMethod(DoPOST, String.class, HttpRequest.class);
			if(!toPost.equals(Resource.class.getMethod(DoPOST, String.class, HttpRequest.class))){

				allowd.add(Method.POST);

			}

			final java.lang.reflect.Method toPut = c.getMethod(DoPUT, String.class, HttpRequest.class);
			if(!toPut.equals(Resource.class.getMethod(DoPUT, String.class, HttpRequest.class))){

				allowd.add(Method.PUT);

			}

		} catch (final NoSuchMethodException e) {

		}

		this.allowed = new Method[allowd.size()];
		allowd.toArray(this.allowed);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * リソースの名前を取得する．
	 *
	 * @return このリソースの名前
	 */
	public final String getName() {
		LOGGER.entering("getName");

		LOGGER.exiting("getName", this.name);
		return this.name;

	}

	/**
	 * DELETEメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse doDelete(final String path, final HttpRequest request) throws HttpException{

		throw new MethodNotAllowedException(this.allowed);

	}

	/**
	 * GETメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse doGet(final String path, final HttpRequest request) throws HttpException{

		throw new MethodNotAllowedException(this.allowed);

	}

	/**
	 * POSTメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse doPost(final String path, final HttpRequest request) throws HttpException{

		throw new MethodNotAllowedException(this.allowed);

	}

	/**
	 * PUTメソッドに応える．
	 *
	 * @param path 要求URI
	 * @param request HTTPリクエスト本体
	 * @return HTTPレスポンス
	 */
	public HttpResponse doPut(final String path, final HttpRequest request) throws HttpException{

		throw new MethodNotAllowedException(this.allowed);

	}

}


