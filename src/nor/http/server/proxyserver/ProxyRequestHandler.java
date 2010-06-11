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
//$Id: ProxyRequestHandler.java 471 2010-04-03 10:25:20Z kawamoto $
package nor.http.server.proxyserver;

import static nor.http.HeaderName.AcceptEncoding;
import static nor.http.HeaderName.Connection;
import static nor.http.HeaderName.KeepAlive;
import static nor.http.HeaderName.ProxyConnection;
import static nor.http.HeaderName.Via;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.regex.Pattern;

import nor.http.ErrorResponseBuilder;
import nor.http.Status;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.util.log.EasyLogger;


/**
 * プロキシサーバとして働くHttpRequestHandler．
 * このクラスは，Httpサーバにおけるリクエストハンドラであり，HttpRequestHandleableを
 * 実装している．要求が来ると，URLConnectionを用いてWeb上から該当のリソースを取得し
 * レスポンスとして送信する．即ちプロキシとして動作する．
 * <br />
 * また，CookieやQuery，メッセージボディに対するフィルタリング機構を提供し，
 * プロキシサーバを通過するこれらのデータをトラップし処理することが可能である．
 * フィルタリングにはObserverパターンを利用している．
 * <br />
 *
 * 外部への接続にプロキシを通さなければならない場合，環境変数に設定する．
 * <dl>
 * 	<dt>例</dt>
 * 	<dd>-Dhttp.proxyHost=proxy.kuins.net -Dhttp.proxyPort=8080</dd>
 * </dl>
 *
 * @author KAWAMOTO Junpei
 *
 */
public class ProxyRequestHandler implements HttpRequestHandler{

	// このクラスはスレッドセーフ
	private final String name;
	private final String version;

	private final Router router = new Router();

	private static final String Close = "close";

	private static final EasyLogger LOGGER = EasyLogger.getLogger(ProxyRequestHandler.class);


	//============================================================================
	//  public メソッド
	//============================================================================
	public ProxyRequestHandler(final String name, final String version){
		LOGGER.entering("<init>", name, version);
		assert name != null && name.length() != 0;
		assert version != null;

		this.name = name;
		this.version = version;

		HttpURLConnection.setFollowRedirects(false);

		LOGGER.exiting("<init>");
	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequestHandleable#doRequest(jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse doRequest(final HttpRequest request){
		LOGGER.entering("doRequest", request);
		assert request != null;

		LOGGER.finest(request.getHeadLine());

		HttpResponse response = null;
		try{

			// ヘッダーの書き換え
			this.cleanHeader(request);

			// URLコネクションの作成
			HttpURLConnection con;
			final URL url = new URL(request.getPath());
			final Proxy proxy = this.router.query(request.getPath());
			con = (HttpURLConnection)url.openConnection(proxy);

			// リクエストの送信とレスポンスの作成
			response = request.createResponse(con);

		} catch (final IOException e) {

			LOGGER.warning(String.format("IOException [%s]", e.getLocalizedMessage()));

			final StringWriter body = new StringWriter();
			e.printStackTrace(new PrintWriter(body));
			response = ErrorResponseBuilder.create(request, Status.InternalServerError, body.toString());

		}

		// ヘッダの整理
		this.cleanHeader(response);

		LOGGER.info(request.getHeadLine() + " > " + response.getHeadLine());

		LOGGER.exiting("doRequest", response);
		return response;

	}

	//--------------------------------------------------------------------
	//  外部プロキシの設定
	//--------------------------------------------------------------------
	/**
	 * 外部プロキシを設定する．
	 * @param extProxyHost プロキシホスト名
	 * @param extProxyPort プロキシポート番号
	 * @throws MalformedURLException
	 */
	public void addRouting(final Pattern pat, final URL extProxyHost){
		LOGGER.entering("addRouting", pat, extProxyHost);
		assert pat != null;
		assert extProxyHost != null;;

		final InetSocketAddress extProxyAddr = new InetSocketAddress(extProxyHost.getHost(), extProxyHost.getPort());
		this.router.put(pat, new Proxy(Type.HTTP, extProxyAddr));

		LOGGER.info("外部プロキシを使用 [" + extProxyAddr + "]");

		LOGGER.exiting("addRouting");
	}

	/**
	 * 外部プロキシの設定を解除する．
	 *
	 */
	public void removeRouting(final Pattern pat){
		LOGGER.entering("removeRouting", pat);
		assert pat != null;

		this.router.remove(pat);

		LOGGER.exiting("removeRouting");
	}

	//============================================================================
	//  private メソッド
	//============================================================================
	private void cleanHeader(final HttpRequest request){
		LOGGER.entering("cleanHeader", request);
		assert request != null;

		final HttpHeader header = request.getHeader();

		// 許容できるエンコーディングの指定
		header.set(AcceptEncoding, "gzip, identity");

		// 持続接続を行うか
		String timeout = null;
		boolean close = false;
		if(header.containsKey(KeepAlive)){

			timeout = header.get(KeepAlive);

		}else if(header.containsValue(Connection, Close)){

			close = true;

		}

		// ホップバイホップヘッダの削除
		if(header.containsKey(Connection)){

			for(final String value : header.get(Connection).split(",")){

				header.remove(value.trim());

			}
			header.remove(Connection);

		}
		// TODO: さらにプロキシ経由の場合も消していいのか？>とりあえず保存する
		if(this.router.query(request.getPath()) == null && header.containsKey(ProxyConnection)){

			for(final String value : header.get(ProxyConnection).split(",")){

				header.remove(value.trim());

			}
			header.remove(ProxyConnection);

		}

		// 持続接続の設定
		if(timeout != null){

			header.set(Connection, KeepAlive.toString());
			header.set(KeepAlive, timeout);

		}else if(close){

			header.set(Connection, Close);

		}

		// プロキシ通過スタンプ
		header.add(Via, String.format("%s %s", this.version, this.name));

		LOGGER.exiting("cleanHeader");
	}

	/**
	 * レスポンスヘッダの書き換え．
	 * ホップバイホップヘッダを削除する．
	 *
	 * @param response 整理するレスポンス
	 */
	private void cleanHeader(final HttpResponse response){
		LOGGER.entering("cleanHeader", response);
		assert response != null;

		final HttpHeader header = response.getHeader();

		// ホップバイホップヘッダの削除
		if(header.containsKey(Connection)){

			boolean isClose = false;
			for(final String value : header.get(Connection).split(",")){

				final String tvalue = value.trim();
				if("close".equalsIgnoreCase(tvalue)){

					isClose = true;

				}else{

					header.remove(value.trim());

				}

			}
			header.remove(Connection);
			if(isClose){

				header.set(Connection, "close");

			}

		}
		if(header.containsKey(ProxyConnection)){

			for(final String value : header.get(ProxyConnection).split(",")){

				header.remove(value.trim());

			}
			header.remove(ProxyConnection);

		}

		// プロキシ通過スタンプ
		header.add(Via, String.format("%s %s", this.version, this.name));

		LOGGER.exiting("cleanHeader");
	}

}
