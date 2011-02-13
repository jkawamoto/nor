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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nor.http.server.HttpRequestHandler;
import nor.util.log.Logger;


/**
 * ポートリスンを行うスレッドクラス．
 */
final class ListenWorker implements Runnable{

	/**
	 * Httpサーバが利用するソケット
	 */
	private final ServerSocket socket;

	/**
	 * ハンドラ
	 */
	private final HttpRequestHandler handler;

	/**
	 * スレッドプール
	 */
	private final ExecutorService pool;

	private static final Logger LOGGER = Logger.getLogger(ListenWorker.class);

	/**
	 * 指定されたポリシで動作するリスンスレッドクラスを作成する．
	 * @param policy 接続要求応答ポリシ
	 */
	ListenWorker(final ServerSocket socket, final HttpRequestHandler handler, final int nThreads){
		LOGGER.entering("<init>", socket, handler, nThreads);
		assert socket != null;
		assert handler != null;
		assert nThreads >= 0;

		this.socket = socket;
		this.handler = handler;

		// サービススレッド数の設定
		if(nThreads == 0){

			this.pool = Executors.newCachedThreadPool();

		}else{

			this.pool = Executors.newFixedThreadPool(nThreads);

		}

		LOGGER.exiting("<init>");
	}

	/**
	 * 要求に答える．
	 */
	@Override
	public void run() {
		LOGGER.entering("run");

		try{

			// 接続要求を待つ
			Socket socket;
			while((socket = this.socket.accept()) != null && !Thread.currentThread().isInterrupted()){

				this.pool.execute(new ServiceWorker(socket, this.handler));

			}

			// 終了処理
			this.socket.close();

		}catch(final SocketException e){

			LOGGER.info("run", "サーバの待ち受けが終了しました");

		}catch(final IOException e){

			LOGGER.severe("run", e.getMessage());

		}finally{

			this.pool.shutdownNow();
			try{

				this.pool.awaitTermination(60, TimeUnit.SECONDS);

			}catch(final InterruptedException e) {

				this.pool.shutdownNow();
				Thread.currentThread().interrupt();

			}

		}

		LOGGER.exiting("run");
	}

}


