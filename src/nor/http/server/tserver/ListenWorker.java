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

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nor.http.server.HttpRequestHandler;
import nor.util.log.EasyLogger;


/**
 * ポートリスンを行うスレッドクラス．
 */
final class ListenWorker implements Runnable, Closeable{

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

	private static final EasyLogger LOGGER = EasyLogger.getLogger(ListenWorker.class);

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
			for (Socket socket = this.socket.accept(); socket != null; socket = this.socket.accept()) {

				this.pool.execute(new ServiceWorker(socket, this.handler));

			}

		}catch(final SocketException e){

			LOGGER.info("サーバの待ち受けが終了しました");

		}catch(final IOException e){

			LOGGER.severe(e.getLocalizedMessage());
		}

		LOGGER.exiting("run");
	}

	/**
	 * ソケットを閉じて待ち受けを終了する．
	 * @throws IOException IOエラーが発生した場合
	 */
	public void close() throws IOException{
		LOGGER.entering("close");

		this.pool.shutdownNow();
		try {

			this.pool.awaitTermination(60, TimeUnit.SECONDS);

		} catch (final InterruptedException e) {

			throw new IOException(e.getMessage());

		}finally{

			this.socket.close();

		}

		LOGGER.exiting("close");
	}

}


