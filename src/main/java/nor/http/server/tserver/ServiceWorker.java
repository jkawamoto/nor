/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.error.HttpException;
import nor.http.server.HttpRequestHandler;
import nor.util.io.NoCloseInputStream;
import nor.util.io.NoCloseOutputStream;
import nor.util.log.Logger;


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
	private static final Logger LOGGER = Logger.getLogger(ServiceWorker.class);

	/**
	 * スレッドを作成する．
	 *
	 * @param socket このクラスが答えるべき要求ソケット
	 */
	ServiceWorker(final Socket socket, final HttpRequestHandler handler){
		LOGGER.entering("<init>", socket, handler);
		assert socket != null;
		assert handler != null;

		this.socket = socket;
		this.handler = handler;

		LOGGER.exiting("<init>");
	}

	/**
	 * 要求に答える．
	 */
	@Override
	public void run() {
		LOGGER.entering("run");

		try{

			// KeepAliveの設定
			this.socket.setKeepAlive(true);
			this.socket.setSoTimeout(0);

			// ストリームの取得
			final InputStream input = new BufferedInputStream(new NoCloseInputStream(this.socket.getInputStream()), this.socket.getReceiveBufferSize());
			final OutputStream output = new BufferedOutputStream(new NoCloseOutputStream(this.socket.getOutputStream()), this.socket.getSendBufferSize());

			// 切断要求が来るまで持続接続する
			boolean keepAlive = true;
			while(keepAlive && !Thread.currentThread().isInterrupted()){

				// リクエストオブジェクト
				this.socket.setSoTimeout(TServer.Timeout);
				// リクエストオブジェクト
				final HttpRequest request = HttpRequest.create(input);
				if(request == null){

					break;

				}
				this.socket.setSoTimeout(0);

				// スレッドの名称を変更
				Thread.currentThread().setName(request.getHeadLine());

				// リクエストに切断要求が含まれているか
				keepAlive &= this.isKeepingAlive(request);

				try{

					// リクエストの実行
					final HttpResponse response = this.handler.doRequest(request);

					// レスポンスに切断要求が含まれているか
					keepAlive &= this.isKeepingAlive(response);

					// レスポンスの書き出し
					response.writeTo(output);

				}catch(final HttpException e){

					final HttpResponse response = e.createResponse(request);
					response.writeTo(output);
					output.flush();

				}

				this.socket.setSoTimeout(0);
				this.socket.getOutputStream().flush();

			}

		}catch(final SocketTimeoutException e){

			LOGGER.fine("run", e.getMessage());

		}catch(final IOException e){

			LOGGER.warning("run", e.getMessage());

		}finally{

			// socketの削除に関して責任がある
			if(!this.socket.isClosed()){

				try {

					// ソケットを閉じる
					this.socket.close();

				} catch (IOException e) {

					LOGGER.warning("run", "Cannot close the socket.");

				}

			}

			// スレッドの名称を変更
			Thread.currentThread().setName("Sleep");

		}

		LOGGER.exiting("run");
	}

	private boolean isKeepingAlive(final HttpRequest request){

		return !request.getHeader().containsValue(Connection, "close");

	}

	private boolean isKeepingAlive(final HttpResponse response){

		return !response.getHeader().containsValue(Connection, "close");

	}

}
