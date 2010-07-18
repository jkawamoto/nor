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

public class Logger{

	private final String classname;
	private final LoggerImpl impl;

	//============================================================================
	// Static methods
	//============================================================================
	public static <T> Logger getLogger(final Class<T> type){

		final String classname = type.getName();
		final LogManager m = LogManager.getLogManager();

		final java.util.logging.Logger log = m.getLogger(classname);
		if(log == null || !(log instanceof LoggerImpl)){

			final Logger n = new Logger(classname);
			m.addLogger(n.impl);

			return n;

		}else{

			final LoggerImpl impl = (LoggerImpl)log;
			return impl.getLogger();

		}

	}

	//============================================================================
	// Constractor
	//============================================================================
	private Logger(final String classname){

		this.classname = classname;
		this.impl = new LoggerImpl(classname);

	}


	//============================================================================
	// Public methods
	//============================================================================
	public void entering(final String method, final Object ... params){

		this.finest(method, "ENTRY {0}", params);

	}

	public void exiting(final String method){

		this.finest(method, "EXIT");

	}

	public void exiting(final String method, final Object param){

		this.finest(method, "EXIT {0}", param);

	}

	//============================================================================
	//
	//============================================================================
	public void throwing(final String method, final Throwable e){

		this.impl.throwing(this.classname, method, e);

	}

	public void throwing(final Class<?> cls, final String method, final Throwable e){

		this.impl.throwing(cls.getName(), method, e);

	}

	public void catched(final Level level, final String method, final String msg, final Throwable thrown){

		this.impl.logp(level, this.classname, method, msg, thrown);

	}

	public void catched(final Level level, final String method, final Throwable thrown){

		this.catched(level, method, thrown.getMessage(), thrown);

	}

	public void catched(final Level level, final Class<?> cls, final String method, final String msg, final Throwable thrown){

		this.impl.logp(level, cls.getName(), method, msg, thrown);

	}

	public void catched(final Level level, final Class<?> cls, final String method, final Throwable thrown){

		this.catched(level, cls, method, thrown.getMessage(), thrown);

	}


	//============================================================================
	// Formatable logging methods.
	// For format strings, you can use {0}, {1}, ... as place holders.
	//============================================================================
	public void severe(final String method, final String format, final Object... args){

		this.impl.logp(Level.SEVERE, this.classname, method, format, args);

	}

	public void warning(final String method, final String format, final Object... args){

		this.impl.logp(Level.WARNING, this.classname, method, format, args);

	}

	public void info(final String method, final String format, final Object... args){

		this.impl.logp(Level.INFO, this.classname, method, format, args);

	}

	public void config(final String method, final String format, final Object... args){

		this.impl.logp(Level.CONFIG, this.classname, method, format, args);

	}

	public void fine(final String method, final String format, final Object... args){

		this.impl.logp(Level.FINE, this.classname, method, format, args);

	}

	public void finer(final String method, final String format, final Object... args){

		this.impl.logp(Level.FINER, this.classname, method, format, args);

	}

	public void finest(final String method, final String format, final Object... args){

		this.impl.logp(Level.FINEST, this.classname, method, format, args);

	}

	/////////////////////////////////////////////////////////////////////////////

	public void severe(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.SEVERE, cls.getName(), method, format, args);

	}

	public void warning(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.WARNING, cls.getName(), method, format, args);

	}

	public void info(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.INFO, cls.getName(), method, format, args);

	}

	public void config(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.CONFIG, cls.getName(), method, format, args);

	}

	public void fine(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.FINE, cls.getName(), method, format, args);

	}

	public void finer(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.FINER, cls.getName(), method, format, args);

	}

	public void finest(final Class<?> cls, final String method, final String format, final Object... args){

		this.impl.logp(Level.FINEST, cls.getName(), method, format, args);

	}

	//============================================================================
	// Inner class
	//============================================================================
	private class LoggerImpl extends java.util.logging.Logger{

		//============================================================================
		// Constractor
		//============================================================================
		public LoggerImpl(final String classname){
			super(classname, null);
		}

		//============================================================================
		// Public methods
		//============================================================================
		public Logger getLogger(){

			return Logger.this;

		}

		//============================================================================
		// Overridden logging methods
		//============================================================================
		@Override
		public void log(final LogRecord record) {

			final String msg = record.getMessage();
			record.setMessage(String.format("[%s] %s", Thread.currentThread().getName(), msg));

			super.log(record);

		}

	}

}


