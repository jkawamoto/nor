/*
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import nor.http.HttpRequest;

public interface ConnectHandler {

	public Result doConnect(final HttpRequest request, final InputStream in, final OutputStream out) throws IOException;

	public class Result{

		private boolean end;

		private final String prefix;

		private final InputStream in;
		private final OutputStream out;


		public Result(){

			this.end = false;
			this.prefix = null;
			this.in = null;
			this.out = null;

		}

		public Result(final String prefix, final InputStream in, final OutputStream out){

			this.end = true;
			this.prefix = prefix;
			this.in = in;
			this.out = out;

		}

		public boolean isEnd(){

			return this.end;

		}

		public String getPrefix(){

			return this.prefix;

		}

		public InputStream getInputStream(){

			return this.in;

		}

		public OutputStream getOutputStream(){

			return this.out;

		}

	}

}


