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

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EasyLogger extends Logger{

	private final String classname;

	private EasyLogger(final String classname){
		super(classname, null);

		this.classname = classname;

	}

	public static <T> EasyLogger getLogger(final Class<T> type){

		final String classname = type.getName();
		final LogManager m = LogManager.getLogManager();

		final Logger log = m.getLogger(classname);
		if(log == null || !(log instanceof EasyLogger)){

			final EasyLogger n = new EasyLogger(classname);
			m.addLogger(n);

			return n;

		}else{

			return (EasyLogger)log;

		}

	}

	public <T> void entering(final String method, final T ... params){

		super.entering(this.classname, method, params);

	}

	public void exiting(final String method){

		super.exiting(this.classname, method);

	}

	public <T> void exiting(final String method, final T param){

		super.exiting(this.classname, method, param);

	}

	public void finest(final Object msg){

		if(msg == null){

			super.finest("null");

		}else{

			super.finest(msg.toString());

		}

	}

	public void warning(final Object msg){

		if(msg == null){

			super.warning("null");

		}else{

			super.warning(msg.toString());

		}

	}

	public void severe(final Object msg){

		if(msg == null){

			super.severe("null");

		}else{

			super.severe(msg.toString());

		}

	}

	public void throwing(final String method, final Throwable e){

		super.throwing(this.classname, method, e);

	}

}


