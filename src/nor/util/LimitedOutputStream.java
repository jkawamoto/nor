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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LimitedOutputStream extends FilterOutputStream{

	private int remains;

	public LimitedOutputStream(final OutputStream out, final int size) {

		super(out);
		this.remains = size;

	}



	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		if(len > this.remains){

			throw new IOException("書き込み可能サイズを超えています");

		}

		this.out.write(b, off, len);
		this.remains -= len;

	}



	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {

		if(this.remains == 0){

			throw new IOException("これ以上書き込めません");


		}

		this.out.write(b);
		--this.remains;

	}


	public int remains(){

		return this.remains;

	}

}


