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

import java.io.IOException;

import nor.http.nserver.HttpNServer;
import nor.http.server.HttpServer;
import nor.http.server.proxyserver.ProxyRequestHandler;

/**
 * @author KAWAMOTO Junpei
 *
 */
public class Nor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		final ProxyRequestHandler h = new ProxyRequestHandler("Nor", "1.0");
		final HttpServer server = new HttpNServer(h);

		try {

			server.start("127.0.0.1", 9080, 4);

			System.in.read();
			server.close();

		} catch (IOException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}

	}

}
