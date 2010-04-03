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

import nor.core.proxy.FilterContainer;
import nor.http.HttpMessage;

/**
 * @author Junpei
 *
 */
public interface MessageFilter<Message extends HttpMessage>{

	/**
	 * 新しいメッセージが届いたことを通知します．
	 *
	 * @param msg 新しいメッセージ
	 * @param container ストリームフィルタの登録先
	 * @param isCharacter テキストメッセージの場合 true
	 */
	public void update(final Message msg, final FilterContainer container, final boolean isCharacter);

}
