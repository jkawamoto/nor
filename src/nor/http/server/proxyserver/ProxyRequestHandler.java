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
package nor.http.server.proxyserver;

import static nor.http.HeaderName.AcceptEncoding;
import static nor.http.HeaderName.Connection;
import static nor.http.HeaderName.KeepAlive;
import static nor.http.HeaderName.ProxyConnection;
import static nor.http.HeaderName.Via;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import nor.http.HeaderName;
import nor.http.Http;
import nor.http.HttpBody;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.error.InternalServerErrorException;
import nor.http.server.HttpRequestHandler;
import nor.util.log.Logger;


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

	private final String name;
	private final String version;

	private final Router router;

	private static final int Timeout;

	private static final String Close = "close";
	private static final String VIA_FORMAT = "%s %s";

	private static final Logger LOGGER = Logger.getLogger(ProxyRequestHandler.class);


	//============================================================================
	//  public メソッド
	//============================================================================
	public ProxyRequestHandler(final String name, final String version, final Router router){
		LOGGER.entering("<init>", name, version, router);
		assert name != null && name.length() != 0;
		assert version != null;
		assert router != null;

		this.name = name;
		this.version = version;
		this.router = router;

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

		LOGGER.finest("doRequest", "{0}", request);

		HttpResponse response = null;
		try{

			// This handler doesn't support the connect method.
			if(Method.CONNECT.equals(request.getMethodString())){

				throw new HttpException(Status.NotImplemented);

			}

			// ヘッダーの書き換え
			this.cleanHeader(request);

			// URLコネクションの作成
			final URL url = new URL(request.getPath());
			final Proxy proxy = this.router.query(request.getPath());
			if(proxy != Proxy.NO_PROXY){

				LOGGER.config("doRequest", "Use the proxy; {0}", proxy);

			}

			LOGGER.fine("doRequest", "Send the {0}", request);

			// リクエストの送信とレスポンスの作成
			final HttpURLConnection con = (HttpURLConnection)url.openConnection(proxy);
			con.setConnectTimeout(Timeout);
			this.sendRequest(con, request);
			response = this.receiveResponse(con, request);

			LOGGER.fine("doRequest", "Receive the {0}", response);

		} catch (final IOException e) {

			LOGGER.warning("doRequest", "Catch a IOException({0})", e.getMessage());
			LOGGER.catched(Level.FINE, "doRequest", e);

			response = HttpException.createResponse(request, Status.InternalServerError, e);

		} catch (final HttpException e) {

			LOGGER.warning("doRequest", "Catch a HttpException({0})", e.getMessage());
			LOGGER.catched(Level.FINE, "doRequest", e);

			response = e.createResponse(request);

		}

		// ヘッダの整理
		this.cleanHeader(response);

		LOGGER.exiting("doRequest", response);
		return response;

	}

	//============================================================================
	//  private メソッド
	//============================================================================
	/**
	 * HttpURLConnection を用いてリクエストを送信する．
	 *
	 * @param con 送信先の HttpURLConnection インスタンス
	 * @param request 送信するリクエスト
	 * @throws HttpException リクエストの送信にエラーが起こった場合
	 */
	private void sendRequest(final HttpURLConnection con, final HttpRequest request) throws HttpException{

		// リクエストヘッダの登録
		final HttpHeader header = request.getHeader();
		final HttpBody body = request.getBody();
		for(final String key : header.keySet()){

			con.addRequestProperty(key, header.get(key));

		}

		try{

			// ボディがある場合は送信
			if(header.containsKey(HeaderName.ContentLength)){

				final int length = Integer.parseInt(header.get(HeaderName.ContentLength));
				con.setFixedLengthStreamingMode(length);
				con.setDoOutput(true);
				con.connect();

				body.output(con.getOutputStream(), header);

			}else if(header.containsKey(HeaderName.TransferEncoding)){

				con.setDoOutput(true);
				con.connect();

				body.output(con.getOutputStream(), header);

			}else{

				con.connect();

			}
			body.close();

		}catch(final SocketTimeoutException e){

			LOGGER.warning("sendRequest", e.getMessage());
			throw new HttpException(Status.RequestTimeout, e);

		}catch(final ConnectException e){

			LOGGER.warning("sendRequest", e.getMessage());
			throw new HttpException(Status.RequestTimeout, e);

		}catch(final IOException e){

			throw new InternalServerErrorException(e);

		}

	}

	/**
	 * HttpURLConnection を用いてレスポンスを受信する．
	 *
	 * @param con レスポンスを受信する HttpURLConnection インスタンス
	 * @param request レスポンスの元となるリクエスト
	 * @return 受信したレスポンス
	 * @throws HttpException データの受信中にエラーが起こった場合
	 */
	private HttpResponse receiveResponse(final HttpURLConnection con, final HttpRequest request) throws HttpException{

		// レスポンスの作成
		HttpResponse ret = null;
		try {

			final int code = con.getResponseCode();
			InputStream resStream = null;
			if(code == -1){

				throw new InternalServerErrorException("Recieve an invalid message.");

			}else if(code < 400){

				resStream = con.getInputStream();

				// ContentLength も TransferEncoding も指定していない場合は内容コーディングを無視する．
				if(con.getHeaderField(HeaderName.ContentLength.toString()) != null || con.getHeaderField(HeaderName.TransferEncoding.toString()) != null){

					// 内容エンコーディングの解決
					final String encode = con.getHeaderField(HeaderName.ContentEncoding.toString());
					if(encode != null){

						if(Http.GZIP.equalsIgnoreCase(encode)){

							LOGGER.info("receiveResponse", "{0}: {1} : {2}", con, con.getResponseMessage(), con.getHeaderFields());
							resStream = new GZIPInputStream(resStream);

						}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

							resStream = new DeflaterInputStream(resStream);

						}

					}

				}

			}else{

				resStream = con.getErrorStream();

			}
			ret = request.createResponse(Status.valueOf(code), resStream);

			// ヘッダの登録
			final HttpHeader resHeader = ret.getHeader();
			final Map<String, List<String>> fields = con.getHeaderFields();
			for(final String key : fields.keySet()){

				if(key != null){

					for(final String value : fields.get(key)){

						resHeader.add(key, value);

					}

				}

			}

		}catch(final IOException e){

			throw new InternalServerErrorException(e);

		}

		return ret;

	}

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
		this.removeHopByHopHeader(header);

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
		header.add(Via, String.format(VIA_FORMAT, this.version, this.name));

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
		this.removeHopByHopHeader(header);

		if(header.containsKey(ProxyConnection)){

			for(final String value : header.get(ProxyConnection).split(",")){

				header.remove(value.trim());

			}
			header.remove(ProxyConnection);

		}

		// プロキシ通過スタンプ
		header.add(Via, String.format(VIA_FORMAT, this.version, this.name));

		LOGGER.exiting("cleanHeader");
	}

	/**
	 * ホップバイホップヘッダの削除．
	 *
	 * @param header 削除対象のヘッダ
	 */
	private void removeHopByHopHeader(final HttpHeader header){

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

	}

	//============================================================================
	//  Class constructor
	//============================================================================
	static{

		final String classname = ProxyRequestHandler.class.getName();
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));

		LOGGER.config("<init>", "Load a constant: Timeout = {0}", Timeout);

	}

}
