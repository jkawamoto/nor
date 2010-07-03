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
package nor.http.server.nserver;

import java.io.IOException;

import nor.http.server.HttpRequestHandler;
import nor.http.server.HttpServer;
import nor.util.log.EasyLogger;

public class HttpNServer implements HttpServer{

	public static final String VERSION = "1.1";

	/**
	 * Httpリクエストに答えるハンドラ
	 */
	private final HttpRequestHandler handler;

	private ListenWorker listener;
	private Thread listenThread;

	private final int minThreads;
	private final int queueSize;
	private final int timeout;

	private static final EasyLogger LOGGER =  EasyLogger.getLogger(HttpNServer.class);

	//============================================================================
	//  Constants
	//============================================================================
	private static final String KeyTemplate = "%s.%s";
	private static final String MinThreadsKey = "MinimusThreads";
	private static final String QueueSizeKey = "QuerySize";
	private static final String TimeoutKey = "Timeout";

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * リクエストハンドラを指定して Http サーバを作成する．
	 * インスタンスを作成しただけではサービスは開始されず，start メソッドを呼ぶ必要がある．
	 * また，サービスを停止する場合には close メソッドを呼ぶ．
	 *
	 * @param handler Http メッセージのハンドラ
	 * @see #start(String, int, int)
	 * @see #close()
	 */
	public HttpNServer(final HttpRequestHandler handler){
		LOGGER.entering("<init>", handler);
		assert handler != null;

		// ハンドラの登録
		this.handler = handler;

		final String classname = this.getClass().getName();
		this.minThreads = Integer.valueOf(System.getProperty(String.format(KeyTemplate, classname, MinThreadsKey)));
		this.queueSize = Integer.valueOf(System.getProperty(String.format(KeyTemplate, classname, QueueSizeKey)));
		this.timeout = Integer.valueOf(System.getProperty(String.format(KeyTemplate, classname, TimeoutKey)));

		LOGGER.exiting("<init>");
	}


	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * サービスを開始する．
	 * 別スレッドとして Http サーバを起動する．
	 *
	 * @param hostname バインドするホスト名またはIPアドレス
	 * @param port 待ち受けポート番号
	 * @throws IOException I/Oエラーが発生した場合
	 */
	@Override
	public void start(final String hostname, final int port) throws IOException{
		LOGGER.entering("start", hostname, (Object)port);

		this.listener = new ListenWorker(hostname, port, this.handler, this.minThreads, this.queueSize, this.timeout);
		this.listenThread = new Thread(this.listener);
		this.listenThread.start();

		LOGGER.exiting("service");
	}

	/**
	 *	サーバ処理を終了する．
	 *	サーバが使用していたリソースを解放するために，必ず呼ぶ必要がある．
	 *	既にクローズされているサーバに対して本メソッドを呼んだ場合，何も行わない．
	 *
	 * @throws IOException サーバソケットを閉じている時にI/Oエラーが起きた場合
	 */
	@Override
	public void close() throws IOException{
		LOGGER.entering("close");

		if(this.listener != null){

			this.listener.close();
			try {

				this.listenThread.join();

			} catch (final InterruptedException e) {

				LOGGER.throwing("close", e);

			}

		}
		LOGGER.info("Close.");

		LOGGER.exiting("close");
	}

}
