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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.Proxy.Type;
import java.util.regex.Pattern;

import nor.core.plugin.Plugin;
import nor.core.proxy.LocalContentsHandler.Contents;
import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.http.server.HttpServer;
import nor.http.server.nserver.HttpNServer;
import nor.http.server.proxyserver.Router;
import nor.util.log.EasyLogger;

/**
 * LocalProxyサブシステム．
 * Httpサーバ機能を有し，ローカルプロキシサーバとして動作する．
 * 実行中にリスンポートの変更が必要な場合，
 * 1. サービスを停止する．
 * 2. 新しいポートを引数にサービスを開始する．
 * という手順を実行する．
 *
 */
public class ProxyServer implements Closeable{

	/**
	 * 現在稼働中のHttpServer
	 */
	private final HttpServer server;

	/**
	 * リクエストハンドラ
	 */
	private final ProxyHandler handler;

	/**
	 * 外部プロキシルーティングテーブル
	 */
	private final Router router = new Router();;

	/**
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(ProxyServer.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	public ProxyServer(final String name){
		LOGGER.entering("<init>", name);
		assert name != null;

		this.handler = new ProxyHandler(name, HttpNServer.VERSION, this.router);
		this.server = new HttpNServer(this.handler);

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * サービスを開始する．
	 *
	 * @param hostname バインドするアドレス
	 * @param port 待ち受けポート
	 * @throws IOException I/Oエラーが発生した場合
	 */
	public void start(final String hostname, final int port) throws IOException{
		LOGGER.entering("start", hostname, (Object)port);
		assert hostname != null && hostname.length() != 0;
		assert port > 0;

		// PAC ファイルの登録
		final LocalContentsHandler h  =new LocalContentsHandler();
		h.put("/nor/core/proxy.pac", new Contents(this.getPAC(hostname, port), "application/x-javascript-config"));
		this.handler.attach(h);

		// サービスの開始
		this.server.start(hostname, port);

		LOGGER.exiting("start");
	}

	/**
	 * サービスを停止する．
	 *
	 * @throws IOException I/Oエラーが発生した場合
	 */
	@Override
	public void close() throws IOException{
		LOGGER.entering("close");

		this.server.close();

		LOGGER.exiting("close");
	}

	//--------------------------------------------------------------------
	//  Pluginの管理
	//--------------------------------------------------------------------
	public void attach(final Plugin plugin){
		LOGGER.entering("attach", plugin);

		LOGGER.info("Loading a plugin (" + plugin.getClass().getName() + ")");

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.handler.attach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.handler.attach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.handler.attach(f);

			}

		}

		LOGGER.exiting("attach");
	}

	public void detach(final Plugin plugin){
		LOGGER.entering("detach", plugin);

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.handler.detach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.handler.detach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.handler.detach(f);

			}

		}

		LOGGER.exiting("detach");
	}

	//--------------------------------------------------------------------
	//  外部プロキシの設定
	//--------------------------------------------------------------------
	/**
	 * 外部プロキシ設定を追加する．
	 * @param pat 外部プロキシを適用するURLパターン
	 * @param extProxyHost 外部プロキシサーバのURL
	 */
	public void addRouting(final Pattern pat, final URL extProxyHost){
		LOGGER.entering("addRouting", pat, extProxyHost);
		assert pat != null;
		assert extProxyHost != null;

		final InetSocketAddress extProxyAddr = new InetSocketAddress(extProxyHost.getHost(), extProxyHost.getPort());
		this.router.put(pat, new Proxy(Type.HTTP, extProxyAddr));

		LOGGER.exiting("addRouting");
	}

	/**
	 * 外部プロキシ設定を削除する．
	 * @param pat 外部プロキシ設定を削除するURLパターン
	 */
	public void removeRouting(final Pattern pat){
		LOGGER.entering("removeRouting", pat);
		assert pat != null;

		this.router.remove(pat);
//		this.handler.removeRouting(pat);

		LOGGER.exiting("removeRouting");
	}

	//====================================================================
	//  Private methods
	//====================================================================
	private String getPAC(final String hostname, final int port) throws IOException{

		final Class<?> c = this.getClass();
		final BufferedReader rin = new BufferedReader(new InputStreamReader(c.getResourceAsStream("res/proxy.pac.template")));
		final StringBuilder pac_template = new StringBuilder();
		for(String buf = rin.readLine(); buf != null; buf = rin.readLine()){

			pac_template.append(buf);
			pac_template.append("\n");

		}

		final String proxy = String.format("%s:%d", hostname, port);

		final StringBuilder filtering_rule = new StringBuilder();
		for(final String pat : this.handler.getHandlingURLPatterns()){

			filtering_rule.append("if(url.match(new RegExp(\"");
			filtering_rule.append(pat);
			filtering_rule.append("\"))){return proxy;}\n");

		}

		final StringBuilder routing_rule = new StringBuilder();
		for(final Pattern pat : this.router.keySet()){

			final InetSocketAddress addr = (InetSocketAddress)this.router.get(pat).address();
			routing_rule.append("if(url.match(new RegExp(\"");
			routing_rule.append(pat.pattern());
			routing_rule.append("\"))){return \"PROXY ");
			routing_rule.append(addr.getHostName());
			routing_rule.append(":");
			routing_rule.append(addr.getPort());
			routing_rule.append("\";}");

		}

		return pac_template.toString().replace("{PROXY_URL}", proxy).replace("{FILTERING_RULE}", filtering_rule.toString()).replace("{ROUTING_RULE}", routing_rule.toString());

	}

	//====================================================================
	//  テスト用のMain
	//====================================================================
	public static void main(final String[] args){

		final ProxyServer app = new ProxyServer("nor");
		try {

			if(args.length < 2){

				System.err.println("Usage: <bind address> <listen port> [<external proxy url>]");
				System.exit(1);

			}
			if(args.length >= 3){

				app.addRouting(Pattern.compile(".*"), new URL(args[2]));

			}

			// プロキシサーバの開始
			app.start(args[0], Integer.parseInt(args[1]));

			// システムの終了待ち
			System.in.read();
			app.close();

		} catch (final IOException e) {

			e.printStackTrace();

		}

	}

}


