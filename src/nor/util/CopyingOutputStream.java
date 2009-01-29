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
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 出力と同時に複製を作成するフィルタ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class CopyingOutputStream extends FilterOutputStream{

	/**
	 * コピー
	 */
	private final ByteArrayOutputStream _copy = new ByteArrayOutputStream();

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * 出力ストリームoutをフィルタリングするCopyingOutputStreamを作成する．
	 *
	 * @param out フィルタリング対象の出力ストリーム
	 */
	public CopyingOutputStream(final OutputStream out){

		super(out);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {

		this.out.write(b);
		this._copy.write(b);

	}

	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		this.out.write(b, off, len);
		this._copy.write(b, off, len);

	}

	/**
	 * 作成した複製の取得する．
	 * 書き込みが終了していない場合は，今まで書き込まれたデータのコピーを返す．
	 * 返されるコピーは深いコピーである．
	 *
	 * @return CopyingOutputStreamに書き込まれたデータのコピー
	 */
	public byte[] copy(){

		return this._copy.toByteArray();

	}

}


