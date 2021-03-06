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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import nor.util.log.Logger;

public class NoCloseInputStream extends FilterInputStream{

	private static final Logger LOGGER = Logger.getLogger(NoCloseInputStream.class);

	public NoCloseInputStream(final InputStream in){

		super(in);

	}

	@Override
	public void close() throws IOException {

	}

	public void reallyClose() throws IOException{
		LOGGER.entering("reallyClose");

		this.in.close();

		LOGGER.exiting("reallyClose");
	}

}


