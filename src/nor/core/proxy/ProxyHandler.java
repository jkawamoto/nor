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
package nor.core.proxy;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.core.proxy.filter.EditingByteFilter;
import nor.core.proxy.filter.MessageFilter;
import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.ReadonlyByteFilter;
import nor.core.proxy.filter.ReadonlyStringFilter;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.core.proxy.filter.EditingStringFilter;
import nor.http.HeaderName;
import nor.http.HttpBody;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.http.server.proxyserver.ProxyRequestHandler;
import nor.util.log.EasyLogger;

/**
 * nor システム用 HTTP リクエストハンドラ
 *
 * @author KAWAMOTO Junpei
 *
 */
class ProxyHandler implements HttpRequestHandler{

	/**
	 * Httpリクエストに対するフィルタ
	 */
	private final Collection<RequestFilter> requestFilters = new ArrayList<RequestFilter>();

	/**
	 * Httpレスポンスに対するフィルタ
	 */
	private final Collection<ResponseFilter> responseFilters = new ArrayList<ResponseFilter>();

	/**
	 * 特別なリクエストハンドラ
	 */
	private final Collection<MessageHandler> handlers = new ArrayList<MessageHandler>();


	/**
	 * ローカルプロキシハンドラ
	 */
	private final ProxyRequestHandler proxyHandler;

	/**
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(ProxyHandler.class);


	//====================================================================
	//  Constructer
	//====================================================================
	public ProxyHandler(final String name, final String version){
		LOGGER.entering("<init>", name, version);
		assert name != null;
		assert version != null;

		this.proxyHandler = new ProxyRequestHandler(name, version);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequestHandleable#doRequest(jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	public HttpResponse doRequest(final HttpRequest request) {
		LOGGER.entering("doRequest", request);
		assert request != null;

		HttpResponse response = null;

		// リクエストのフィルタリング
		ProxyHandler.doFiltering(request, this.requestFilters);

		// 他のハンドラがインストールされている場合はそれを実行する
		final String path = request.getPath();
		for(final MessageHandler h : this.handlers){

			final Matcher url = h.getFilteringURL().matcher(path);
			if(url.find()){

				response = h.doRequest(request, url);
				if(response != null){

					break;

				}

			}

		}

		// 他のハンドラでリクエストが処理されなかった場合はデフォルトプロキシが処理する
		if(response == null){

			response = this.proxyHandler.doRequest(request);

		}
		assert response != null;

		// レスポンスのフィルタリング
		ProxyHandler.doFiltering(response, this.responseFilters);


		LOGGER.exiting("doRequest", response);
		return response;

	}

	//--------------------------------------------------------------------
	//  Requestハンドラの管理
	//--------------------------------------------------------------------
	/**
	 * メッセージハンドラを追加する．
	 *
	 * @param handlers 追加するメッセージハンドラ
	 */
	public void attach(final MessageHandler[] handlers){
		LOGGER.entering("attach", handlers);

		if(handlers != null){

			for(final MessageHandler h : handlers){

				this.handlers.add(h);

			}

		}

		LOGGER.exiting("attach");
	}

	/**
	 * メッセージハンドラを削除する．
	 *
	 * @param handlers 削除するメッセージハンドラ
	 */
	public void detach(final MessageHandler[] handlers){
		LOGGER.entering("detach", handlers);

		if(handlers != null){

			for(final MessageHandler h : handlers){

				this.handlers.remove(h);

			}

		}

		LOGGER.exiting("detach");
	}

	//--------------------------------------------------------------------
	//  RequestFilterの管理
	//--------------------------------------------------------------------
	/**
	 * リクエストフィルタを追加する．
	 *
	 * @param observers 追加するリクエストフィルタ
	 */
	public void attach(final RequestFilter[] observers) {
		LOGGER.entering("attach", observers);

		if(observers != null){

			for(final RequestFilter f : observers){

				this.requestFilters.add(f);

			}

		}

		LOGGER.exiting("attach");
	}

	/**
	 * リクエストフィルタを削除する．
	 *
	 * @param observers 削除するリクエストフィルタ
	 */
	public void detach(final RequestFilter[] observers) {
		LOGGER.entering("detach", observers);

		if(observers != null){

			for(final RequestFilter f : observers){

				this.requestFilters.remove(f);

			}

		}

		LOGGER.exiting("detach");
	}

	//--------------------------------------------------------------------
	//  ResponseFilterの管理
	//--------------------------------------------------------------------
	/**
	 * レスポンスフィルタを追加する．
	 *
	 * @param observers 追加するレスポンスフィルタ
	 */
	public void attach(final ResponseFilter[] observers) {
		LOGGER.entering("attach", observers);

		if(observers != null){

			for(final ResponseFilter f : observers){

				this.responseFilters.add(f);

			}

		}

		LOGGER.exiting("attach");
	}

	/**
	 * レスポンスフィルタを削除する．
	 *
	 * @param observers 削除するレスポンスフィルタ
	 */
	public void detach(final ResponseFilter[] observers) {
		LOGGER.entering("detach", observers);

		if(observers != null){

			for(final ResponseFilter f : observers){

				this.responseFilters.remove(f);

			}

		}

		LOGGER.exiting("detach");
	}

	//--------------------------------------------------------------------
	//  外部プロキシの設定
	//--------------------------------------------------------------------
	/**
	 * 外部プロキシを設定する．
	 * @param extProxyHost プロキシホスト
	 * @throws MalformedURLException
	 */
	public void addRouting(final Pattern pat, final URL extProxyHost){
		LOGGER.entering("setExternalProxy", pat, extProxyHost);
		assert pat != null;
		assert extProxyHost != null;

		this.proxyHandler.addRouting(pat, extProxyHost);

		LOGGER.exiting("setExternalProxy");
	}

	/**
	 * 外部プロキシの設定を解除する．
	 *
	 */
	public void removeRouting(final Pattern pat){
		LOGGER.entering("removeExternalProxy", pat);
		assert pat != null;

		this.proxyHandler.removeRouting(pat);

		LOGGER.exiting("removeExternalProxy");
	}

	//====================================================================
	// private static メソッド
	//====================================================================
	private static <Message extends HttpMessage, Filter extends MessageFilter<Message>> void doFiltering(final Message msg, final Collection<Filter> filters){

		// 文字コードの取得
		Charset charset = null;
		final HttpHeader header = msg.getHeader();
		boolean isChar = false;
		if(header.containsKey(HeaderName.ContentType)){

			isChar = header.get(HeaderName.ContentType).contains("text");

			final Pattern pat = Pattern.compile("charset=(\\S+)");
			final Matcher m = pat.matcher(header.get(HeaderName.ContentType));

			if(m.find()){

				try{

					charset = Charset.forName(m.group(1).toLowerCase());

				}catch(final UnsupportedCharsetException e){

					LOGGER.warning(e.getMessage());

				}

			}

		}

		// メッセージフィルタに対してメッセージボディフィルタが必要か尋ねる
		final FilterRegister container = new FilterRegister();
		final String path = msg.getPath();
		for(final MessageFilter<Message> f : filters){

			if(header.containsKey("x-nor-nofilter")){
				break;
			}

			final Matcher url = f.getFilteringURL().matcher(path);
			if(url.find()){

				final Matcher cType = f.getFilteringContentType().matcher(header.get(HeaderName.ContentType));
				if(cType.matches()){

					f.update(msg, url, cType, container, isChar);

				}

			}

		}

		// フィルタリング要求があった場合，入力ストリームにフィルタを接続 (バイナリストリーム > テキストストリームの順)
		final HttpBody body = msg.getBody();
		InputStream in = body.getStream();


		final List<EditingByteFilter> editingByteFilters = container.getEditingByteFilters();
		final List<ReadonlyByteFilter> readonlyByteFilters = container.getReadonlyByteFilters();
		if(editingByteFilters.size() != 0 || readonlyByteFilters.size() != 0){

			in = new FilteringByteInputStream(body.getStream(), editingByteFilters, readonlyByteFilters);

		}

		final List<EditingStringFilter> editingStringFilters = container.getEditingStringFilters();
		final List<ReadonlyStringFilter> readonlyStringFilters = container.getReadonlyStringFilters();
		if(editingStringFilters.size() != 0 || readonlyStringFilters.size() != 0){

			if(charset == null){

				if(header.containsKey(HeaderName.ContentType)){

					final String ctype = header.get(HeaderName.ContentType);
					if(ctype.contains("html") || ctype.contains("xml")){

						final CharsetDetectingInputStream cin = new CharsetDetectingInputStream(in);
						charset = cin.getCharset();
						in = cin;

					}

				}

			}

			in = new FilteringCharacterInputStream(in, charset, editingStringFilters, readonlyStringFilters);

		}
		msg.getBody().setStream(in);

		// 書き込みを行うフィルタがあった場合，ContentLengthは分からなくなる
		if(!container.readonly()){

			if(header.containsKey(HeaderName.ContentLength)){

				header.set("x-nor-old-datasize", header.get(HeaderName.ContentLength));
				header.remove(HeaderName.ContentLength);
				header.set(HeaderName.TransferEncoding, "chunked");

			}

		}

	}

}
