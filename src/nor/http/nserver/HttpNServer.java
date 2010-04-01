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
package nor.http.nserver;

import java.io.IOException;

import nor.http.server.HttpRequestHandler;
import nor.http.server.HttpServer;
import nor.util.log.LoggedObject;

public class HttpNServer extends LoggedObject implements HttpServer{

	/**
	 * Httpリクエストに答えるハンドラ
	 */
	private final HttpRequestHandler handler;

	private ListenWorker listener;
	private Thread listenThread;

	private final int minThreads = 4;
	private final int queueSize = 3;
	private final int waitTime = 6000;


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
		entering("<init>", handler);
		assert handler != null;

		// ハンドラの登録
		this.handler = handler;

		exiting("<init>");
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
		entering("start", hostname, port);

		this.listener = new ListenWorker(hostname, port, this.handler, this.minThreads, this.queueSize, this.waitTime);
		this.listenThread = new Thread(this.listener);
		this.listenThread.start();

		exiting("service");
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
		entering("close");

		this.listener.close();
		try {

			this.listenThread.join();

		} catch (InterruptedException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}

		exiting("close");
	}

}
