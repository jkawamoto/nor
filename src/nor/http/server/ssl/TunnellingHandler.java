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
package nor.http.server.ssl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import nor.http.HttpError;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

public class TunnellingHandler implements ConnectHandler{

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(TunnellingHandler.class.getName());

	private static final Pattern ADDRESS = Pattern.compile("([^:]+):([0-9]+)");


	public Result doConnect(final HttpRequest request, final InputStream input, final OutputStream output) throws IOException {
		LOGGER.entering(TunnellingHandler.class.getName(), "doConnect", new Object[]{request, input, output});
		assert request != null;
		assert input != null;
		assert output != null;

		final String path = request.getPath();
		final Matcher m = ADDRESS.matcher(path);

		if(m.find()){

			try{
				SSLContext ctx = SSLContext.getDefault();
				SSLSocketFactory factory = ctx.getSocketFactory();

				final String host = m.group(1);
				final int port = Integer.parseInt(m.group(2), 10);

				SSLSocket socket = (SSLSocket)factory.createSocket(host, port);
				socket.startHandshake();

				// TODO: プロキシの設定は認証が必要な場合もあるので、BASIC認証設定も追加する

				// TODO: さらに外側にプロキシがいる場合、CONNECTメソッドを呼ぶ必要がある
				final StringBuilder header = new StringBuilder();
				header.append("HTTP/1.1 200 Connection established\n");
				header.append("Proxy-agent: arthra/1.0\n");
				header.append("\n");

				try{
					final HttpResponse ret = request.createResponse(new ByteArrayInputStream(header.toString().getBytes()));
					ret.writeOut(output);

					InputStream netIn = socket.getInputStream();
					OutputStream netOut = socket.getOutputStream();

					while(true){

						byte[] buf = new byte[1024];

						// TODO: 書き換え
						int inRes = input.read(buf);
						while(inRes != 0 || inRes != -1){

							netOut.write(buf);
							inRes = input.read(buf);

						}

						int outRes = netIn.read(buf);
						while(outRes != 0 || outRes != -1){

							output.write(buf);
							outRes = netIn.read(buf);

						}

						if(inRes == -1 || outRes == -1){

							socket.shutdownInput();
							socket.shutdownOutput();

							break;

						}

					}

				} catch (HttpError e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}finally{

				}
			} catch (NoSuchAlgorithmException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}finally{

			}

		}else{



		}

		final Result result = new Result();

		LOGGER.exiting(TunnellingHandler.class.getName(), "doConnect", result);
		return result;

	}
}


