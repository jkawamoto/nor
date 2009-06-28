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

		final ProxyRequestHandler h = new ProxyRequestHandler("Nor");




		final HttpServer server = new HttpServer(9080, h, 1);

		try {

			server.doService();

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}

	}

}
