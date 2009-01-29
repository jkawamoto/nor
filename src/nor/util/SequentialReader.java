/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * 逐次読み出しのみをサポートするフィルタ．
 * このフィルタを通したリーダには逐次アクセスしかできなくなる．
 * mark, resetメソッドを使用した場合，IOExceptionが投げられる．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class SequentialReader extends FilterReader{

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * ストリームリーダinをフィルタリングするSequentialReaderを作成する．
	 *
	 * @param in フィルタリング対象のストリームリーダ
	 */
	public SequentialReader(final Reader in){

		super(in);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.FilterReader#mark(int)
	 */
	@Override
	public final void mark(int readAheadLimit) throws IOException {

		throw new IOException("このリーダはリセットをサポートしません。");

	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#markSupported()
	 */
	@Override
	public final boolean markSupported() {

		return false;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#reset()
	 */
	@Override
	public final void reset() throws IOException {

		throw new IOException("このリーダはリセットをサポートしません。");

	}

}

