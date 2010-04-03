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
// $Id: Lotte.java 453 2010-04-01 10:58:43Z kawamoto $
package nor.core.proxy;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

import nor.core.plugin.Plugin;
import nor.http.nserver.HttpNServer;
import nor.http.server.HttpServer;
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
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(ProxyServer.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	public ProxyServer(final String name){
		LOGGER.entering("<init>", name);
		assert name != null;

		this.handler = new ProxyHandler(name, HttpNServer.VERSION);
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

		// サーバが既に稼働中の場合停止する
		if(this.server != null){

			this.close();

		}

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

		this.handler.attach(plugin.messageHandlers());
		this.handler.attach(plugin.requestFilters());
		this.handler.attach(plugin.responseFilters());

		LOGGER.exiting("attach");
	}

	public void detach(final Plugin plugin){
		LOGGER.entering("detach", plugin);

		this.handler.detach(plugin.messageHandlers());
		this.handler.detach(plugin.requestFilters());
		this.handler.detach(plugin.responseFilters());

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

		this.handler.addRouting(pat, extProxyHost);

		LOGGER.exiting("addRouting");
	}

	/**
	 * 外部プロキシ設定を削除する．
	 * @param pat 外部プロキシ設定を削除するURLパターン
	 */
	public void removeRouting(final Pattern pat){
		LOGGER.entering("removeRouting", pat);
		assert pat != null;

		this.handler.removeRouting(pat);

		LOGGER.exiting("removeRouting");
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


