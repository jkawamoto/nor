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
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nor.http.io.ChunkedInputStream;
import nor.http.io.ChunkedOutputStream;
import nor.util.io.LimitedInputStream;
import nor.util.io.LimitedOutputStream;
import nor.util.io.Stream;
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
public abstract class HttpMessage implements Closeable{

	private static final Logger LOGGER = Logger.getLogger(HttpMessage.class);

	private InputStream body;

	//====================================================================
	//	Constructors
	//====================================================================
	protected HttpMessage(){

	}

	//====================================================================
	//	Public methods
	//====================================================================
	public InputStream getBody(){

		final InputStream ret = this.body;
		this.body = null;

		return ret;

	}

	public void setBody(final InputStream body){

		this.body = body;

	}

	/**
	 * メッセージをストリームに書き出す．
	 * このオブジェクトが表すメッセージをストリームに書き出します．メッセージの書き出しは破壊的操作になります．
	 * プラグイン開発者がこのメソッドを呼ぶことはありません．
	 * メッセージを取得する場合は，ストリームフィルタを使用してください．
	 *
	 * @param output 書き出し先の出力ストリーム
	 * @throws IOException ストリームの書き出しにエラーが発生した場合
	 */
	public void writeTo(final OutputStream output) throws IOException{
		LOGGER.entering("writeMessage", output);
		assert output != null;

		final HttpHeader header = this.getHeader();

		if(this.body == null){

			header.set(HeaderName.ContentLength, "0");
			header.remove(HeaderName.TransferEncoding);

		}

		// ヘッドラインの書き出し
		final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output));
		writer.append(this.getHeadLine());
		writer.append('\r');
		writer.append('\n');
		writer.flush();

		// ヘッダの書き出し
		header.output(writer);

		// バッファのフラッシュ
		writer.append('\r');
		writer.append('\n');
		writer.flush();
		writer.close();

		// ボディの書き出し
		this.writeBodyTo(output);

		LOGGER.exiting("writeMessage");
	}

	public void writeTo(final HttpURLConnection con) throws IOException{

		// リクエストヘッダの登録
		final HttpHeader header = this.getHeader();
		if(this.body == null){

			header.remove(HeaderName.ContentLength);
			header.remove(HeaderName.TransferEncoding);

		}
		for(final String key : header.keySet()){

			con.addRequestProperty(key, header.get(key));

		}

		// ボディがある場合は送信
		if(header.containsKey(HeaderName.ContentLength)){

			final int length = Integer.parseInt(header.get(HeaderName.ContentLength));
			con.setFixedLengthStreamingMode(length);
			con.setDoOutput(true);

			this.writeBodyTo(con.getOutputStream());

		}else if(header.containsKey(HeaderName.TransferEncoding)){

			con.setDoOutput(true);

			this.writeBodyTo(con.getOutputStream());

		}

	}

	@Override
	public void close() throws IOException{

		if(this.body != null){

			this.body.close();

		}

	}

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

	//====================================================================
	//	Private methods
	//====================================================================
	private void writeBodyTo(final OutputStream out) throws IOException{

		if(this.body != null){

			final HttpHeader header = this.getHeader();
			OutputStream cout = out;
			if(header.containsKey(HeaderName.ContentLength)){

				// ContentLengthが指定されていればそのサイズだけ送る
				final String length = header.get(HeaderName.ContentLength).split(",")[0];
				cout = new LimitedOutputStream(cout, Integer.parseInt(length));

			}else if(Http.CHUNKED.equalsIgnoreCase(header.get(HeaderName.TransferEncoding))){

				// TransferEncodingにchunkが指定されていればChunk形式で送る
				cout = new ChunkedOutputStream(cout);

			}

			// 内容コーディングが指定されていれば従う
			if(header.containsKey(HeaderName.ContentEncoding)){

				final String encode = header.get(HeaderName.ContentEncoding);
				if(Http.GZIP.equalsIgnoreCase(encode)){

					cout = new GZIPOutputStream(cout, Stream.DefaultBufferSize);

				}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

					cout = new DeflaterOutputStream(cout);

				}

			}

			Stream.copy(this.body, cout);
			cout.flush();
			cout.close();

		}
		out.close();

	}

	//====================================================================
	//	Package-private static methods
	//====================================================================
	static InputStream decodeStream(final InputStream input, final HttpHeader header){

		InputStream cin = input;

		// 転送エンコーディングの解決
		if(header.containsKey(HeaderName.TransferEncoding)){

			cin = new ChunkedInputStream(cin);

		}else if(header.containsKey(HeaderName.ContentLength)){

			final String length = header.get(HeaderName.ContentLength);
			cin = new LimitedInputStream(cin, Long.valueOf(length));

		}

		// 内容エンコーディングの解決
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if(Http.GZIP.equalsIgnoreCase(encode)){

				try {

					cin = new GZIPInputStream(cin);

				} catch (final IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

				cin = new DeflaterInputStream(cin);

			}

		}

		return cin;

	}

}
