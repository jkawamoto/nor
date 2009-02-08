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
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.BadMessageException;
import nor.http.BinaryBody;
import nor.http.Body;
import nor.http.Header;
import nor.http.HeaderName;
import nor.http.Request;
import nor.http.Response;
import nor.http.TextBody;
import nor.http.error.BadRequestException;
import nor.http.error.HttpException;
import nor.http.error.InternalServerErrorException;
import nor.http.error.RequestTimeoutException;
import nor.http.server.RequestHandler;
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
	private final Subject<Request, RequestFilter> _requestFilters = BasicSubject.create();

	/**
	 * テキストレスポンスに対するフィルタ
	 */
	private final List<TextResponseFilter> _textResponseFilters = new ArrayList<TextResponseFilter>();

	/**
	 * バイナリレスポンスに対するフィルタ
	 */
	private final List<BinaryResponseFilter> _binaryResponseFilters = new ArrayList<BinaryResponseFilter>();

	/**
	 * スレッドプール
	 */
	private final ExecutorService _pool = Executors.newFixedThreadPool(20);

	/**
	 * 外部プロキシのホスト
	 */
	private String _proxy_host;

	/**
	 * 外部プロキシのポート
	 */
	private int _proxy_port;

	private final String _name;
	private final String _version;
	private static final String Via = "%s %s";

	private final Requester _requester = new Requester();

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
	public ProxyRequestHandler(final String name, final String version){

		this._name = name;
		this._version = version;

	}

	/**
	 * コンストラクタ．
	 * 外部プロキシを利用する設定の場合，プロキシの準備を行う．
	 */
	public ProxyRequestHandler(final String name, final String version, final String extProxyHost, final int extProxyPort){
		this(name, version);

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

		Response response = null;

		// リクエストに対するフィルタを実行
		this._requestFilters.notify(request);

		try{

			// ヘッダーの書き換え
			this.cleanRequestHeader(request.getHeader());

			if(this._proxy_host == null){

				// 外部プロキシを使用しない
				final URL url = new URL(request.getPath());
				if(url.getQuery() != null){

					request.setPath(String.format("%s?%s", url.getPath(), url.getQuery()));

				}else{

					request.setPath(url.getPath());

				}

				final String host = url.getHost();
				final int port = url.getPort() != -1 ? url.getPort() : 80;
				final InetSocketAddress addr = new InetSocketAddress(host, port);

				response = this._requester.request(request, addr);

			}else{

				// 外部プロキシを使用する
				response = this._requester.request(request, new InetSocketAddress(this._proxy_host, this._proxy_port));

			}

		} catch (final IOException e) {

			LOGGER.warning(String.format("IOException [%s]", e.getLocalizedMessage()));
			response = HttpException.CreateResponse(request, new InternalServerErrorException());

			e.printStackTrace();

		} catch (final BadMessageException e) {

			LOGGER.warning(String.format("Bad Request [%s]", e.getLocalizedMessage()));
			response = HttpException.CreateResponse(request, new BadRequestException());

		}
		if(response == null){

			LOGGER.warning(String.format("Timeout"));
			response = HttpException.CreateResponse(request, new RequestTimeoutException());

		}
		// TODO : フィルタリングを通さずスルーする設定も追加する

		// レスポンスボディに対するフィルタリング
		final Body body = response.getBody();
		if(body instanceof TextBody){

			this.filtering(request, response.getHeader(), (TextBody)body);

		}else if(body instanceof BinaryBody){

			this.filtering(request, response.getHeader(), (BinaryBody)body);

		}

		// ヘッダの整理
		response.setVersion("1.1");
		this.cleanResponseHeader(response);

		LOGGER.info(response.toString());
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
	public void attach(TextResponseFilter observer) {
		assert observer != null;

		this._textResponseFilters.add(observer);

	}

	/**
	 * @param observer
	 */
	public void detach(TextResponseFilter observer) {
		assert observer != null;

		this._textResponseFilters.remove(observer);

	}

	/**
	 * @param observer
	 */
	public void attach(BinaryResponseFilter observer) {
		assert observer != null;

		this._binaryResponseFilters.add(observer);

	}

	/**
	 * @param observer
	 */
	public void detach(BinaryResponseFilter observer) {
		assert observer != null;

		this._binaryResponseFilters.remove(observer);

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
		header.set(HeaderName.Via, String.format(ProxyRequestHandler.Via, this._version, this._name));

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
		header.set(HeaderName.Via, String.format(ProxyRequestHandler.Via, this._version, this._name));

	}



	private void filtering(final Request request, final Header header, final TextBody body){

		final String url = request.getPath();
		for(final TextResponseFilter filter : this._textResponseFilters){

			if(filter.isFiltering(url)){

				try {

					final Runnable woker = new ResponseFilterWorker<TextBody.Streams, TextResponseFilter>(filter, request, header, body.getStreams());
					this._pool.execute(woker);

				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}

		}

	}

	private void filtering(final Request request, final Header header, final BinaryBody body){

		final String url = request.getPath();
		for(final BinaryResponseFilter filter : this._binaryResponseFilters){

			if(filter.isFiltering(url)){

				try {

					final Runnable woker = new ResponseFilterWorker<BinaryBody.Streams, BinaryResponseFilter>(filter, request, header, body.getStreams());
					this._pool.execute(woker);

				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}

		}

	}

}
