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
import java.io.InputStream;

public class EmptyInputStream extends InputStream{

	private static InputStream instance = null;

	private EmptyInputStream(){

	}

	@Override
	public int read() throws IOException {
		return -1;
	}

	public static synchronized InputStream getInstance(){

		if(instance == null){

			instance = new EmptyInputStream();

		}

		return instance;

	}

}


