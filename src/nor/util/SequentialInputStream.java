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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SequentialInputStream extends FilterInputStream{

	/**
	 * @param in
	 */
	protected SequentialInputStream(InputStream in) {

		super(in);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#markSupported()
	 */
	@Override
	public final boolean markSupported() {

		return false;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#reset()
	 */
	@Override
	public final synchronized void reset() throws IOException {

		throw new IOException("このストリームはリセットをサポートしません。");

	}

}


