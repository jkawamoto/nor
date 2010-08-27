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
package nor.http.server.nserver;

import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class NServer {

	public static final int Timeout;
	public static final int BufferSize;
	public static final int MinimusThreads;

	private NServer(){

	}


	static{

		final Logger LOGGER = Logger.getLogger(NServer.class);

		final String classname = NServer.class.getName();
		BufferSize = Integer.valueOf(System.getProperty(String.format("%s.BufferSize", classname)));
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));
		MinimusThreads = Integer.valueOf(System.getProperty(String.format("%s.MinimusThreads", classname)));

		LOGGER.config("<class init>", "Load a constant: BufferSize = {0}", BufferSize);
		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: MinimusThreads = {0}", MinimusThreads);

	}

}
