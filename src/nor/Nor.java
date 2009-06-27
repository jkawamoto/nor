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
package nor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nor.http.Body2.IOStreams;
import nor.http.server.HttpServer;
import nor.http.server.proxyserver.ProxyRequestHandler;
import nor.http.server.proxyserver.ResponseFilter;
import nor.http.server.proxyserver.TransferListener;

/**
 * @author KAWAMOTO Junpei
 *
 */
public class Nor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final ProxyRequestHandler h = new ProxyRequestHandler("Nor");


		h.attach(new ResponseFilter(){

			@Override
			public void update(ResponseInfo info) {

				try {

					final FileOutputStream file = new FileOutputStream("F:\\Nor\\" + info.getRequest().getPath().replaceAll("\\W", ""));
					info.addPreTransferListener(new TransferListener(){

						@Override
						public void update(InputStream in, OutputStream out) {

							int n = 0;
							byte[] buffer = new byte[10240];
							try {

								while((n = in.read(buffer)) != -1){

									file.write(buffer, 0, n);
									out.write(buffer, 0 , n);

								}

								file.close();
							} catch (IOException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
							}


						}

					});

				} catch (FileNotFoundException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
			}

		});


		final HttpServer server = new HttpServer(9080, h, 1);

		try {

			server.doService();

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

}
