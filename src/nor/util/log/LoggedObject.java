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
package nor.util.log;

import java.util.logging.Logger;

public abstract class LoggedObject {

	private final String className;
	protected final Logger LOGGER;

	public LoggedObject(){

		this.className = this.getClass().getName();
		this.LOGGER = Logger.getLogger(this.className);

	}

	protected LoggedObject(final String className){

		this.className = className;
		this.LOGGER = Logger.getLogger(this.className);

	}

	protected void entering(final String method, final Object ...params){

		this.LOGGER.entering(this.className, method, params);

	}

	protected void exiting(final String method, final Object ...params){

		this.LOGGER.exiting(this.className, method, params);

	}

}


