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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正規表現にマッチするパターンのみを抽出する文字列ストリームフィルタ作成のためのアダプタ．
 * この抽象クラスを用いることで，転送中のストリームから特定のパターンにマッチする文字列を抽出することができます．
 * 例えば，HTML 文書中から title 要素の値を取得する場合は次のようにできます．
 * <pre>
 *     // register: ストリームフィルタのレジスタ
 *     register.add(new ReadonlyPatternFilterAdapter("&lt;(?:title|TITLE)&gt;(.*)&lt;/(?:title|TITLE)&gt;"){
 *
 *         public void update(final MatchResult res){
 *
 *             // title 要素の値
 *             final String title = res.group(1);
 *
 *         }
 *
 *     });
 * </pre>
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public abstract class ReadonlyPatternFilterAdapter extends ReadonlyStringFilterAdapter{

	private final Pattern pat;

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * 通知を希望する正規表現パターンを指定して ReadonlyPatternFilterAdapter を作成する．
	 * このコンストラクタでは，Pattern インスタンスを使用します．
	 *
	 * @param pat 通知を希望する正規表現パターン
	 */
	public ReadonlyPatternFilterAdapter(final Pattern pat){

		this.pat = pat;

	}

	/**
	 * 通知を希望する正規表現パターンを指定して ReadonlyPatternFilterAdapter を作成する．
	 * このコンストラクタでは，文字列で正規表現を表します．
	 *
	 * @param pat 通知を希望する正規表現パターン
	 */
	public ReadonlyPatternFilterAdapter(final String regex){
		this(Pattern.compile(regex));
	}

	//============================================================================
	//  Public methods
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.ReadonlyStringFilter#update(java.lang.String)
	 */
	@Override
	public final void update(final String in) {

		final Matcher m = this.pat.matcher(in);
		while(m.find()){

			this.update(m.toMatchResult());

		}

	}

	//============================================================================
	//  Public abstract methods
	//============================================================================
	/**
	 * パターンにマッチする文字列が見つかった．
	 * コンストラクタで指定したパターンが転送ストリーム中に見つかった場合に呼び出されます．
	 * 発見されたパターンには，引数 res を用いてアクセスすることができます．
	 *
	 * @param res パターンマッチの結果
	 */
	public abstract void update(final MatchResult res);

}
