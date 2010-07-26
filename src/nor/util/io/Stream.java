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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public class Stream {

	public static final int DefaultBufferSize;

	public static void copy(final InputStream in, final OutputStream out) throws IOException{

		copy(in, out, DefaultBufferSize);

	}

	public static void copy(final InputStream in, final OutputStream out, final int bufSize) throws IOException{

		try{

			final ReadableByteChannel cin = Channels.newChannel(in);
			final WritableByteChannel cout = Channels.newChannel(out);
			copy(cin, cout, bufSize);

		}catch(final ClosedByInterruptException e){

			throw new IOException(e);

		}

	}

	public static void copy(final ReadableByteChannel in, final WritableByteChannel out) throws IOException{

		Stream.copy(in, out, DefaultBufferSize);

	}

	public static void copy(final ReadableByteChannel in, final WritableByteChannel out, final int bufSize) throws IOException{

		final ByteBuffer buf = ByteBuffer.allocate(bufSize);

		long c = 0;

		while(true){

			buf.clear();
			if(in.read(buf) < 0){

				break;

			}
			buf.flip();

			int len = buf.limit() - buf.position();
			c += len;
			while(len != 0){

				len -= out.write(buf);

			}

			if(Thread.interrupted()){

				throw new IOException();

			}

		}

	}

	//============================================================================
	//  Class constructor
	//============================================================================
	static{

		final String classname = Stream.class.getName();
		DefaultBufferSize = Integer.valueOf(System.getProperty(String.format("%s.DefaultBufferSize", classname)));

	}

}


