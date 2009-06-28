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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.Header;
import nor.http.HeaderName;
import nor.http.Request;
import nor.http.Response;
import nor.http.Body2.IOStreams;
import nor.http.error.BadRequestException;
import nor.http.error.HttpException;
import nor.http.server.RequestHandler;
import nor.util.CopyingOutputStream;
import nor.util.observer.BasicSubject;
import nor.util.observer.Subject;

/**
 * プロキシサーバとして働くHttpリクエストハンドラ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class ProxyRequestHandler implements RequestHandler{

	/**
	 * Httpリクエストに対するフィルタ
	 */
	private final Subject<RequestFilter.RequestInfo, RequestFilter> _requestFilters = BasicSubject.create();

	/**
	 * Httpレスポンスに対するフィルタ
	 */
	private final Subject<ResponseFilter.ResponseInfo, ResponseFilter> _responseFilters = BasicSubject.create();

	/**
	 * 外部プロキシのホスト
	 */
	private String _proxy_host;

	/**
	 * 外部プロキシのポート
	 */
	private int _proxy_port;

	private final String _serverName;
	private static final String Via = "%s 1.1";

	private final Requester _requester = new Requester();

	private final int BufferSize = 10240;

	private final ExecutorService _executors = Executors.newCachedThreadPool();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(ProxyRequestHandler.class.getName());


	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * コンストラクタ．
	 * 外部プロキシを利用しない場合のコンストラクタ
	 */
	public ProxyRequestHandler(final String serverName){
		assert serverName != null && serverName.length() != 0;

		this._serverName = serverName;

	}

	/**
	 * コンストラクタ．
	 * 外部プロキシを利用する設定の場合，プロキシの準備を行う．
	 */
	public ProxyRequestHandler(final String serverName, final String extProxyHost, final int extProxyPort){
		this(serverName);

		this.setExternalProxy(extProxyHost, extProxyPort);

	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequestHandleable#doRequest(jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public Response doRequest(final Request request){
		assert request != null;

		// リクエストに対するフィルタを実行
		this.filtering(request);

		// HTTPヘッダの整理
		this.cleanRequestHeader(request.getHeader());

		Response response = null;
		if(this._proxy_host == null){ // 外部プロキシを使用しない場合

			try{

				// 要求パスを相対URLに書き替える
				// HTTP1.1の仕様上絶対パスを送信しても構わないが，相対パスしか認識できないサーバが存在する
				final URL url = new URL(request.getPath());
				if(url.getQuery() != null){

					request.setPath(String.format("%s?%s", url.getPath(), url.getQuery()));

				}else{

					request.setPath(url.getPath());

				}

				response = this._requester.request(request);

			} catch (final MalformedURLException e) { // URLの解析エラー

				LOGGER.warning("Wrong request path (" + request.getPath() + ")");

				final HttpException err = new BadRequestException();
				response = err.createResponse(request);

			}

		}else{ // 外部プロキシを使用する場合

			response = this._requester.request(request, new InetSocketAddress(this._proxy_host, this._proxy_port));

		}
		assert response != null;

		// TODO : フィルタリングを通さずスルーする設定も追加する
		// レスポンスに対するフィルタリング
		this.filtering(response);

		// ヘッダの整理
		response.setVersion("1.1");
		this.cleanResponseHeader(response);

		LOGGER.fine("Created " + response);
		return response;

	}

	//--------------------------------------------------------------------
	//  外部プロキシの設定
	//--------------------------------------------------------------------
	/**
	 * 外部プロキシを設定する．
	 * @param extProxyHost プロキシホスト名
	 * @param extProxyPort プロキシポート番号
	 */
	public void setExternalProxy(final String extProxyHost, final int extProxyPort){
		assert extProxyHost != null;
		assert extProxyPort >= 0;

		final Pattern HOST = Pattern.compile("(.+://)?(.+)(:[0-9]+)?(/)?");
		final Matcher m = HOST.matcher(extProxyHost);
		if(m.find()){

			this._proxy_host = m.group(2);
			this._proxy_port = extProxyPort;

		}else{

			// TODO: 例外

		}

	}

	/**
	 * 外部プロキシの設定を解除する．
	 *
	 */
	public void removeExternalProxy(){

		this._proxy_host = null;

	}


	//-----------------------------------------------------------------------------
	// Observer パターン
	//-----------------------------------------------------------------------------
	/**
	 * @param observer
	 */
	public void attach(RequestFilter observer) {
		assert observer != null;

		this._requestFilters.attach(observer);

	}

	/**
	 * @param observer
	 */
	public void detach(RequestFilter observer) {
		assert observer != null;

		this._requestFilters.detach(observer);

	}

	/**
	 * @param observer
	 */
	public void attach(ResponseFilter observer) {
		assert observer != null;

		this._responseFilters.attach(observer);

	}

	/**
	 * @param observer
	 */
	public void detach(ResponseFilter observer) {
		assert observer != null;

		this._responseFilters.detach(observer);

	}

	//============================================================================
	//  private メソッド
	//============================================================================
	private void cleanRequestHeader(final Header header){
		assert header != null;

		// 許容できるエンコーディングの指定
		header.remove(HeaderName.AcceptEncoding);
		header.set(HeaderName.AcceptEncoding, "gzip, identity");

		// 持続接続を行うか
		String timeout = null;
		boolean close = false;
		if(header.containsKey(HeaderName.KeepAlive)){

			timeout = header.get(HeaderName.KeepAlive);

		}else if(header.containsValue(HeaderName.Connection, "close")){

			close = true;

		}

		// ホップバイホップヘッダの削除
		if(header.containsKey(HeaderName.Connection)){

			for(final String value : header.get(HeaderName.Connection).split(",")){

				header.remove(value.trim());

			}
			header.remove(HeaderName.Connection);

		}

		// 外部プロキシを利用する場合は削除しない
		if(this._proxy_host == null && header.containsKey(HeaderName.ProxyConnection)){

			for(final String value : header.get(HeaderName.ProxyConnection).split(",")){

				header.remove(value.trim());

			}
			header.remove(HeaderName.ProxyConnection);

		}

		// 持続接続の設定
		if(timeout != null){

			header.set(HeaderName.Connection, HeaderName.KeepAlive.toString());
			header.set(HeaderName.KeepAlive, timeout);

		}else if(close){

			header.set(HeaderName.Connection, "close");

		}

		// プロキシ通過スタンプ
		header.set(HeaderName.Via, String.format(ProxyRequestHandler.Via, this._serverName));

	}

	/**
	 * レスポンス用にヘッダを整理する．．
	 * ホップバイホップヘッダ等を削除し，オリジナルサーバ用に書き換えたヘッダを
	 * コネクションに書き出す．
	 *
	 * @param response 整理するレスポンス
	 */
	private void cleanResponseHeader(final Response response){
		assert response != null;

		final Header header = response.getHeader();

		// 持続接続を行うか
		String timeout = null;
		boolean close = false;
		if(header.containsKey(HeaderName.KeepAlive)){

			timeout = header.get(HeaderName.KeepAlive);

		}else if(header.containsValue(HeaderName.Connection, "close")){

			close = true;

		}else if(header.containsValue(HeaderName.ProxyConnection, "close")){

			close = true;

		}

		// ホップバイホップヘッダの削除
		if(header.containsKey(HeaderName.Connection)){

			for(final String value : header.get(HeaderName.Connection).split(",")){

				if(!"close".equalsIgnoreCase(value.trim())){

					header.remove(value.trim());

				}

			}
			header.remove(HeaderName.Connection);

		}
		if(header.containsKey(HeaderName.ProxyConnection)){

			for(final String value : header.get(HeaderName.ProxyConnection).split(",")){

				header.remove(value.trim());

			}
			header.remove(HeaderName.ProxyConnection);

		}

		// 持続接続の設定
		if(timeout != null){

			// TODO: 単純な追加で良いのか
			header.set(HeaderName.Connection, HeaderName.KeepAlive);
			header.set(HeaderName.KeepAlive, timeout);

		}else if(close){

			header.set(HeaderName.Connection, "close");

		}

		// プロキシ通過スタンプ
		header.set(HeaderName.Via, String.format(ProxyRequestHandler.Via, this._serverName));

	}

	private void filtering(final Request request){

		final RequestFilter.RequestInfo info = new RequestFilter.RequestInfo(request);
		this._requestFilters.notify(info);

	}

	private void filtering(final Response response){

		final ResponseFilter.ResponseInfo info = new ResponseFilter.ResponseInfo(response);
		this._responseFilters.notify(info);

		if(info.getPostTransferListeners().size() != 0){

			// Postフィルタ
			try{

				final IOStreams s = response.getBody().getIOStreams();
				_executors.execute(new Runnable(){

					@Override
					public void run() {

						final CopyingOutputStream out = new CopyingOutputStream(s.out);

						try{

							final byte[] buffer = new byte[BufferSize];
							int n = -1;
							while((n = s.in.read(buffer)) != -1){

								// ボディの書き出し
								out.write(buffer, 0, n);
								out.flush();

							}
							out.flush();

						}catch(IOException e){

							e.printStackTrace();

						}

						// フィルタの実行
						final byte[] copy = out.copy();
						for(final TransferredListener l : info.getPostTransferListeners()){

							l.update(new ByteArrayInputStream(copy), copy.length);

						}

					}

				});

			}catch(IOException e){

				e.printStackTrace();

			}

		}

	}

}
