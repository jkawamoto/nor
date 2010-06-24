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
 * MessageHandler 実装用のアダプタクラス．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public abstract class MessageHandlerAdapter implements MessageHandler{

	private final Pattern pat;

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * ハンドリングするリクエスト URL を指定して MessageHandlerAdapter を作成します．
	 * このコンストラクタでは，URL を Pattern オブジェクトで指定します．
	 *
	 * @param pat ハンドリングするリクエスト URL
	 */
	public MessageHandlerAdapter(final Pattern pat){

		this.pat = pat;

	}

	/**
	 * ハンドリングするリクエスト URL を指定して MessageHandlerAdapter を作成します．
	 * このコンストラクタでは，URL を正規表現文字列で指定します．
	 *
	 * @param pat ハンドリングするリクエスト URL の正規表現文字列
	 */
	public MessageHandlerAdapter(final String regex){
		this(Pattern.compile(regex));
	}

	//============================================================================
	//  Public methods
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.MessageHandler#getHandlingURL()
	 */
	@Override
	public Pattern getHandlingURL() {

		return this.pat;

	}

}
