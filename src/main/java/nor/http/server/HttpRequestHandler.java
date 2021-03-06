/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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
package nor.http.server;

import java.nio.channels.SocketChannel;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.error.HttpException;

/**
 * Httpサーバにおいて，Httpリクエストのハンドラに必要なインタフェイス．
 * このインタフェースを実装するクラスはマルチスレッドでアクセスされることを意識する必要がある．
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface HttpRequestHandler {

	/**
	 * リクエストを処理しレスポンスを返す．
	 *
	 * @param request Http リクエストインスタンス
	 * @return 与えられたリクエストに対するレスポンス
	 */
	public HttpResponse doRequest(final HttpRequest request);


	/**
	 * Connect リクエストを処理しソケットチャンネルを返す．
	 *
	 * @param request A connect request
	 * @return SocketChannel connected to the requested host.
	 * @throws HttpException If some IO error happens.
	 */
	public SocketChannel doConnectRequest(final HttpRequest request) throws HttpException;

}

