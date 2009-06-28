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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import nor.http.streams.ChunkedInputStream;
import nor.http.streams.ChunkedOutputStream;
import nor.util.CopyingOutputStream;
import nor.util.LimitedInputStream;

/**
 * HTTPメッセージボディを表す抽象クラス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class Body2 {

	/**
	 * このメッセージボディを所有するHTTPメッセージ
	 */
	private final Message _parent;

	/**
	 * メッセージボディの入力ストリーム
	 */
	private InputStream _in;

	private static int BufferSize = 10240;


	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Body2.class.getName());


	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * このメッセージボディを持つparentと入力ストリームinを指定してBodyを作成する．
	 *
	 * @param parent このメッセージボディを持つ親メッセージ
	 * @param in メッセージボディを含む入力ストリーム
	 */
	public Body2(final Message parent, final InputStream in){
		assert parent != null;
		assert in != null;

		this._parent = parent;
		this._in = in;

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

		return this._parent.getHeader().getContentType();

	}


	public IOStreams getIOStreams() throws IOException{

		InputStream in = this._in;

		// 転送コーディングの解決
		final Header header = this._parent.getHeader();
		if(header.containsKey(HeaderName.TransferEncoding)){

			in = new ChunkedInputStream(in);
			header.remove(HeaderName.TransferEncoding);

		}else if(header.containsKey(HeaderName.ContentLength)){

			final int length = Integer.valueOf(header.get(HeaderName.ContentLength));
			in = new LimitedInputStream(in, length);
			header.remove(HeaderName.ContentLength);

		}

		// 内容コーディングの解決
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if("gzip".equalsIgnoreCase(encode)){

				in = new GZIPInputStream(in);

			}else if("deflate".equalsIgnoreCase(encode)){

				in = new DeflaterInputStream(in);

			}
			header.remove(HeaderName.ContentEncoding);

		}

		// 出力をBodyの入力へ繋ぐ
		final PipedOutputStream out = new PipedOutputStream();
		this._in = new PipedInputStream(out);

		assert in != null;
		assert out != null;
		return new IOStreams(in, out);

	}

	void writeBody(final OutputStream output) throws IOException{

		OutputStream out = output;
		InputStream in = this._in;

		final Header header = this._parent.getHeader();

		// 転送エンコーディング，転送サイズの両方が指定されていない場合，チャンク形式で転送する
		// 注： 既に転送エンコーディングが指定されているなら，データはエンコーディングが施されている
		if(header.containsKey(HeaderName.ContentLength)){

			final int length = Integer.valueOf(header.get(HeaderName.ContentLength));
			in = new LimitedInputStream(in, length);

		}else if(header.containsValue(HeaderName.TransferEncoding, "chunked")){

			in = new ChunkedInputStream(in);
			out = new ChunkedOutputStream(out, BufferSize);

		}else{

			out = new ChunkedOutputStream(out, BufferSize);
			header.set(HeaderName.TransferEncoding, "chunked");


		}

		// TODO: Length-requiredの場合はここで全データを受信する．
		final CopyingOutputStream cout = new CopyingOutputStream(out);
		final byte[] buffer = new byte[BufferSize];
		int n = -1;
		while((n = in.read(buffer)) != -1){

			// ボディの書き出し
			cout.write(buffer, 0, n);
			cout.flush();

		}
		cout.flush();

	}


	public class IOStreams{

		public final InputStream in;
		public final OutputStream out;

		private IOStreams(final InputStream in, final OutputStream out){

			this.in = in;
			this.out = out;

		}

		public void close() throws IOException{

			in.close();
			out.close();

		}

		public Reader getReader(){

			// 文字コードの判定
			final ContentType type = _parent.getHeader().getContentType();
			if(ContentType.UNKNOWN.equals(type.getCharset())){

				return new InputStreamReader(this.in);

			}else{

				try {

					return new InputStreamReader(this.in, type.getCharset());

				} catch (final UnsupportedEncodingException e) {

					LOGGER.warning("Unsupported encoding " + e.getLocalizedMessage());

					return new InputStreamReader(this.in);

				}

			}

		}

	}

}
