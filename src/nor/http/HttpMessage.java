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
package nor.http;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import nor.util.log.Logger;


/**
 * HTTP メッセージを表す抽象クラス．
 * リクエスト及びレスポンスはこのクラスのサブクラスになります．
 * 一つの HTTP メッセージは，メッセージヘッダとメッセージボディを一つずつ持ちます．
 * このクラスは，それらに対するアクセッサを提供します．また，メッセージの書き出しメソッドも提供します．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 *
 */
public abstract class HttpMessage{

	private static final Logger LOGGER = Logger.getLogger(HttpMessage.class);

	//====================================================================
	//	Constructors
	//====================================================================
	protected HttpMessage(){

	}

	//====================================================================
	//	Public methods
	//====================================================================
	/**
	 * メッセージをストリームに書き出す．
	 * このオブジェクトが表すメッセージをストリームに書き出します．メッセージの書き出しは破壊的操作になります．
	 * プラグイン開発者がこのメソッドを呼ぶことはありません．
	 * メッセージを取得する場合は，ストリームフィルタを使用してください．
	 *
	 * @param output 書き出し先の出力ストリーム
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


	//====================================================================
	//	Public abstract methods
	//====================================================================
	/**
	 * HTTP メッセージのバージョンを取得する．
	 *
	 * @return HTTP メッセージのバージョン
	 */
	public abstract String getVersion();

	/**
	 * HTTP メッセージの要求パスを取得する.
	 *
	 * @return 要求パス
	 */
	public abstract String getPath();

	/**
	 * メッセージヘッダを取得する．
	 *
	 * @return ヘッダオブジェクト
	 */
	public abstract HttpHeader getHeader();

	/**
	 * メッセージボディを取得する．
	 *
	 * @return ボディオブジェクト
	 */
	public abstract HttpBody getBody();

	/**
	 * メッセージのヘッドラインを取得する．
	 * ヘッドラインはリクエストとレスポンスで異なっています．
	 * リクエストでは，
	 * <pre>
	 *     {Method} {Request-path} HTTP/{Version}
	 * </pre>
	 * というフォーマットになり，レスポンスでは，
	 * <pre>
	 *     HTTP/{Version} {Status-code} {Status-message}
	 * </pre>
	 * というフォーマットになります．
	 *
	 * @return HTTP メッセージのヘッドライン
	 */
	public abstract String getHeadLine();

	/**
	 * オブジェクトの文字列表現を返す．
	 * このメソッドでは，ヘッドラインとヘッダのみからなる文字列を返します．
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

}
