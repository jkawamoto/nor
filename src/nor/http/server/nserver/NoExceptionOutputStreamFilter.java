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
package nor.http.server.nserver;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

class NoExceptionOutputStreamFilter extends FilterOutputStream{

	private boolean alive = true;

	public NoExceptionOutputStreamFilter(OutputStream out) {

		super(out);

	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {

		if(this.alive){

			try{

				super.flush();

			}catch(final IOException e){

				this.alive = false;

			}

		}
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		if(this.alive){

			try{

				super.write(b, off, len);

			}catch(final IOException e){

				this.alive = false;

			}

		}
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[])
	 */
	@Override
	public void write(byte[] b) throws IOException {

		if(this.alive){

			try{

				super.write(b);

			}catch(final IOException e){

				this.alive = false;

			}

		}
	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {

		if(this.alive){

			try{

				super.write(b);

			}catch(final IOException e){

				this.alive = false;

			}

		}
	}

	public boolean alive(){

		return this.alive;

	}

}
