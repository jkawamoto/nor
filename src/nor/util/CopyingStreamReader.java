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
package nor.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

public class CopyingStreamReader extends SequentialReader{

	private final StringBuilder _copy = new StringBuilder();

	public CopyingStreamReader(final Reader in){

		super(in);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read()
	 */
	@Override
	public int read() throws IOException {

		final int ret = this.in.read();
		this._copy.append((char)ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[])
	 */
	@Override
	public int read(char[] cbuf) throws IOException {

		final int ret = this.in.read(cbuf);
		this._copy.append(cbuf, 0, ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {

		final int ret = this.in.read(cbuf, off, len);
		this._copy.append(cbuf, off, ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(java.nio.CharBuffer)
	 */
	@Override
	public int read(CharBuffer target) throws IOException {

		final int ret = this.in.read(target);
		this._copy.append(target);

		return ret;

	}

}


