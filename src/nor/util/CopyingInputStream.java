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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 入力と同時に複製を作成するフィルタ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class CopyingInputStream extends SequentialInputStream{

	/**
	 * コピー
	 */
	private final ByteArrayOutputStream _copy = new ByteArrayOutputStream();

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * 入力ストリームinをフィルタリングするCopyingInputStreamを作成する．
	 *
	 * @param in フィルタリング対象の入力ストリーム
	 */
	public CopyingInputStream(final InputStream in){

		super(in);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {

		final int ret = this.in.read();
		if(ret != -1){

			this._copy.write(ret);

		}

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		final int ret = this.in.read(b, off, len);
		if(ret != -1){

			this._copy.write(b, off, ret);

		}

		return ret;

	}

	/**
	 * 作成した複製の取得する．
	 * 読み込みが終了していない場合は，今まで読み込まれたデータのコピーを返す．
	 * 返されるコピーは深いコピーである．
	 *
	 * @return CopyingInputStreamから読み込まれたデータのコピー
	 */
	public byte[] copy(){

		return this._copy.toByteArray();

	}

}


