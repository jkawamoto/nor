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

import java.io.BufferedWriter;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * チャンク形式で書き出すストリームフィルタ．
 * バッファサイズに達するか，flushが呼ばれるごとに送信する．
 * トレイラを送信するために，出力が終了した場合はcloseかwriteEOFを必ず呼ぶこと．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class ChunkedOutputStream extends FilterOutputStream{

	/**
	 * デフォルトバッファサイズ
	 */
	public static final int DefaultBufferSize = 4096;

	/**
	 *  バッファ
	 */
	private final byte[] _buffer;

	/**
	 * 現在のバッファへの書き込みサイズ
	 */
	private int _counter;

	/**
	 * トレイラ
	 */
	private final Map<String, String> _trailer = new HashMap<String, String>();

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * デフォルトのバッファサイズごとに出力ストリームoutへチャンク形式で書き出す
	 * ChunkedOutputStreamを作成する．
	 *
	 * @param out 書き出し先のストリーム
	 */
	public ChunkedOutputStream(final OutputStream out){

		this(out, ChunkedOutputStream.DefaultBufferSize);

	}

	/**
	 * サイズbuffer_sizeごとに出力ストリームoutへチャンク形式で書き出す
	 * ChunkedOutputStreamを作成する．
	 *
	 * @param out 書き出し先のストリーム
	 * @param buffer_size 基本書き込みサイズ
	 */
	public ChunkedOutputStream(final OutputStream out, final int buffer_size){

		super(out);
		this._buffer = new byte[buffer_size];
		this._counter = 0;

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {

		this._buffer[this._counter++] = (byte)b;
		if(this._counter == this._buffer.length){

			this.flush();

		}

	}

	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(byte[], int, int)
	 */
	@Override
	public void write(byte[] b, int off, int len) throws IOException {

		// このメソッドは内部バッファを使わず一気に書き込む

		// バッファに保存されているデータを書き込む
		this.flush();

		final String size = Integer.toHexString(len);

		this.out.write(size.getBytes());
		this.out.write(Chars.CR);
		this.out.write(Chars.LF);

		this.out.write(b, off, len);

		this.out.write(Chars.CR);
		this.out.write(Chars.LF);

		this.out.flush();

	}

	/* (非 Javadoc)
	 * @see java.io.FilterOutputStream#flush()
	 */
	@Override
	public void flush() throws IOException {

		if(this._counter != 0){

			final String size = Integer.toHexString(this._counter);

			this.out.write(size.getBytes());
			this.out.write(Chars.CR);
			this.out.write(Chars.LF);

			this.out.write(this._buffer, 0, this._counter);

			this.out.write(Chars.CR);
			this.out.write(Chars.LF);

			this.out.flush();
			this._counter = 0;

		}

	}

	/* (non-Javadoc)
	 * @see java.io.FilterOutputStream#close()
	 */
	@Override
	public void close() throws IOException {

		this.flush();
		this.writeEOF();
		super.close();

	}

	/**
	 * EOFを書き出す.
	 * トレイラを送信するために，書き出すデータが無くなった場合このメソッドかcloseを必ず呼ぶ．
	 *
	 * @throws IOException ストリーム出力中にエラーが発生した場合
	 */
	public void writeEOF() throws IOException{

		this.out.write(Chars.ZERO);
		this.out.write(Chars.CR);
		this.out.write(Chars.LF);

		final BufferedWriter output = new BufferedWriter(new OutputStreamWriter(this.out));
		for(final String key : this._trailer.keySet()){

			output.append(key);
			output.append(": ");
			output.append(this._trailer.get(key));
			output.append("\n");

		}
		output.append("\n");
		output.flush();

	}

	/**
	 * keyに対応するトレイラの値を取得する．
	 *
	 * @param key トレイラのキー
	 * @return keyに関連付けられている値
	 */
	public String getTrailer(final String key){

		return this._trailer.get(key);

	}

	/**
	 * keyに対応するトレイラの値を設定する．
	 * keyに対して既に値が設定されている場合，古い値は上書きされます．
	 *
	 * @param key トレイラのキー
	 * @param value キーに関連付ける値
	 */
	public void setTrailer(final String key, final String value){

		this._trailer.put(key.toLowerCase(), value);

	}

	/**
	 * トレイラにkeyが含まれているか調べる．
	 *
	 * @param key トレイラに含まれているか調べるキー
	 * @return 含まれている場合 true
	 */
	public boolean containsTrailer(final String key){

		return this._trailer.containsKey(key);

	}

	/**
	 * トレイラに含まれるキーの集合を取得する．
	 *
	 * @return トレイラに含まれるキー集合
	 */
	public Set<String> keySet(){

		return this._trailer.keySet();

	}

	/**
	 * トレイラからキーと対応付けられている値を削除する．
	 *
	 * @param key 削除するキー
	 */
	public void removeTrailer(final String key){

		this._trailer.remove(key);

	}

}


