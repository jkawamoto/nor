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
// $Id: HttpMessage.java 471 2010-04-03 10:25:20Z kawamoto $
package nor.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import nor.util.log.EasyLogger;


/**
 * 一つのHttpメッセージを表す抽象クラス．
 * Httpメッセージは，メッセージヘッダとメッセージボディを一つずつ持つ．
 *
 * @author KAWAMOTO Junpei
 *
 */
public abstract class HttpMessage{

	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpMessage.class);

	/**
	 * HTTPメッセージを書き出す．
	 * このオブジェクトが表すメッセージをストリームに書き出す．
	 *
	 * @param output 書き出し先のストリーム
	 * @throws IOException ストリームの書き出しにエラーが発生した場合
	 */
	public void output(final OutputStream output) throws IOException{
		LOGGER.entering("writeMessage", output);
		assert output != null;

		final HttpHeader header = this.getHeader();

		// ヘッドラインの書き出し
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		writer.append(this.getHeadLine());
		writer.append('\r');
		writer.append('\n');
		writer.flush();

		// ヘッダの書き出し
		header.writeHeader(writer);

		// バッファのフラッシュ
		writer.append('\r');
		writer.append('\n');
		writer.flush();
		writer.close();

		// ボディの書き出し
		this.getBody().output(output, this.getHeader());
		this.getBody().close();

		output.close();

		LOGGER.exiting("writeMessage");
	}



	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		LOGGER.entering("toString");

		final StringBuilder ret = new StringBuilder();

		ret.append(this.getHeadLine());
		ret.append("\n");
		ret.append(this.getHeader());
		ret.append("\n");

		LOGGER.exiting("toString", ret.toString());
		return ret.toString();
	}

	//====================================================================
	//	abstract メソッド
	//====================================================================
	/**
	 * Httpメッセージのバージョンを返す．
	 *
	 * @return Httpメッセージのバージョン
	 */
	public abstract String getVersion();

	/**
	 * Httpメッセージの要求パスを返す.
	 *
	 * @return 要求パス
	 */
	public abstract String getPath();

	/**
	 * Httpメッセージに含まれるヘッダを返す．
	 *
	 * @return このHttpメッセージにおけるヘッダオブジェクト
	 */
	public abstract HttpHeader getHeader();

	/**
	 * Httpメッセージに含まれるメッセージボディを返す．
	 *
	 * @return このHttpメッセージにおけるメッセージボディオブジェクト
	 */
	public abstract HttpBody getBody();

	/**
	 * ヘッドラインの取得．
	 * このメソッドはスーパークラスから呼び出されます．
	 * サブクラスはHTTPメッセージの1行目を返します．
	 *
	 * @return HTTPメッセージの1行目
	 */
	public abstract String getHeadLine();


}
