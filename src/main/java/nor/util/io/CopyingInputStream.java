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
package nor.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class CopyingInputStream extends SequentialInputStream{

	private final ByteArrayOutputStream copy = new ByteArrayOutputStream();

	/**
	 * @param in
	 */
	public CopyingInputStream(final InputStream in){

		super(in);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {

		final int ret = this.in.read();
		if(ret != -1){

			this.copy.write(ret);

		}

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		final int ret = this.in.read(b, off, len);
		if(ret != -1){

			this.copy.write(b, off, ret);

		}

		return ret;

	}

	/**
	 * @return
	 */
	public byte[] getCopy(){

		return this.copy.toByteArray();

	}

}


