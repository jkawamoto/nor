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
package nor.http.server.nserver;

import java.io.IOException;
import java.util.Properties;

import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class NServer {

	public static final int Timeout;
	public static final int BufferSize;
	public static final int MaxThreads;

	private NServer(){

	}


	static{

		final Logger LOGGER = Logger.getLogger(NServer.class);

		final String classname = NServer.class.getName();
		final Properties defaults = new Properties();
		try {

			defaults.load(NServer.class.getResourceAsStream("res/default.conf"));

		} catch (final IOException e) {

			LOGGER.severe("<class init>", "Cannot load default configs ({0})", e);

		}

		final String bsize = String.format("%s.BufferSize", classname);
		BufferSize = Integer.valueOf(System.getProperty(bsize, defaults.getProperty(bsize)));

		final String tout = String.format("%s.Timeout", classname);
		Timeout = Integer.valueOf(System.getProperty(tout, defaults.getProperty(tout)));

		final String mthreads = String.format("%s.MaxThreads", classname);
		MaxThreads = Integer.valueOf(System.getProperty(mthreads, defaults.getProperty(mthreads)));

		LOGGER.config("<class init>", "Load a constant: BufferSize = {0}", BufferSize);
		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: MaxThreads = {0}", MaxThreads);

	}

}
