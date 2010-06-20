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

import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import nor.http.HttpMessage;

/**
 * HTTPメッセージのフィルタが実装すべきインタフェース．
 *
 * @author Junpei
 *
 */
public interface MessageFilter<Message extends HttpMessage>{

	/**
	 * フィルタを適用するURLにマッチするパターンオブジェクトを取得する．
	 * フレームワークは，このメソッドが返すパターンオブジェクトを使用して update メソッドを呼び出すか判断します．
	 *
	 * @return パターンオブジェクト
	 */
	public Pattern getFilteringURL();

	/**
	 *
	 * @return
	 */
	public Pattern getFilteringContentType();

	/**
	 *
	 * @param msg
	 * @param url
	 * @param cType
	 * @param register
	 */
	public void update(final Message msg, final MatchResult url, final MatchResult cType, final FilterRegister register);

}
