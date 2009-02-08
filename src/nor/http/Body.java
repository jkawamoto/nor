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

/**
 * HTTPメッセージボディを表す抽象クラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public abstract class Body {

	/**
	 * このメッセージボディを所有するHTTPメッセージ
	 */
	protected final Message parent;

	/**
	 * メッセージボディの入力ストリーム
	 */
	protected InputStream in;

	/**
	 * ボディタイプを表すヘッダ項目名
	 */
	private static final String BodyTypeHeader = "x-nor-bodytype";

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * このメッセージボディを持つparentと入力ストリームinを指定してBodyを作成する．
	 *
	 * @param parent このメッセージボディを持つ親メッセージ
	 * @param in メッセージボディを含む入力ストリーム
	 */
	public Body(final Message parent, final InputStream in){
		assert parent != null;
		assert in != null;

		this.parent = parent;
		this.in = in;

		this.parent.getHeader().set(BodyTypeHeader, this.getClass().getSimpleName());

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * ContentTypeを取得する．
	 *
	 * @return このオブジェクトを持つメッセージのContentType
	 */
	public ContentType getContentType(){

		return this.parent.getHeader().getContentType();

	}

	//====================================================================
	//  package private メソッド
	//====================================================================
	/**
	 * 入力ストリームを取得する．
	 * メッセージボディコンテンツを返す入力ストリームを取得する．
	 *
	 * @return 入力ストリーム
	 */
	InputStream getInputStream(){

		return this.in;

	}


}
