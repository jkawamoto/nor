/*
 *  Copyright (C) 2011 Junpei Kawamoto
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

import java.io.IOException;
import java.util.Properties;

import nor.util.log.Logger;

final class TServer {

	public static final int Timeout;
	public static final int MaxThreads;

	private TServer(){}


	static{

		final Logger LOGGER = Logger.getLogger(TServer.class);

		final String classname = TServer.class.getName();
		final Properties defaults = new Properties();
		try {

			defaults.load(TServer.class.getResourceAsStream("default.conf"));

		} catch (final IOException e) {

			LOGGER.severe("<class init>", "Cannot load default configs ({0})", e);

		}

		final String timeout = String.format("%s.Timeout", classname);
		Timeout = Integer.valueOf(System.getProperty(timeout, defaults.getProperty(timeout)));

		final String mthreads = String.format("%s.MaxThreads", classname);
		MaxThreads = Integer.valueOf(System.getProperty(mthreads, defaults.getProperty(mthreads)));

		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: MaxThreads = {0}", MaxThreads);

	}

}
