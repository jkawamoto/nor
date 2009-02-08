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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 閉じられない出力ストリームフィルタ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class NoCloseOutputStream extends FilterOutputStream{

	/**
	 * 出力ストリームoutをフィルタリングするNoCloseOutputStreamを作成する．
	 *
	 * @param out フィルタリング対象の出力ストリーム
	 */
	public NoCloseOutputStream(final OutputStream out){

		super(out);

	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#close()
	 */
	@Override
	public void close() throws IOException {

	}

	/**
	 * ストリームを実際に閉じる．
	 *
	 * @throws IOException ストリーム操作にI/Oエラーが発生した場合
	 */
	public void reallyClose() throws IOException{

		this.close();

	}


}


