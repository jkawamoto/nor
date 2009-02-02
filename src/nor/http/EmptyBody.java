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
package nor.http;

import java.io.InputStream;

import nor.util.EmptyInputStream;

/**
 * 空のメッセージボディ．
 * このメッセージボディは，GETリクエストなどメッセージボディを持ってはいけない場合に用いられる．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class EmptyBody extends Body{

	/**
	 * 空の入力ストリーム．
	 * 状態固定のため，一つだけ持っておけば良い
	 */
	private static final InputStream Empty = new EmptyInputStream();

	/**
	 * このメッセージボディを持つHTTPメッセージparentを指定してEmptyBodyを作成する．
	 *
	 * @param parent このメッセージボディを持つHTTPメッセージ
	 */
	public EmptyBody(final Message parent) {

		super(parent, Empty);

	}

}


