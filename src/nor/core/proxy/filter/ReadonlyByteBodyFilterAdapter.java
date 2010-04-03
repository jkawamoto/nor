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
package nor.core.proxy.filter;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ReadonlyByteBodyFilterAdapter implements ByteBodyFilter{

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.arthra.lotte.StreamFilter#readonly(jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage)
	 */
	@Override
	public final boolean readonly() {

		return true;

	}

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.arthra.lotte.StreamFilter#update(jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage, java.nio.ByteBuffer)
	 */
	@Override
	public final ByteBuffer update(final ByteBuffer in) {

		this.update2(in);
		return null;

	}

	public abstract void update2(final ByteBuffer in);

	/* (非 Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.arthra.lotte.StreamFilter#close(jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage, boolean)
	 */
	@Override
	public void close() throws IOException{

	}


}
