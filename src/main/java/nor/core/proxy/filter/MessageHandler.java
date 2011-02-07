/*
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

import nor.http.HttpRequest;
import nor.http.HttpResponse;

/**
 * メッセージハンドラが実装すべきインタフェース．
 * <p>
 * メッセージハンドラは，そのハンドラが処理するURLパターンを返す{@link nor.core.proxy.filter.MessageHandler#getHandlingURL メソッド}と，
 * 実際のリクエスト処理を行う{@link nor.core.proxy.filter.MessageHandler#doRequest メソッド}を提供する必要があります．
 * </p>
 * <p>
 * フレームワークは，{@link nor.core.proxy.filter.MessageHandler#getHandlingURL getHandlingURL}メソッドが返すパターンを用いてリクエストURLを評価し，
 * マッチする場合，そのマッチング結果とともに{@link nor.core.proxy.filter.MessageHandler#doRequest doRequest}メソッドを呼び出します．
 * したがって，{@link java.util.regex.MatchResult#group(int) group}メソッドを用いて，URLから情報を取得することができます．
 * </p>
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface MessageHandler{

	/**
	 * ハンドラを適用するURLにマッチするパターンオブジェクトを取得する．
	 * フレームワークは，このメソッドが返すパターンオブジェクトを使用してメッセージ処理を移譲するか判断します．
	 *
	 * @return パターンオブジェクト
	 */
	public Pattern getHandlingURL();

	/**
	 * リクエストを処理する．
	 * リクエスト URL がパターンにマッチした場合，このメソッドが呼び出されます．
	 *
	 * @param request リクエストオブジェクト
	 * @param url パターンマッチの結果
	 * @return
	 */
	public HttpResponse doRequest(final HttpRequest request, final MatchResult url);

}
