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
package nor.http.server.proxyserver;

import nor.http.Message;


/**
 * HTTPリクエストに対するフィルタインタフェース．
 * フィルタリング機構はObserverパターンを採用しており，新たなリクエストが送信される前に，通知が行われる．
 * 通知を受け取るためには，このインタフェースを実装したオブジェクトをSubjectへ登録する必要がある．
 *
 * @author KAWAMOTO Junpei
 *
 */
public interface RequestFilter extends MessageFilter<RequestFilter.Info>{

	/**
	 * 新たなHTTPリクエストが送信される前に呼ばれる．
	 *
	 * @param request 送信されようとしているHTTPリクエスト
	 */
	@Override
	public void update(final RequestFilter.Info request);

	public class Info extends MessageFilter.Info{

		public Info(final Message msg) {
			super(msg);
		}

	}

}