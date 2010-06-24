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
package nor.core.proxy.filter;

import java.io.Closeable;
import java.nio.ByteBuffer;

/**
 * ストリーム内容を変更する可能性のあるバイナリストリームフィルタが実装すべきインタフェース．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface EditingByteFilter extends Closeable{


	public ByteBuffer update(final ByteBuffer in);

	/**
	 * データの転送が終了したことを通知します．
	 */
	@Override
	public void close();

}
