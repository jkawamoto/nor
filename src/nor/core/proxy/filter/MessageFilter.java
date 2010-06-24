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

import nor.http.HttpMessage;

/**
 * HTTPメッセージのフィルタが実装すべきインタフェース．
 * リクエストやレスポンスといったHTTPメッセージに対するフィルタを作成するには，
 * このインタフェースを特殊化した{@link RequestFilter} または {@link ResponseFilter} を実装してください.
 *
 * フレームワークは，リクエストあるいはレスポンスを受け取ると，フレームワークに登録されているすべてのフィルタに対して，
 * リクエスト URL 及びメッセージボディのコンテンツタイプがフィルタリング対象のパターンにマッチするか調べます.
 * そして，マッチするフィルタに対して，そのパターンマッチングの結果とともに update メソッドを呼び出します.
 * マッチング結果を用いることで，URL パターンやコンテンツタイプに応じてフィルタリング処理を変えることができます.
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface MessageFilter<Message extends HttpMessage>{

	/**
	 * フィルタを適用する URL にマッチする Pattern オブジェクトを取得する．
	 * フレームワークは，このメソッドが返すパターンオブジェクトを使用して update メソッドを呼び出すか判断します．
	 *
	 * @return フィルタを適用する URL にマッチするパターンオブジェクト
	 */
	public Pattern getFilteringURL();

	/**
	 * フィルタを適用する コンテンツタイプにマッチする Pattern オブジェクトを取得する．
	 * フレームワークは，このメソッドが返すパターンオブジェクトを使用して update メソッドを呼び出すか判断します．
	 *
	 * @return フィルタを適用するコンテンツタイプにマッチするパターンオブジェクト
	 */
	public Pattern getFilteringContentType();

	/**
	 * フィルタを適用すべきメッセージを受け取ったことを通知します．
	 * getFilteringURL メソッドと getFilteringContentType メソッドが返すパターンにより宣言した，
	 * フィルタリングすべきメッセージをフレームワークが受け取ったことを通知します．
	 *
	 * このメソッドが呼び出される段階では，メッセージボディの転送は始まっていませんが，ヘッダを参照することはできます．
	 * URL, コンテンツタイプ及びメッセージヘッダを見て，メッセージボディをフィルタリングすべきと判断した場合，
	 * register オブジェクトにストリームフィルタを登録します．
	 * 実勢にメッセージボディの転送が始まると，ストリームフィルタに通知されます．
	 *
	 * update メソッドでは，ストリームフィルタの登録のほかに，ヘッダの書き換えを行うことができます．
	 * 内容コーディングなどメッセージボディの転送前に変更すべきヘッダがある場合は，ここで変更してください．
	 *
	 * @param msg フィルタを適用するメッセージ
	 * @param url getFilteringURL メソッドが返す Pattern オブジェクトに URL をマッチさせた結果
	 * @param cType getFilteringContentType メソッドが返す Pattern オブジェクトにコンテンツタイプをマッチさせた結果
	 * @param register ストリームフィルタの登録先
	 */
	public void update(final Message msg, final MatchResult url, final MatchResult cType, final FilterRegister register);

}
