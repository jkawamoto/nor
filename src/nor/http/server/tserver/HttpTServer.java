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
//$Id: HttpTServer.java 471 2010-04-03 10:25:20Z kawamoto $
package nor.http.server.tserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import nor.http.server.HttpRequestHandler;
import nor.http.server.HttpServer;
import nor.util.log.EasyLogger;

/**
 * Httpサーバ
 *
 * @version $Rev: 471 $
 * @author KAWAMOTO Junpei
 *
 */
public class HttpTServer implements HttpServer{

	/**
	 * Httpリクエストに答えるハンドラ
	 */
	private final HttpRequestHandler handler;

	/**
	 * Connectメソッドに答えるハンドラ
	 */
//	private final ConnectHandler _connect = new DecryptHandler();

	/**
	 * ポートリスニング用スレッド
	 */
	private Thread listenThread;

	/**
	 * ポートリスニング用スレッドワーカー
	 */
	private ListenWorker listener = null;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpTServer.class);


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
	public HttpTServer(final HttpRequestHandler handler){
		LOGGER.entering("<init>", handler);
		assert handler != null;

		// ハンドラの登録
		this.handler = handler;

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

		// ソケットの作成
		final ServerSocket socket = new ServerSocket();
		socket.setReuseAddress(true);
		LOGGER.info("Create new socket.");

		socket.bind(new InetSocketAddress(hostname, port));
		LOGGER.info("Bind the socket to port " + port);

		this.listener = new ListenWorker(socket, this.handler, 0);
		LOGGER.info("Start listening.");

		this.listenThread = new Thread(this.listener);
		this.listenThread.setName("ListenWorker");
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

			} catch (InterruptedException e) {

				// TODO 自動生成された catch ブロック
				e.printStackTrace();

			}

			LOGGER.info("End.");

		}

		LOGGER.exiting("close");
	}

}
