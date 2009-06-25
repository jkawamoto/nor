/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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
package nor.http.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import nor.http.BadMessageException;
import nor.http.Request;
import nor.http.Response;
import nor.http.error.BadRequestException;
import nor.http.error.HttpException;
import nor.util.NoCloseOutputStream;

/**
 * HTTPサーバ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HttpServer implements Closeable{

	/**
	 * 待ち受けポート
	 */
	private final int _port;

	/**
	 * Httpリクエストに答えるハンドラ
	 */
	private final RequestHandler _handler;

	/**
	 * ポートリスニング用スレッドワーカー
	 */
	private ListenWorker _listener = null;

	/**
	 * デフォルトのタイムアウト時間
	 */
	private static final int Timeout = 300;

	/**
	 * サーバのバージョン
	 */
	public static final String VERSION = "1.1";

	/**
	 * HTTPリクエストを処理するスレッドプール
	 */
	private final ExecutorService _pool;


	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(HttpServer.class.getName());


	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * 待ち受けポートとリクエストハンドラ，スレッド数を指定してHTTPサーバーを作成する．
	 * インスタンスを作成しただけではサービスは開始されず，doServiceメソッドを呼ぶ必要がある．
	 *
	 * @param port サーバの待ち受けポート
	 * @param handler HTTPメッセージのハンドラ
	 * @param nThreds 同時に処理するリクエストの数
	 */
	public HttpServer(final int port, final RequestHandler handler, final int nThreds){
		assert port > 0;
		assert handler != null;
		assert nThreds > 0;

		this._port = port;
		this._handler = handler;

		// リンスソケット用のスレッドを一つ追加する
		//this._pool = Executors.newFixedThreadPool(nThreds + 1);
		this._pool = Executors.newCachedThreadPool();


	}


	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * 待ち受けを開始する．
	 * サーバは別スレッドとして起動するため，このメソッドはノンブロックである．
	 * サーバを終了する場合はcloseメソッドを呼ぶ．
	 *
	 * @throws IOException I/Oエラーが発生した場合
	 */
	public void doService() throws IOException{

		// ソケットの作成
		final ServerSocket socket = new ServerSocket();
		LOGGER.info("Created new server socket.");

		socket.setReuseAddress(true);

		final SocketAddress addr = new InetSocketAddress(this._port);
		socket.bind(addr);
		LOGGER.info("Binded the socket to " + addr);


		this._listener = new ListenWorker(socket);
		this._pool.execute(this._listener);

	}

	/**
	 * サーバ処理を終了する．
	 * サーバが使用していたリソースを解放するために必ず呼ぶ必要がある．
	 * 既にクローズされているサーバに対して本メソッドは何も行わない．
	 *
	 * @throws IOException サーバソケットを閉じている時にI/Oエラーが起きた場合
	 */
	@Override
	public void close() throws IOException{

		if(this._listener != null){

			this._listener.close();
			this._pool.shutdown();


		}

	}

	//============================================================================
	//  private クラス
	//============================================================================
	/**
	 * ポートリスンを行うワーカークラス．
	 */
	private final class ListenWorker implements Runnable{

		/**
		 * 待ち受けソケット
		 */
		private final ServerSocket _serverSocket;

		public ListenWorker(final ServerSocket socket){
			assert socket != null;

			this._serverSocket = socket;

		}

		/**
		 * 要求に答える．
		 */
		@Override
		public void run() {

			// スレッド名称の変更
			Thread.currentThread().setName(this.getClass().getSimpleName());


			LOGGER.info("Start listening " + this._serverSocket);
			try{

				// 接続要求を待つ
				for (Socket socket = this._serverSocket.accept(); socket != null; socket = this._serverSocket.accept()) {

					_pool.execute(new ServiceWorker(socket));

				}

			}catch(SocketException e){

				LOGGER.info("Stoped the listening " + this._serverSocket + " (caused by " + e.getLocalizedMessage() + ")");

			}catch(IOException e){

				LOGGER.severe("Stoped the listening " + this._serverSocket + " (caused by " + e.getLocalizedMessage() + ")");

			}

		}

		/**
		 * ソケットを閉じて待ち受けを終了する．
		 * @throws IOException IOエラーが発生した場合
		 */
		public void close() throws IOException{

			this._serverSocket.close();
			LOGGER.info("Closed " + this._serverSocket);

		}

	}

	/**
	 * HTTP要求に答えるためのワーカークラス．
	 * HTTP要求が来ると，別スレッドを起動して答える．このクラスはスレッドを起動して
	 * 要求に答える一連のプロセスを行う．
	 *
	 */
	private final class ServiceWorker implements Runnable{

		/**
		 * ソケット
		 */
		private final Socket _socket;

		/**
		 * スレッドを作成する．
		 *
		 * @param socket このクラスが答えるべき要求ソケット
		 */
		public ServiceWorker(final Socket socket){
			assert socket != null;

			this._socket = socket;

		}

		/**
		 * 要求に答える．
		 */
		@Override
		public void run() {

			try{

				LOGGER.info("Respond a request from " + this._socket);

				// KeepAliveの設定
				this._socket.setKeepAlive(true);
				this._socket.setSoTimeout(Timeout);

				// 入出力ストリームの取得
				InputStream input = this._socket.getInputStream();
				OutputStream output = this._socket.getOutputStream();

				Request request = null;
				try{

					// 切断要求が来るまで持続接続する
					boolean isKeepAlive = true;

					String prefix = "";
					while(isKeepAlive){

						// リクエストオブジェクト
						request = new Request(input, prefix);
						LOGGER.info("Receieved " + request.toOnelineString() + " from " + this._socket);

						// スレッド名称の変更
						Thread.currentThread().setName(request.toString());

						// リクエストに切断要求が含まれているか
						isKeepAlive &= !request.isClosing();

						// リクエストの実行
						final Response response = _handler.doRequest(request);
						LOGGER.info("Created " + response.toOnelineString() + " of " + request.toOnelineString());

						// レスポンスに切断要求が含まれているか
						isKeepAlive &= !response.isClosing();

						// レスポンスの書き出し
						response.writeMessage(new NoCloseOutputStream(output));
						LOGGER.info("Sended " + response.toOnelineString() + " of " + request.toOnelineString());

					}

				}catch(final BadMessageException e){

					// TODO: タイムアウトなのかどうかの判定
					//LOGGER.warning(e.getLocalizedMessage());

					final Response response = HttpException.CreateResponse(request, new BadRequestException());
					response.writeMessage(output);

				}catch(SocketTimeoutException e){

					// 正常なフロー
					LOGGER.info(this._socket + "  is timeout (" + e.getLocalizedMessage() + ")");

				}catch(SocketException e){

					// 突然切断されることもある。
					LOGGER.warning("SocketException is occured for " + this._socket + " : " + e.getLocalizedMessage());

				}finally{

					input.close();
					output.close();

				}

			}catch(IOException e){

				LOGGER.severe(e.getLocalizedMessage());

			}catch(Throwable e){

				//LOGGER.severe(e.getLocalizedMessage());
				e.printStackTrace();

			}finally{

				if(!this._socket.isClosed()){

					try {

						// シャットダウン

						this._socket.shutdownInput();
						this._socket.shutdownOutput();

						this._socket.close();

					} catch (IOException e) {

						LOGGER.warning("Cannot close " + this._socket);

					}

				}

				// スレッドの名称を変更
				Thread.currentThread().setName("Sleep");
				LOGGER.info("Closed " + this._socket);


			}

		}

	}


}
