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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import nor.http.streams.ChunkedInputStream;
import nor.util.LimitedInputStream;

/**
 * バイナリデータであるメッセージボディ．
 * 転送エンコーディング，内容エンコーディングは自動で解読される.
 *
 * @author KAWAMOTO Junpei
 *
 */
public class BinaryBody extends Body{

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * このメッセージボディを持つメッセージparentと入力ストリームinputを指定してBinaryBodyを作成する．
	 *
	 * @param parent このメッセージボディを持つHTTPメッセージ
	 * @param input 読み込むストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	BinaryBody(final Message parent, final InputStream input) throws IOException{
		super(parent, input);

		final Header header = this.parent.getHeader();

		// 転送コーディングの解決
		if(header.containsKey(HeaderName.TransferEncoding)){

			this.in = new ChunkedInputStream(this.in);
			header.remove(HeaderName.TransferEncoding);

		}else if(header.containsKey(HeaderName.ContentLength)){

			final int length = Integer.valueOf(header.get(HeaderName.ContentLength));
			this.in = new LimitedInputStream(this.in, length);

		}

		// 内容コーディングの解決
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if("gzip".equalsIgnoreCase(encode)){

				this.in = new GZIPInputStream(this.in);

			}else if("deflate".equalsIgnoreCase(encode)){

				this.in = new DeflaterInputStream(this.in);

			}
			header.remove(HeaderName.ContentEncoding);

		}

		header.set(HeaderName.TransferEncoding, "chunked");
		header.remove(HeaderName.ContentLength);

	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 * フィルタリング用ストリームセットの取得．
	 *
	 * @return ストリームセット
	 * @throws IOException I/Oエラーが発生した場合．
	 */
	public synchronized Streams getStreams() throws IOException{

		final InputStream in = this.in;

		final PipedOutputStream out = new PipedOutputStream();
		this.in = new PipedInputStream(out);

		return new Streams(in, out);

	}

	//============================================================================
	//  public クラス
	//============================================================================
	/**
	 * バイナリデータへのフィルタリング用ストリームセット．
	 *
	 * @author KAWAMOTO Junpei
	 *
	 */
	public class Streams implements Closeable{

		/**
		 * フィルタリング対象のメッセージボディ入力ストリーム
		 */
		public final InputStream in;

		/**
		 * フィルタリング後のデータを書き込む出力ストリーム
		 */
		public final OutputStream out;

		/**
		 * 入出力ストリームin, outを指定してStreamsを作成する．
		 *
		 * @param in フィルタリング対象のデータを共有する入力ストリーム
		 * @param out フィルタリング後のデータを格納する出力ストリーム
		 */
		private Streams(final InputStream in, final PipedOutputStream out){
			assert in != null;
			assert out != null;

			this.in = in;
			this.out = out;

		}

		/**
		 * フィルタリングを行わないことを示す．
		 * このメソッドは入力ストリームの内容を出力ストリームへコピーします．
		 */
		public void pass(){

			try{

				final byte[] buffer = new byte[Message.BufferSize];
				int i = 0;
				while((i = this.in.read(buffer, 0, buffer.length)) != -1){

					this.out.write(buffer, 0, i);

				}

			}catch(IOException e){

				e.printStackTrace();

			}

		}

		/**
		 * フィルタリング操作の終了．
		 * Streamsを受け取ったプログラムはフィルタリングの終了を表すために，
		 * このメソッドを呼ぶ必要がある．
		 *
		 * @throws I/Oエラーが発生した場合
		 */
		public void close() throws IOException{

			this.in.close();
			this.out.close();

		}

	}

}
