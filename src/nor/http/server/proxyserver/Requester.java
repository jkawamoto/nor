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
package nor.http.server.proxyserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import nor.http.BadMessageException;
import nor.http.Header;
import nor.http.HeaderName;
import nor.http.Request;
import nor.http.Response;
import nor.util.NoCloseInputStream;
import nor.util.NoCloseOutputStream;

/**
 * @author KAWAMOTO Junpei
 *
 */
class Requester {

	private static final Logger LOGGER = Logger.getLogger(Requester.class.getName());

	private static final int Timeout = 300;

	public Response request(final Request request) throws IOException{
		assert request != null;

		final Header header = request.getHeader();
		final String[] sp = header.get(HeaderName.Host).split(":");

		int port = 80;
		if(sp.length > 1){

			port = Integer.valueOf(sp[1]);

		}

		final String host = sp[0];
		return this.request(request, new InetSocketAddress(host, port));

	}

	public Response request(final Request request, final InetSocketAddress address) throws IOException{

		// ソケットの取得
		Socket socket = new Socket();
		socket.setKeepAlive(true);
		socket.connect(address, Timeout);

		// 送受信
		Response response = null;

		//リトライ回数
//		for(int i = 0; i != 2; ++i){

			try{

				final OutputStream out = socket.getOutputStream();

				// リクエストの送信
				LOGGER.fine("Sending " + request.toOnelineString());
				request.writeMessage(new NoCloseOutputStream(out));

				// レスポンスの作成
				//response = request.createResponse(new NoCloseInputStream(socket.getInputStream()));
				response = request.createResponse(socket.getInputStream());

//				break;

			}catch(final IOException e){

				LOGGER.warning(e.getLocalizedMessage());
//
//				// ソケットの再作成
//				socket = new Socket();
//				socket.setKeepAlive(true);
//				socket.connect(address, Timeout);


			}catch (final BadMessageException e) {

				// ソケットがタイムアウトした可能性がある
				 LOGGER.warning(String.format("Bad Message [%s]", request.toString()));

//				// ソケットの再作成
//				socket = new Socket();
//				socket.setKeepAlive(true);
//				socket.connect(address, Timeout);

			}

//		}


		return response;

	}


}


