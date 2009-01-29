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

import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * 入力と同時に複製を作成するフィルタ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class CopyingStreamReader extends SequentialReader{

	/**
	 * コピー
	 */
	private final StringBuilder _copy = new StringBuilder();

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * リーダinをフィルタリングするCopyingStreamReaderを作成する．
	 *
	 * @param in フィルタリング対象のリーダ
	 */
	public CopyingStreamReader(final Reader in){

		super(in);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.FilterReader#read()
	 */
	@Override
	public int read() throws IOException {

		final int ret = this.in.read();
		this._copy.append((char)ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(char[])
	 */
	@Override
	public int read(char[] cbuf) throws IOException {

		final int ret = this.in.read(cbuf);
		this._copy.append(cbuf, 0, ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterReader#read(char[], int, int)
	 */
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {

		final int ret = this.in.read(cbuf, off, len);
		this._copy.append(cbuf, off, ret);

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.Reader#read(java.nio.CharBuffer)
	 */
	@Override
	public int read(CharBuffer target) throws IOException {

		final int ret = this.in.read(target);
		this._copy.append(target);

		return ret;

	}

	/**
	 * 作成した複製の取得する．
	 * 読み込みが終了していない場合は，今まで読み込まれた文字列のコピーを返す．
	 * 返されるコピーは深いコピーである．
	 *
	 * @return CopyingStreamReaderに読み込まれた文字列のコピー
	 */
	public String copy(){

		return this._copy.toString();

	}

}


