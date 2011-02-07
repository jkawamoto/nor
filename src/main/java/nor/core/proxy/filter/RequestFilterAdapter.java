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

import java.util.regex.Pattern;

/**
 * RequestFilter 実装用のアダプタクラス．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public abstract class RequestFilterAdapter implements RequestFilter{

	private final Pattern url;
	private final Pattern cType;

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * フィルタを適用する URL とコンテンツタイプを指定して RequestFilterAdapter を作成します．
	 * このコンストラクタでは，URL とコンテンツタイプを Pattern オブジェクトで指定します．
	 *
	 * @param url フィルタを適用する URL
	 * @param cType フィルタを適用するコンテンツタイプ
	 */
	public RequestFilterAdapter(final Pattern url, final Pattern cType){

		this.url = url;
		this.cType = cType;

	}

	/**
	 * フィルタを適用する URL とコンテンツタイプを指定して RequestFilterAdapter を作成します．
	 * このコンストラクタでは，URL とコンテンツタイプを正規表現文字列で指定します．
	 *
	 * @param url フィルタを適用する URL の正規表現文字列
	 * @param cType フィルタを適用するコンテンツタイプの正規表現文字列
	 */
	public RequestFilterAdapter(final String urlRegex, final String cTypeRegex){
		this(Pattern.compile(urlRegex), Pattern.compile(cTypeRegex));
	}

	//============================================================================
	//  Public methods
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.MessageFilter#getFilteringURL()
	 */
	@Override
	public Pattern getFilteringURL() {

		return this.url;

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.MessageFilter#getFilteringContentType()
	 */
	@Override
	public Pattern getFilteringContentType() {

		return this.cType;

	}

}
