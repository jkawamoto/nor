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

/**
 * 指定されたサイズで読み込みを終了するストリームフィルタ．
 * このストリームは、指定されたサイズだけ入力ストリームから読み込みます。
 *
 * @author KAWAMOTO Junpei
 *
 */
public class LimitedInputStream extends SequentialInputStream{

	/**
	 * 残り読み可能サイズ
	 */
	private int remains;

	/**
	 * 限定入力ストリームを作成する．
	 *
	 * @param in 入力元のストリーム
	 * @param size 読み込むサイズ
	 */
	public LimitedInputStream(final InputStream in, final int size){
		super(in);

		this.remains = size;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read()
	 */
	@Override
	public int read() throws IOException {

		if(this.remains == 0){

			return -1;

		}else{

			--this.remains;
			return this.in.read();

		}

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		int ret = -1;
		if(this.remains != 0){

			ret = this.in.read(b, off, Math.min(this.remains, len));
			if(ret != -1){

				this.remains -= ret;

			}

		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {

		long ret = 0;
		if(n > 0){
			if(n > this.remains){

				ret = this.remains;
				this.remains = 0;

			}else{

				ret = n;
				this.remains -= (int)n;

			}

			this.in.skip(ret);
		}

		return ret;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#available()
	 */
	@Override
	public int available() throws IOException {

		return Math.min(super.available(), this.remains);

	}

	public int remains(){

		return this.remains;

	}

}


