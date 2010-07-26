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
import java.net.URL;
import java.util.regex.Pattern;

import nor.core.plugin.Plugin;
import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.http.server.HttpServer;
import nor.http.server.local.ListResource;
import nor.http.server.local.TextResource;
import nor.http.server.nserver.HttpNServer;
import nor.http.server.proxyserver.ProxyConnectRequestHandler;
import nor.http.server.proxyserver.Router;
import nor.http.server.tserver.HttpTServer;
import nor.util.log.Logger;

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
	private final ProxyHandler proxy;

	/**
	 *
	 */
	private final LocalHandler local;

	/**
	 * 外部プロキシルーティングテーブル
	 */
	private final Router router;

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(ProxyServer.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	public ProxyServer(final String name, final Router router){
		this(name, router, false);
	}

	public ProxyServer(final String name, final Router router, final boolean useTServer){
		LOGGER.entering("<init>", name, router, useTServer);
		assert name != null;
		assert router != null;

		this.router = router;
		this.proxy = new ProxyHandler(name, HttpNServer.VERSION, this.router);
		this.local = new LocalHandler();

		this.proxy.attach(this.local);

		if(useTServer){

			this.server = new HttpTServer(this.proxy);

		}else{

			this.server = new HttpNServer(this.proxy, new ProxyConnectRequestHandler(this.router));

		}
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
		this.local.getRoot().add(new TextResource("/nor/core/proxy.pac", this.getPAC(hostname, port), "application/x-javascript-config"));

//		final LocalContentsHandler h  =new LocalContentsHandler();
//		h.put("/nor/core/proxy.pac", new Contents(this.getPAC(hostname, port), ));
//		this.proxy.attach(h);

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

	public ListResource localResourceRoot(){

		return this.local.getRoot();

	}

	//--------------------------------------------------------------------
	//  Pluginの管理
	//--------------------------------------------------------------------
	public void attach(final Plugin plugin){
		LOGGER.entering("attach", plugin);

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.proxy.attach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.proxy.attach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.proxy.attach(f);

			}

		}

		LOGGER.exiting("attach");
	}

	public void detach(final Plugin plugin){
		LOGGER.entering("detach", plugin);

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.proxy.detach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.proxy.detach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.proxy.detach(f);

			}

		}

		LOGGER.exiting("detach");
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
		for(final String pat : this.proxy.getHandlingURLPatterns()){

			filtering_rule.append("if(url.match(new RegExp(\"");
			filtering_rule.append(pat.replace("\\", "\\\\"));
			filtering_rule.append("\")) !== null){return proxy;}\n");

		}

		final StringBuilder routing_rule = new StringBuilder();
		for(final Pattern pat : this.router.keySet()){

			final InetSocketAddress addr = (InetSocketAddress)this.router.get(pat).address();
			routing_rule.append("if(url.match(new RegExp(\"");
			routing_rule.append(pat.pattern());
			routing_rule.append("\")) !== null){return \"PROXY ");
			routing_rule.append(addr.getHostName());
			routing_rule.append(":");
			routing_rule.append(addr.getPort());
			routing_rule.append("\";}\n");

		}

		final String ret =  pac_template.toString().replace("{PROXY_URL}", proxy).replace("{FILTERING_RULE}", filtering_rule.toString()).replace("{ROUTING_RULE}", routing_rule.toString());
		LOGGER.fine("getPAC", ret);

		return ret;

	}

	//====================================================================
	//  テスト用のMain
	//====================================================================
	public static void main(final String[] args){

		final Router router = new Router();
		final ProxyServer app = new ProxyServer("nor", router);
		try {

			if(args.length < 2){

				System.err.println("Usage: <bind address> <listen port> [<external proxy url>]");
				System.exit(1);

			}
			if(args.length >= 3){

				router.put(".*", new URL(args[2]));

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


