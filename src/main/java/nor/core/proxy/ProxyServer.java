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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import nor.core.plugin.Plugin;
import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.http.server.HttpServer;
import nor.http.server.local.ListResource;
import nor.http.server.nserver.HttpNServer;
import nor.http.server.proxyserver.ProxyRequestHandler;
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
	private final RequestHandler remoteHandler;

	/**
	 *
	 */
	private final LocalContentsHandlerAdapter localHandler;

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
	public ProxyServer(final ProxyRequestHandler handler, final Router router){
		this(handler, router, false);
	}

	public ProxyServer(final ProxyRequestHandler handler, final Router router, final boolean useTServer){
		LOGGER.entering("<init>", handler, router, useTServer);
		assert handler != null;
		assert router != null;

		this.router = router;
		this.remoteHandler = new RequestHandler(handler);
		this.localHandler = new LocalContentsHandlerAdapter();

		this.remoteHandler.attach(this.localHandler);

		if(useTServer){

			this.server = new HttpTServer(this.remoteHandler);

		}else{

			this.server = new HttpNServer(this.remoteHandler);

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

		return this.localHandler.getRoot();

	}

	//--------------------------------------------------------------------
	//  Pluginの管理
	//--------------------------------------------------------------------
	public void attach(final Plugin plugin){
		LOGGER.entering("attach", plugin);

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.remoteHandler.attach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.remoteHandler.attach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.remoteHandler.attach(f);

			}

		}

		LOGGER.exiting("attach");
	}

	public void detach(final Plugin plugin){
		LOGGER.entering("detach", plugin);

		final MessageHandler[] hs = plugin.messageHandlers();
		if(hs != null){

			for(final MessageHandler h: hs){

				this.remoteHandler.detach(h);

			}

		}

		final RequestFilter[] rqs = plugin.requestFilters();
		if(rqs != null){

			for(final RequestFilter f: rqs){

				this.remoteHandler.detach(f);

			}

		}

		final ResponseFilter[] res = plugin.responseFilters();
		if(res != null){

			for(final ResponseFilter f: res){

				this.remoteHandler.detach(f);

			}

		}

		LOGGER.exiting("detach");
	}

	/**
	 * Get the PAC file for this proxy server.
	 *
	 * @param host The address of this server.
	 * @param port The listening port.
	 * @return A string of the PAC file.
	 * @throws IOException When some I/O exception happens.
	 */
	public String getPAC(final String host, final int port, final boolean ssh) throws IOException{
		LOGGER.entering("getPAC", host, port, ssh);

		final List<Object[]> routings = new ArrayList<Object[]>();
		for(final Pattern pat : this.router.keySet()){

			final InetSocketAddress addr = (InetSocketAddress)this.router.get(pat).address();
			routings.add(new Object[]{pat.pattern(), addr.getHostName(), addr.getPort()});

		}

		final VelocityEngine velocity = new VelocityEngine();
		velocity.init();

		final VelocityContext context = new VelocityContext();
		context.put("ssh", ssh);
		context.put("host", host);
		context.put("port", port);
		context.put("filters", this.remoteHandler.getHandlingURLPatterns());
		context.put("routings", routings);

		final Class<?> c = this.getClass();
		final InputStream in = c.getResourceAsStream("proxy.pac.vm");
		final StringWriter out = new StringWriter();
		velocity.evaluate(context, out, "", new BufferedReader(new InputStreamReader(in)));

		final String res = out.toString();
		LOGGER.exiting("getPAC", res);
		return res;
	}

	//====================================================================
	//  テスト用のMain
	//====================================================================
	public static void main(final String[] args){

		final Router router = new Router();
		final ProxyServer app = new ProxyServer(new ProxyRequestHandler("nor", router), router);
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


