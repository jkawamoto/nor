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
import java.util.logging.Level;

import nor.http.server.HttpRequestHandler;
import nor.http.server.HttpServer;
import nor.network.SelectionWorker;
import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
public class HttpNServer implements HttpServer{

	public static final String VERSION = "1.1";

	/**
	 * Httpリクエストに答えるハンドラ
	 */
	private final HttpRequestHandler handler;

	private SelectionWorker selection;
	private PortListener listener;


	private Thread selectionThread;
	private ConnectionManager workerThreadManager;

	private static final Logger LOGGER =  Logger.getLogger(HttpNServer.class);

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

		/*
		 * If a server is already working, close it.
		 */
		if(this.selection != null || this.workerThreadManager != null){

			this.close();

		}

		this.selection = new SelectionWorker();

		this.workerThreadManager = new ConnectionManager(this.handler, NServer.MinimusThreads, NServer.Timeout);
		this.listener = new PortListener(hostname, port, this.workerThreadManager, this.selection);

		this.selectionThread = new Thread(this.selection);
		this.selectionThread.start();

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
	public void close(){
		LOGGER.entering("close");

		this.listener.close();

		if(this.selection != null){

			this.selection.close();
			this.selectionThread.interrupt();
			try {

				this.selectionThread.join();

			} catch (final InterruptedException e) {

				LOGGER.catched(Level.WARNING, "close", e);

			}

			this.selection = null;

		}

		if(this.workerThreadManager != null){

			this.workerThreadManager.close();
			this.workerThreadManager = null;

		}

		LOGGER.exiting("close");
	}

}
