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
package nor.core.proxy;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.core.proxy.filter.EditingByteFilter;
import nor.core.proxy.filter.EditingStringFilter;
import nor.core.proxy.filter.MessageFilter;
import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.ReadonlyByteFilter;
import nor.core.proxy.filter.ReadonlyStringFilter;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.http.HeaderName;
import nor.http.HttpBody;
import nor.http.HttpHeader;
import nor.http.HttpMessage;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.http.server.proxyserver.ProxyRequestHandler;
import nor.http.server.proxyserver.Router;
import nor.util.log.EasyLogger;

/**
 * nor システム用 HTTP リクエストハンドラ
 *
 * @author Junpei Kawamoto
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
	public ProxyHandler(final String name, final String version, final Router router){
		LOGGER.entering("<init>", name, version, router);
		assert name != null;
		assert version != null;
		assert router != null;

		this.proxyHandler = new ProxyRequestHandler(name, version, router);

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

			final Matcher url = h.getHandlingURL().matcher(path);
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

	public String[] getHandlingURLPatterns(){

		final List<String> pats = new ArrayList<String>();
		for(final RequestFilter f : this.requestFilters){

			pats.add(f.getFilteringURL().pattern());

		}
		for(final ResponseFilter f : this.responseFilters){

			pats.add(f.getFilteringURL().pattern());

		}
		for(final MessageHandler h : this.handlers){

			pats.add(h.getHandlingURL().pattern());

		}

		return pats.toArray(new String[0]);

	}

	//--------------------------------------------------------------------
	//  Requestハンドラの管理
	//--------------------------------------------------------------------
	/**
	 * メッセージハンドラを追加する．
	 *
	 * @param handlers 追加するメッセージハンドラ
	 */
	public void attach(final MessageHandler handler){
		LOGGER.entering("attach", handler);

		if(handler != null){

			this.handlers.add(handler);

		}

		LOGGER.exiting("attach");
	}

	/**
	 * メッセージハンドラを削除する．
	 *
	 * @param handlers 削除するメッセージハンドラ
	 */
	public void detach(final MessageHandler handler){
		LOGGER.entering("detach", handler);

		if(handler != null){

			this.handlers.remove(handler);

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
	public void attach(final RequestFilter filter) {
		LOGGER.entering("attach", filter);

		if(filter != null){

			this.requestFilters.add(filter);

		}

		LOGGER.exiting("attach");
	}

	/**
	 * リクエストフィルタを削除する．
	 *
	 * @param filter 削除するリクエストフィルタ
	 */
	public void detach(final RequestFilter filter) {
		LOGGER.entering("detach", filter);

		if(filter != null){

			this.requestFilters.remove(filter);

		}

		LOGGER.exiting("detach");
	}

	//--------------------------------------------------------------------
	//  ResponseFilterの管理
	//--------------------------------------------------------------------
	/**
	 * レスポンスフィルタを追加する．
	 *
	 * @param filter 追加するレスポンスフィルタ
	 */
	public void attach(final ResponseFilter filter) {
		LOGGER.entering("attach", filter);

		if(filter != null){

			this.responseFilters.add(filter);

		}

		LOGGER.exiting("attach");
	}

	/**
	 * レスポンスフィルタを削除する．
	 *
	 * @param filter 削除するレスポンスフィルタ
	 */
	public void detach(final ResponseFilter filter) {
		LOGGER.entering("detach", filter);

		if(filter != null){

			this.responseFilters.remove(filter);

		}

		LOGGER.exiting("detach");
	}

	//====================================================================
	// private static メソッド
	//====================================================================
	private static <Message extends HttpMessage, Filter extends MessageFilter<Message>> void doFiltering(final Message msg, final Collection<Filter> filters){

		// 文字コードの取得
		Charset charset = null;
		final HttpHeader header = msg.getHeader();
		if(header.containsKey(HeaderName.ContentType)){

			final Pattern pat = Pattern.compile("charset=(\\S+)");
			final Matcher m = pat.matcher(header.get(HeaderName.ContentType));

			if(m.find()){

				try{

					charset = Charset.forName(m.group(1).toLowerCase());

				}catch(final UnsupportedCharsetException e){

					LOGGER.warning(e.getMessage());

				}catch(final IllegalCharsetNameException e){

					LOGGER.warning(e.getMessage());

				}

			}

		}

		// メッセージフィルタに対してメッセージボディフィルタが必要か尋ねる
		final FilterRegisterImpl register = new FilterRegisterImpl();
		final String path = msg.getPath();
		for(final MessageFilter<Message> f : filters){

			if(header.containsKey("x-nor-nofilter")){
				break;
			}

			final Matcher url = f.getFilteringURL().matcher(path);
			if(url.find()){

				if(header.containsKey(HeaderName.ContentType)){

					final Matcher cType = f.getFilteringContentType().matcher(header.get(HeaderName.ContentType));
					if(cType.find()){

						f.update(msg, url, cType, register);

					}

				}else{

					f.update(msg, url, null, register);

				}

			}

		}

		// フィルタリング要求があった場合，入力ストリームにフィルタを接続 (バイナリストリーム > テキストストリームの順)
		final HttpBody body = msg.getBody();
		InputStream in = body.getStream();


		final List<EditingByteFilter> editingByteFilters = register.getEditingByteFilters();
		final List<ReadonlyByteFilter> readonlyByteFilters = register.getReadonlyByteFilters();
		if(editingByteFilters.size() != 0 || readonlyByteFilters.size() != 0){

			in = new FilteringByteInputStream(body.getStream(), editingByteFilters, readonlyByteFilters);

		}

		final List<EditingStringFilter> editingStringFilters = register.getEditingStringFilters();
		final List<ReadonlyStringFilter> readonlyStringFilters = register.getReadonlyStringFilters();
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

			// テキストフィルタリングを行った場合，データサイズは不明になる
			if(header.containsKey(HeaderName.ContentLength)){

				header.set("x-nor-old-content-length", header.get(HeaderName.ContentLength));
				header.remove(HeaderName.ContentLength);
				header.add(HeaderName.TransferEncoding, "chunked");
				header.set(HeaderName.ContentEncoding, "gzip");

			}

		}
		msg.getBody().setStream(in);


		if(header.containsKey(HeaderName.ContentLength)){

			// 書き込みを行うフィルタがあった場合，ContentLengthは分からなくなる
			if(!register.readonly()){

					header.set("x-nor-old-content-length", header.get(HeaderName.ContentLength));
					header.remove(HeaderName.ContentLength);
					header.set(HeaderName.TransferEncoding, "chunked");
					header.set(HeaderName.ContentEncoding, "gzip");

			}

			// 内容コーディングが指定されている場合，最終的なデータサイズが不明のためチャンク形式にする
			if(header.containsKey(HeaderName.ContentEncoding)){

				header.set("x-nor-old-content-length", header.get(HeaderName.ContentLength));
				header.remove(HeaderName.ContentLength);
				header.add(HeaderName.TransferEncoding, "chunked");

			}

		}

	}

}
