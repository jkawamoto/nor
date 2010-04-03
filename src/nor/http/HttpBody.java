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
public class HttpBody{

	/**
	 * このメッセージボディを所有するHTTPメッセージ
	 */
	protected final HttpMessage parent;

	protected InputStream in;

	private static final EasyLogger LOGGER = EasyLogger.getLogger(HttpBody.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * 入力ストリームを指定してメッセージボディを作成する．
	 *
	 * @param parent メッセージボディを持つHttpメッセージ
	 * @param input 入力ストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	HttpBody(final HttpMessage parent, final InputStream in) throws IOException{
		LOGGER.entering("<init>", parent, in);
		assert parent != null;
		assert in != null;

		this.parent = parent;
		this.in = in;

		final HttpHeader header = this.parent.getHeader();

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
			if("gzip".equalsIgnoreCase(encode)){

				this.in = new GZIPInputStream(this.in);

			}else if("deflate".equalsIgnoreCase(encode)){

				this.in = new DeflaterInputStream(this.in);

			}

		}

		LOGGER.exiting("<init>");
	}

	//====================================================================
	//  public メソッド
	//====================================================================
//	/**
//	 * ContentTypeを取得する．
//	 *
//	 * @return このオブジェクトを持つメッセージのContentType
//	 */
//	public ContentType getContentType(){
//
//		return this.parent.getHeader().getContentType();
//
//	}

	public void writeOut(OutputStream out) throws IOException{

		final HttpHeader header = this.parent.getHeader();

		// Content-lengthが指定されていればそのまま，指定されていなければchunkで送る．
		if(!header.containsKey(HeaderName.ContentLength)){

			out = new ChunkedOutputStream(out);

		}

		// 内容コーディングが指定されていれば従う
		if(header.containsKey(HeaderName.ContentEncoding)){

			final String encode = header.get(HeaderName.ContentEncoding);
			if("gzip".equalsIgnoreCase(encode)){

				out = new GZIPOutputStream(out, Stream.DEFAULT_BUFFER_SIZE);

			}else if("deflate".equalsIgnoreCase(encode)){

				out = new DeflaterOutputStream(out);

			}

		}

		// TODO: Length-requiredの場合はここで全データを受信する．
		final InputStream body = this.in;
		Stream.copy(body, out);

		out.flush();
		out.close();
		this.in.close();

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

}
