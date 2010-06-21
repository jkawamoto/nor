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
package nor.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import nor.http.io.ChunkedInputStream;
import nor.http.io.ChunkedOutputStream;
import nor.util.LimitedInputStream;
import nor.util.LimitedOutputStream;
import nor.util.Stream;
import nor.util.log.EasyLogger;


/**
 * Httpボディを表すオブジェクト．
 * Httpメッセージボディはバイト列，文字列のどちらかである．このクラスは，これらデータ型
 * の変換を提供するとともに，ストリームへの入出力メソッドも提供する．
 *
 * <br />
 *
 * このクラスの実装は今後変更になる可能性があります．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HttpBody implements Closeable{

	protected InputStream in;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpBody.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	HttpBody(final InputStream in){

		this.in = in;

	}

	/**
	 * 入力ストリームを指定してメッセージボディを作成する．
	 *
	 * @param input 入力ストリーム
	 * @param parent メッセージボディを持つHttpメッセージ
	 * @throws IOException I/Oエラーが発生した場合
	 */
	HttpBody(final InputStream in, final HttpHeader header) throws IOException{
		this(in);

		// 転送エンコーディングの解決
		if(header.containsKey(HeaderName.TransferEncoding)){

			this.in = new ChunkedInputStream(this.in);

		}else if(header.containsKey(HeaderName.ContentLength)){

			final String length = header.get(HeaderName.ContentLength);
			this.in = new LimitedInputStream(this.in, Integer.valueOf(length));

		}

		// 内容エンコーディングの解決
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if(Http.GZIP.equalsIgnoreCase(encode)){

				this.in = new GZIPInputStream(this.in);

			}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

				this.in = new DeflaterInputStream(this.in);

			}

		}

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	public void output(final OutputStream out) throws IOException{

		if(this.in != null){

			Stream.copy(this.in, out);

		}
		out.flush();

	}

	public void output(final OutputStream out, final HttpHeader header) throws IOException{

		OutputStream cout = out;

		if(header.containsKey(HeaderName.ContentLength)){

			// ContentLengthが指定されていればそのサイズだけ送る
			cout = new LimitedOutputStream(cout, Integer.parseInt(header.get(HeaderName.ContentLength)));

		}else if(Http.CHUNKED.equalsIgnoreCase(header.get(HeaderName.TransferEncoding))){

			// TransferEncodingにchunkが指定されていればChunk形式で送る
			cout = new ChunkedOutputStream(cout);

		}

		// 内容コーディングが指定されていれば従う
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if(Http.GZIP.equalsIgnoreCase(encode)){

				cout = new GZIPOutputStream(cout, Stream.DEFAULT_BUFFER_SIZE);

			}else if(Http.DEFLATE.equalsIgnoreCase(encode)){

				cout = new DeflaterOutputStream(cout);

			}

		}

		this.output(cout);
		cout.close();

	}

	/**
	 * 入力ストリームを取得する．
	 *
	 * @return このメッセージボディに設定されている入力ストリーム
	 */
	public InputStream getStream(){
		LOGGER.entering("getStream");

		LOGGER.exiting("getStream", this.in);
		return this.in;
	}

	/**
	 * 入力ストリームを設定する．
	 *
	 * @param in このメッセージボディに設定する入力ストリーム
	 */
	public void setStream(final InputStream in){
		LOGGER.entering("setStream", in);

		this.in = in;

		LOGGER.exiting("setStream");
	}

	@Override
	public void close() throws IOException {

		this.in.close();

	}

}
