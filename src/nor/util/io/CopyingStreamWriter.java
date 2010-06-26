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
package nor.util.io;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class CopyingStreamWriter extends FilterWriter{

	private final StringBuilder _copy = new StringBuilder();

	public CopyingStreamWriter(final Writer out) {

		super(out);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterWriter#write(char[], int, int)
	 */
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {

		super.write(cbuf, off, len);
		this._copy.append(cbuf, off, len);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterWriter#write(int)
	 */
	@Override
	public void write(int c) throws IOException {

		super.write(c);
		this._copy.append(c);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterWriter#write(java.lang.String, int, int)
	 */
	@Override
	public void write(String str, int off, int len) throws IOException {

		super.write(str, off, len);
		this._copy.append(str, off, len);

	}

	/* (non-Javadoc)
	 * @see java.io.Writer#append(char)
	 */
	@Override
	public Writer append(char c) throws IOException {

		this.append(c);
		return super.append(c);


	}

	/* (non-Javadoc)
	 * @see java.io.Writer#append(java.lang.CharSequence, int, int)
	 */
	@Override
	public Writer append(CharSequence csq, int start, int end) throws IOException {

		this._copy.append(csq, start, end);
		return super.append(csq, start, end);

	}

	/* (non-Javadoc)
	 * @see java.io.Writer#append(java.lang.CharSequence)
	 */
	@Override
	public Writer append(CharSequence csq) throws IOException {

		this._copy.append(csq);
		return super.append(csq);

	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[])
	 */
	@Override
	public void write(char[] cbuf) throws IOException {

		super.write(cbuf);
		this._copy.append(cbuf);

	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(String str) throws IOException {

		super.write(str);
		this.append(str);

	}

	public String copy(){

		return this._copy.toString();


	}

}

