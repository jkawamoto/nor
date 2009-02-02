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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import nor.util.LimitedInputStream;

import org.mozilla.universalchardet.UniversalDetector;

/**
 * テキストとして扱えるメッセージボディ．
 * メッセージボディのバイト列をデコードし文字列として扱う．
 * ヘッダに適切な文字コードが指定されていない場合は自動判別する．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class TextBody extends Body{

	/**
	 * メッセージボディのリーダ
	 */
	private Reader _reader = null;

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * このメッセージボディを持つメッセージparentと入力ストリームinputを指定してTextBodyを作成する．
	 *
	 * @param parent メッセージボディを持つHttpメッセージ
	 * @param input 読み込むストリーム
	 * @throws IOException I/Oエラーが発生した場合
	 */
	TextBody(final Message parent, final InputStream input) throws IOException{
		super(parent, input);

		final Header header = parent.getHeader();

		// 転送エンコーディングの解決
		if(header.containsKey(HeaderName.TransferEncoding)){

			header.remove(HeaderName.TransferEncoding);
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
			header.remove(HeaderName.ContentEncoding);

		}

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

		// ストリームリーダが無ければ作成する
		if(this._reader == null){

			this._reader = new DetectingInputStreamReader(this.parent.getHeader(), this.in);
			this.in = null;

		}

		final Reader reader = this._reader;
		final PipedWriter writer = new PipedWriter();
		this._reader = new PipedReader(writer);

		return new Streams(reader, writer);

	}

	//============================================================================
	//  package private メソッド
	//============================================================================
	/* (非 Javadoc)
	 * @see nor.http.Body#getInputStream()
	 */
	@Override
	InputStream getInputStream(){

		if(this.in == null){

			try {

				final PipedOutputStream out = new PipedOutputStream();
				this.in = new PipedInputStream(out);

				final Thread t = new Thread(new TransferWorker(out, this.parent.getHeader().getContentType().getCharset()));
				t.start();

			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

		}

		final Header header = parent.getHeader();
		header.set(HeaderName.TransferEncoding, "chunked");
		header.remove(HeaderName.ContentLength);

		return this.in;

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
		 * フィルタリング対象のストリームリーダ
		 */
		public final Reader in;

		/**
		 * フィルタリング後のデータを書き込むストリームライタ
		 */
		public final Writer out;

		/**
		 * 入出力ストリームin, outを指定してStreamsを作成する．
		 *
		 * @param in フィルタリング対象のストリームリーダ
		 * @param out フィルタリング後のデータを書き込むストリームライタ
		 */
		private Streams(final Reader in, final PipedWriter out){

			this.in = in;
			this.out = out;

		}

		/**
		 * フィルタリングを行わないことを示す．
		 * このメソッドは入力ストリームの内容を出力ストリームへコピーする．
		 */
		public void pass(){

			try{

				final char[] buffer = new char[1024*1024];
				int i = 0;
				while((i = this.in.read(buffer, 0, buffer.length)) != -1){

					this.out.write(buffer, 0, i);

				}

			}catch(IOException e){

				e.printStackTrace();

			}

		}

		/* (非 Javadoc)
		 * @see java.io.Closeable#close()
		 */
		@Override
		public void close() throws IOException{

			this.in.close();
			this.out.close();

		}

	}

	//============================================================================
	//  private クラス
	//============================================================================
	private class DetectingInputStreamReader extends Reader{

		private final Reader _in;

		public DetectingInputStreamReader(final Header header, final InputStream input) throws IOException{

			final UniversalDetector detector = new UniversalDetector(null);

			// 読み込んだバイト列
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream(Message.BufferSize);
			final byte[] buf = new byte[Message.BufferSize];
			for(int n; (n = input.read(buf)) != Chars.EOF;){

				bytes.write(buf, 0, n);
				if(!detector.isDone()){

					detector.handleData(buf, 0, n);

				}

			}

			// 入力終了
			detector.dataEnd();

			// 文字コードの判定
			final ContentType type = header.getContentType();

			// ヘッダに示されていなければアルゴリズムを利用する
			if(ContentType.UNKNOWN.equals(type.getCharset())){

				final String enc = detector.getDetectedCharset();
				if(enc == null){

					//LOGGER.warning("文字コードが判別できません");
					type.setCharset(ContentType.UNKNOWN);

					this._in = new StringReader(new String(bytes.toByteArray()));

				}else{

					type.setCharset(enc);
					this._in = new StringReader(new String(bytes.toByteArray(), enc));

				}

			}else{

				this._in = new StringReader(new String(bytes.toByteArray(), type.getCharset()));

			}

			header.set("x-nor-chartype", type.getCharset());

		}

		@Override
		public void close() throws IOException {

			this._in.close();

		}

		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {

			return this._in.read(cbuf, off, len);

		}

	}

	/**
	 * @author KAWAMOTO Junpei
	 *
	 */
	private class TransferWorker implements Runnable{

		private String _enc;
		private OutputStream _out;

		public TransferWorker(final OutputStream out, final String enc){

			this._enc = enc;
			this._out = out;

		}

		@Override
		public void run() {

			try{

				OutputStreamWriter writer;
				if(this._enc == ContentType.UNKNOWN){

					writer = new OutputStreamWriter(this._out);

				}else{

					writer = new OutputStreamWriter(this._out, this._enc);

				}

				final char[] buf = new char[Message.BufferSize];
				for(int n; (n = _reader.read(buf)) != Chars.EOF;){

					writer.write(buf, 0, n);
					writer.flush();

				}
				writer.close();

			}catch(IOException e){

				e.printStackTrace();

			}finally{

				try {

					this._out.close();

				} catch (IOException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}

		}

	}

}
