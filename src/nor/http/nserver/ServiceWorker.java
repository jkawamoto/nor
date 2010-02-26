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

import static nor.http.HeaderName.Connection;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.server.HttpRequestHandler;
import nor.util.log.LoggedObject;

class ServiceWorker extends LoggedObject implements Runnable, Closeable{

	private final Queue<Connection> queue;
	private final HttpRequestHandler handler;

	private boolean running = true;

	public ServiceWorker(final Queue<Connection> queue, final HttpRequestHandler handler){

		this.queue = queue;
		this.handler = handler;

	}

	/**
	 * 要求に答える．
	 */
	@Override
	public synchronized void run() {

		try{

			while(this.running){

				// スレッドの名称を変更
				Thread.currentThread().setName("Sleep");
				final Connection con = queue.remove();
				//LOGGER.fine("ソケットが新しい要求を受理");

				try{

					// ストリームの取得
					final InputStream input = new BufferedInputStream(con.getInputStream());
					final OutputStream output = new BufferedOutputStream(con.getOutputStream());

					// 切断要求が来るまで持続接続する
					boolean keepAlive = true;
					String prefix = "";
					while(keepAlive){

						// TODO: パイプライン化に対応。作れるだけリクエストを作成してキューに入れる．

						// リクエストオブジェクト
						final HttpRequest request = HttpRequest.create(input, prefix);
						if(request == null){

							break;

						}

						// スレッドの名称を変更
						Thread.currentThread().setName(request.getHeadLine());

						// リクエストに切断要求が含まれているか
						keepAlive &= this.isKeepingAlive(request);

						// リクエストの実行
						final HttpResponse response = handler.doRequest(request);

						// レスポンスに切断要求が含まれているか
						keepAlive &= this.isKeepingAlive(response);

						// レスポンスの書き出し
						response.writeOut(output);
						output.flush();

					} // Keep-Alive

					input.close();
					output.close();

				}catch(final ClosedChannelException e){

					LOGGER.warning(e.getLocalizedMessage());
					e.printStackTrace();

				}catch(final IOException e){

					LOGGER.warning(e.getLocalizedMessage());
					// e.printStackTrace();

				}finally{

					try {
						con.close();
					} catch (IOException e) {
						// TODO 自動生成された catch ブロック
						e.printStackTrace();
					}

				}

				LOGGER.fine("要求を完了しました");

			} // 1チャネル

		}catch(final InterruptedException e){

			Thread.interrupted();
			e.printStackTrace();

		}

		//LOGGER.exiting(ServiceWorker.class.getName(), "run");
	}

	private boolean isKeepingAlive(final HttpRequest request){

		return !request.getHeader().containsValue(Connection, "close");

	}

	private boolean isKeepingAlive(final HttpResponse response){

		return !response.getHeader().containsValue(Connection, "close");

	}

	@Override
	public void close() throws IOException {

		this.running = false;

	}

}
