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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTPレスポンスを表すクラス．
 * このクラスのインスタンス一つが，一つのHTTPレスポンスに対応している．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class Response extends Message{

	/**
	 * このレスポンスの元になったHTTPリクエスト
	 */
	private final Request _request;

	/**
	 * Status-Code
	 */
	private int _code;

	/**
	 * Reason-Phrase
	 */
	private String _message;

	/**
	 * Status Line解析用正規表現
	 */
	private static final Pattern StatusLine = Pattern.compile("^HTTP/(\\S{3})\\s+(\\d{3})\\s*(.*)$");

	/**
	 * Status Line出力用テンプレート
	 */
	private static final String HeadLine = "HTTP/%s %d %s";

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * レスポンスの元となったリクエストrequestとレスポンスを含む入力ストリームinputを指定してResponseを作成する．
	 *
	 * @param request このレスポンスの元になったHTTPリクエスト
	 * @param input レスポンスを含む入力ストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 * @throws BadMessageException 無効なレスポンスの場合
	 */
	Response(final Request request, final InputStream input) throws IOException, BadMessageException{
		super(input);

		assert request != null;
		this._request = request;

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * レスポンスの元となったリクエストの取得．
	 *
	 * @return HTTPリクエスト
	 */
	public Request getRequest(){

		return this._request;

	}

	/**
	 * HTTPレスポンスコードを返す．
	 *
	 * @return HTTPレスポンスコード
	 */
	public int getCode(){

		return this._code;

	}

	/**
	 * Reason-Phraseの取得．
	 *
	 * @return Reason-Phrase
	 */
	public String getMessage(){

		return this._message;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#getHeadLine()
	 */
	@Override
	public String getHeadLine() {

		return String.format(HeadLine, this.getVersion(), _code, _message);

	}

	//====================================================================
	//	protected メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#readHeadLine(java.lang.String)
	 */
	@Override
	protected boolean readHeadLine(final String line) {

		// ステータスコードの取得
		final Matcher m = StatusLine.matcher(line);
		if(m.matches()){

			this.setVersion(m.group(1));
			this._code = Integer.parseInt(m.group(2));
			this._message = m.group(3);

			return true;

		}

		return false;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.http.HttpMessage#readBody(java.io.InputStream)
	 */
	@Override
	protected Body readBody(final InputStream input) throws IOException {

		return director.build(this, input);

		// ボディの読み取り
		//if("HEAD".equalsIgnoreCase(this.getRequest().getMethod()) || (100 <= this._code && this._code < 200) || this._code == 204 || this._code == 304){
//		if((100 <= this._code && this._code < 200) || this._code == 204 || this._code == 304){
//
//			// メッセージボディなし
//			return new EmptyBody(this);
//
//		}else{
//
//			// TODO: コンテントタイプからボディを決定する．
//			// TODO: リードオンリーボディを用意。フィルタに送るのはコピーでフィルタリング前に転送を完了させておく．
//
//			// メッセージボディあり
//			final String type = this.getHeader().getContentType().getMIMEType();
//			final String subtype = this.getHeader().getContentType().getMIMESubtype();
//			if("text".equalsIgnoreCase(type) || "xhtml+xml".equalsIgnoreCase(subtype)){
//
//				return new TextBody(this, input);
//
//			}else{
//
//				final BinaryBody body = new BinaryBody(this, input);
//				return body;
//
//			}
//
//		}

	}

}
