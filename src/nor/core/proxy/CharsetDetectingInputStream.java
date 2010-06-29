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
package nor.core.proxy;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.io.CopyingInputStream;

class CharsetDetectingInputStream extends FilterInputStream{

	private final Charset charset;

	private static final Pattern Char = Pattern.compile("(?:encoding=\"([\\w_\\-]+)\"|charset=([\\w_\\-]+))", Pattern.CASE_INSENSITIVE);



	public CharsetDetectingInputStream(final InputStream in){
		super(in);

		final CopyingInputStream cin = new CopyingInputStream(in);
		Charset c = null;
		try{

			final BufferedReader bin = new BufferedReader(new InputStreamReader(cin));
			String buf;
			while((buf = bin.readLine()) != null){

				final Matcher m = Char.matcher(buf);
				if(m.find()){

					if(m.group(1) != null){

						c = Charset.forName(m.group(1));

					}else if(m.group(2) != null){

						c = Charset.forName(m.group(2));

					}

					break;

				}

			}

			this.in = new SequenceInputStream(new ByteArrayInputStream(cin.getCopy()), in);

		}catch(final IOException e){

		}

		this.charset = c;

	}

	public Charset getCharset(){

		return this.charset;

	}





}
