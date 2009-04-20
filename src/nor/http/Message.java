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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * HTTPメッセージを表す抽象クラス．
 * ヘッドラインとメッセージヘッダ，メッセージボディからなる．
 *
 * @author KAWAMOTO Junpei
 *
 */
public abstract class Message {

	/**
	 * 送受信用バッファサイズ
	 */
	static final int BufferSize = 1024 * 24;


	/**
	 * メッセージヘッダ
	 */
	private final Header _header;

	/**
	 * メッセージボディ
	 */
	private final Body _body;

	/**
	 * HTTP-Version
	 */
	private String _version;

	/**
	 *
	 */
	protected static final BodyDirector director = new BodyDirector();

	//====================================================================
	//	コンストラクタ
	//====================================================================
	/**
	 * 入力ストリームinからデータを読み込んで，Messageを作成する．
	 *
	 * @param input 入力元のストリーム
	 * @throws IOException ストリームの読み込み中にエラーが発生した場合
	 * @throws BadMessageException 不正なメッセージが渡された場合
	 */
	public Message(final InputStream input) throws IOException, BadMessageException{
		assert input != null;

		// ヘッダのみを読み込むストリームリーダの作成
		final BufferedReader in = new BufferedReader(new InputStreamReader(new HeaderInputStream(input)));

		// ヘッドラインの読み取り
		String buf;
		boolean isReaded = false;
		while(!isReaded && (buf = in.readLine()) != null){

			isReaded = this.readHeadLine(buf.trim());

		}
		if(!isReaded){

			throw new BadMessageException("ヘッドラインの読み込みに失敗しました");

		}

		// ヘッダの読み取り
		this._header = new Header(this, in);

		// ボディの読み取り
		this._body = this.readBody(input);

	}

	//====================================================================
	//	public メソッド
	//====================================================================
	/**
	 * このメッセージのヘッダを取得する．
	 *
	 * @return このメッセージにおけるヘッダ
	 */
	public Header getHeader(){

		return this._header;

	}

	/**
	 * このメッセージのボディを取得する．
	 *
	 * @return このメッセージにおけるボディ
	 */
	public Body getBody(){

		return this._body;

	}

	/**
	 * HTTPバージョンの取得．
	 *
	 * @return HTTPバージョン
	 */
	public String getVersion(){

		return this._version;

	}

	/**
	 * HTTPバージョンを設定する．
	 *
	 * @param version 新しいHTTPバージョン
	 */
	public void setVersion(final String version){

		this._version = version;

	}


	/**
	 * メッセージを出力ストリームへ書き出す．
	 *
	 * @param output 書き出し先の出力ストリーム
	 * @throws IOException ストリームの書き出し中にエラーが発生した場合
	 */
	public void writeMessage(final OutputStream output) throws IOException{
		assert output != null;

		final InputStream body = this._body.getInputStream();

		OutputStream out = output;
		if(this.getHeader().containsValue(HeaderName.TransferEncoding, "chunked")){

			out = new ChunkedOutputStream(output);

		}

		// TODO: Length-requiredの場合はここで全データを受信する．
		boolean header = false;
		if(body != null){

			final byte[] buffer = new byte[BufferSize];
			int n = -1;

			if((n = body.read(buffer)) != -1){

				this.writeHeader(output);
				header = true;

				out.write(buffer, 0, n);

				while((n = body.read(buffer)) != -1){

					// ボディの書き出し
					out.write(buffer, 0, n);

				}

			}

			body.close();

		}
		if(!header){

			this.writeHeader(output);
			header = true;

		}

		out.close();

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){

		final StringBuilder ret = new StringBuilder();

		ret.append(this.getHeadLine());
		ret.append("\n");
		ret.append(this._header);
		ret.append("\n");

		return ret.toString();

	}

	//====================================================================
	//	private メソッド
	//====================================================================
	/**
	 * ヘッダの出力．
	 *
	 * @param output 出力先ストリーム
	 * @throws IOException ストリームへの操作にエラーが発生した場合
	 */
	private void writeHeader(final OutputStream output) throws IOException{

		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));

		// ヘッドラインの書き出し
		writer.append(this.getHeadLine());
		writer.append('\r');
		writer.append('\n');
		writer.flush();

		// ヘッダの書き出し
		this._header.writeHeader(writer);

		// バッファのフラッシュ
		writer.append('\r');
		writer.append('\n');
		writer.flush();

	}

	//====================================================================
	//	abstract メソッド
	//====================================================================
	/**
	 * ヘッドラインを取得する．
	 * このメソッドはスーパークラスから呼び出されます．
	 * サブクラスはHTTPメッセージの1行目を返します．
	 *
	 * @return HTTPメッセージの1行目
	 */
	public abstract String getHeadLine();

	/**
	 * ヘッドラインの読み込み．
	 * このメソッドはスーパークラスから呼び出されます．
	 * サブクラスはHTTPメッセージの1行目を解析するために，このメソッドをオーバーライドします．
	 *
	 * @param line HTTPメッセージの1行目
	 * @return 解析に成功した場合はtrue
	 */
	protected abstract boolean readHeadLine(final String line);


	/**
	 * メッセージボディの読み込み．
	 * このメソッドはスーパークラスから呼び出されます．
	 * サブクラスは，渡されたストリームからメッセージボディを作成します．
	 *
	 * @param input メッセージボディが入ったストリーム
	 * @return メッセージボディオブジェクト
	 * @throws IOException
	 */
	protected abstract Body readBody(final InputStream input) throws IOException;

}