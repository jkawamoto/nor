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
package nor.util.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;

import nor.util.log.Logger;

public class NoExceptionOutputStreamFilter extends FilterOutputStream{

	private boolean alive = true;

	private static final Logger LOGGER = Logger.getLogger(NoExceptionOutputStreamFilter.class);

	public NoExceptionOutputStreamFilter(final OutputStream out) {
		super(out);
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {
		LOGGER.entering("flush");

		if(this.alive){

			try{

				this.out.flush();

			}catch(final IOException e){

				LOGGER.catched(Level.FINE, "flush", e);
				this.alive = false;
				this.out.close();

			}

		}

		LOGGER.exiting("flush");
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		LOGGER.entering("write", b, off, len);

		if(this.alive){

			try{

				this.out.write(b, off, len);

			}catch(final IOException e){

				LOGGER.catched(Level.FINE, "write", e);
				this.alive = false;
				this.out.close();

			}

		}

		LOGGER.exiting("write");
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {
		LOGGER.entering("write", b);

		if(this.alive){

			try{

				this.out.write(b);

			}catch(final IOException e){

				LOGGER.catched(Level.FINE, "write", e);
				this.alive = false;
				this.out.close();

			}

		}

		LOGGER.exiting("write");
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		LOGGER.entering("write", b);

		if(this.alive){

			try{

				this.out.write(b);

			}catch(final IOException e){

				LOGGER.catched(Level.FINE, "write", e);
				this.alive = false;
				this.close();

			}

		}

		LOGGER.exiting("write");
	}

	/**
	 * Check whether this stream is alive or not.
	 *
	 * @return If this stream is still alive, return true
	 */
	public boolean alive(){
		LOGGER.entering("alive");

		LOGGER.exiting("alive", this.alive);
		return this.alive;
	}

}
