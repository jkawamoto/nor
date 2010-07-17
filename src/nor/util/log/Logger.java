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
package nor.util.log;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

public class Logger extends java.util.logging.Logger{

	private final String classname;

	private Logger(final String classname){
		super(classname, null);

		this.classname = classname;

	}

	public static <T> Logger getLogger(final Class<T> type){

		final String classname = type.getName();
		final LogManager m = LogManager.getLogManager();

		final java.util.logging.Logger log = m.getLogger(classname);
		if(log == null || !(log instanceof Logger)){

			final Logger n = new Logger(classname);
			m.addLogger(n);

			return n;

		}else{

			return (Logger)log;

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

	public void severe(final Object msg){

		if(msg == null){

			super.severe("null");

		}else{

			super.severe(msg.toString());

		}

	}


	//============================================================================
	//
	//============================================================================
	public void throwing(final String method, final Throwable e){

		super.throwing(this.classname, method, e);

	}

	public void throwing(final Class<?> cls, final String method, final Throwable e){

		super.throwing(cls.getName(), method, e);

	}

	public void catched(final Level level, final String method, final String msg, final Throwable thrown){

		this.logp(level, this.classname, method, msg, thrown);

	}

	public void catched(final Level level, final String method, final Throwable thrown){

		this.catched(level, method, thrown.getMessage(), thrown);

	}

	public void catched(final Level level, final Class<?> cls, final String method, final String msg, final Throwable thrown){

		this.logp(level, cls.getName(), method, msg, thrown);

	}

	public void catched(final Level level, final Class<?> cls, final String method, final Throwable thrown){

		this.catched(level, cls, method, thrown.getMessage(), thrown);

	}

	//============================================================================
	// Overridden logging methods
	//============================================================================
	@Override
	public void log(final LogRecord record) {

		final String msg = record.getMessage();
		record.setMessage(String.format("[%s] %s", Thread.currentThread().getName(), msg));
		//record.setSourceClassName(this.classname);

		super.log(record);

	}

	//============================================================================
	// Formatable logging methods.
	// For format strings, you can use {0}, {1}, ... as place holders.
	//============================================================================
	public void warning(final String method, final String format, final Object... args){

		this.logp(Level.WARNING, this.classname, method, format, args);

	}

	public void info(final String method, final String format, final Object... args){

		this.logp(Level.INFO, this.classname, method, format, args);

	}

	public void config(final String method, final String format, final Object... args){

		this.logp(Level.CONFIG, this.classname, method, format, args);

	}

	public void fine(final String method, final String format, final Object... args){

		this.logp(Level.FINE, this.classname, method, format, args);

	}

	public void finer(final String method, final String format, final Object... args){

		this.logp(Level.FINER, this.classname, method, format, args);

	}

	public void finest(final String method, final String format, final Object... args){

		this.logp(Level.FINEST, this.classname, method, format, args);

	}

	public void warning(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.WARNING, cls.getName(), method, format, args);

	}

	public void info(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.INFO, cls.getName(), method, format, args);

	}

	public void config(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.CONFIG, cls.getName(), method, format, args);

	}

	public void fine(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.FINE, cls.getName(), method, format, args);

	}

	public void finer(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.FINER, cls.getName(), method, format, args);

	}

	public void finest(final Class<?> cls, final String method, final String format, final Object... args){

		this.logp(Level.FINEST, cls.getName(), method, format, args);

	}

}


