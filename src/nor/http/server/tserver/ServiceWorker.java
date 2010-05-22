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
package nor.http.server.tserver;

import static nor.http.HeaderName.Connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Logger;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.util.NoCloseInputStream;
import nor.util.NoCloseOutputStream;


/**
 * HTTP要求に答えるためのワーカークラス．
 * HTTP要求が来ると，別スレッドを起動して答える．このクラスはスレッドを起動して
 * 要求に答える一連のプロセスを行う．
 *
 */
final class ServiceWorker implements Runnable{

	/**
	 * ソケット
	 */
	private final Socket socket;

	/**
	 * ハンドラ
	 */
	private final HttpRequestHandler handler;

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(ServiceWorker.class.getName());


	/**
	 * デフォルトのタイムアウト時間
	 */
	private static final int DEFAULT_TIMEOUT = 6000;

	private static int Timeout = DEFAULT_TIMEOUT;

	/**
	 * スレッドを作成する．
	 *
	 * @param socket このクラスが答えるべき要求ソケット
	 */
	ServiceWorker(final Socket socket, final HttpRequestHandler handler){
		LOGGER.entering(ServiceWorker.class.getName(), "<init>", new Object[]{socket, handler});
		assert socket != null;
		assert handler != null;

		this.socket = socket;
		this.handler = handler;

		LOGGER.exiting(ServiceWorker.class.getName(), "<init>");
	}

	/**
	 * 要求に答える．
	 */
	@Override
	public void run() {
		LOGGER.entering(ServiceWorker.class.getName(), "run");

		LOGGER.fine("ソケットが新しい要求を受理");
		try{

			// KeepAliveの設定
			this.socket.setKeepAlive(true);
			this.socket.setSoTimeout(0);

			// ストリームの取得
			final InputStream input = new BufferedInputStream(new NoCloseInputStream(this.socket.getInputStream()), this.socket.getReceiveBufferSize());
			final OutputStream output = new BufferedOutputStream(new NoCloseOutputStream(this.socket.getOutputStream()), this.socket.getSendBufferSize());

//			try{

				// 切断要求が来るまで持続接続する
				boolean keepAlive = true;
				String prefix = "";
				while(keepAlive){

					// TODO: パイプライン化に対応。作れるだけリクエストを作成してキューに入れる．

					// リクエストオブジェクト
					this.socket.setSoTimeout(ServiceWorker.Timeout);
					// リクエストオブジェクト
					final HttpRequest request = HttpRequest.create(input, prefix);
					if(request == null){

						break;

					}
					this.socket.setSoTimeout(0);

					// スレッドの名称を変更
					Thread.currentThread().setName(request.getHeadLine());

					// Accept-Encodingを調べる, chunkedが可能なのか、gzipが可能なのか、


					/* テスト用にここで、CONNECTメソッドが来た場合はSSLコネクションを確立させる。
					 */
					//					if("CONNECT".equals(request.getMethod())){
					//
					//						final ConnectHandler.Result result = _connect.doConnect(request, input, output);
					//						if(result.isEnd()){
					//
					//							break;
					//
					//						}else{
					//
					//							input = result.getInputStream();
					//							output = result.getOutputStream();
					//							prefix = result.getPrefix();
					//							request = new HttpRequest(input, prefix);
					//
					//						}
					//
					//					}

					// リクエストに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(request);

					// リクエストの実行
					final HttpResponse response = this.handler.doRequest(request);

					// レスポンスに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(response);

					// レスポンスの書き出し
					this.socket.setSoTimeout(5000);
					response.writeOut(output);
					this.socket.setSoTimeout(0);

					this.socket.getOutputStream().flush();

				}

//			}catch(final HttpError e){
//
//				// TODO: リクエストが作成できない場合，outputへBadRequestを送る
//				final ErrorStatus status = e.getStatus();
//				final StringBuilder header = new StringBuilder();
//				header.append(String.format("HTTP/1.1 %d %s\n", status.getCode() , status.getMessage()));
//				header.append("Content-Type: text/html; charset=utf-8\n");
//				header.append("Server: arthra\n");
//				header.append("Content-Length: 0\n");
//				header.append("Connection: close\n");
//				header.append(String.format("Cause: %s\n", e.getMessage()));
//				header.append("\n");
//
//				Stream.copy(new ByteArrayInputStream(header.toString().getBytes()), output);
//
//			}

		}catch(final SocketTimeoutException e){

			LOGGER.fine(e.getLocalizedMessage());

		}catch(final IOException e){

			LOGGER.warning(e.getLocalizedMessage());

		}finally{

			// socketの削除に関して責任がある
			if(!this.socket.isClosed()){

				try {

					// ソケットを閉じる
					this.socket.close();

				} catch (IOException e) {

					LOGGER.warning("ソケットのクローズエラー");

				}

			}

			// スレッドの名称を変更
			Thread.currentThread().setName("Sleep");

		}

		LOGGER.fine("要求を完了しました");
		LOGGER.exiting(ServiceWorker.class.getName(), "run");
	}

	private boolean isKeepingAlive(final HttpRequest request){

		return !request.getHeader().containsValue(Connection, "close");

	}

	private boolean isKeepingAlive(final HttpResponse response){

		return !response.getHeader().containsValue(Connection, "close");

	}

	/**
	 *
	 * @param timeout
	 */
	static void setTimeout(final int timeout){

		ServiceWorker.Timeout = timeout;

	}

}
