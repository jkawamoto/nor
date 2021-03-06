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
package nor.http.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static nor.http.io.Chars.*;
import nor.util.io.SequentialInputStream;


public class ChunkedInputStream extends SequentialInputStream{

	/**
	 *  読み出した合計サイズ
	 */
	private int size = 0;

	/**
	 * バッファ
	 */
	private byte[] buffer = new byte[0];

	/**
	 * バッファインデックス
	 */
	private int counter = 0;

	/**
	 *  カレントチャンクの残りサイズ
	 */
	private int remains = 0;

	/**
	 *  カレントチャンクから読み出したサイズ
	 */
	private int readed = 0;

	/**
	 *  ストリームの終わりに達したか
	 */
	private boolean isEOF = false;

	/**
	 *  トレイラも考慮する
	 */
	private final Map<String, String> trailers = new HashMap<String, String>();

	/**
	 * チャンク形式をデコードするストリームを作成する.
	 *
	 * @param in 読み出し元のストリーム
	 */
	public ChunkedInputStream(final InputStream in){

		super(in);

	}

	/* (非 Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {

		// ストリームの終わりに達していたら
		if(this.isEOF){

			return -1;

		}

		// バッファにデータが残っていない場合
		if(this.available() == 0){

			// ストリームの終わりに達していたら終了
			if(!this.reload()){

				return -1;

			}

		}

		++this.size;
		return this.buffer[this.counter++] & 0xff;

	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		if(off < 0){

			throw new IndexOutOfBoundsException("offが負");

		}else if(len < 0){

			throw new IndexOutOfBoundsException("lenが負");

		}else if(len > b.length - off){

			throw new IndexOutOfBoundsException("lenがb.length - offよりも大きい");

		}


		// ストリームの終わりに達していたら
		if(this.isEOF){

			return -1;

		}

		// バッファにデータが残っていない場合
		if(this.available() == 0){

			// ストリームの終わりに達していたら終了
			if(!this.reload()){

				return -1;

			}

		}

		// this._bufferのthis._counterから，availableとlenの小さい方分だけコピーする
		final int available = this.available();
		if(len > available){

			for(int i = 0; i != available; ++i){

				++this.size;
				b[off + i] = this.buffer[this.counter++];

			}

			return available;

		}else{

			for(int i = 0; i != len; ++i){

				++this.size;
				b[off + i] = this.buffer[this.counter++];

			}

			return len;

		}

	}

	/* (非 Javadoc)
	 * @see java.io.InputStream#available()
	 */
	@Override
	public int available(){

		return this.readed - this.counter;

	}

	/* (non-Javadoc)
	 * @see java.io.FilterInputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {

		if(n > 0){

			final int available = this.available();
			if(n > available){

				if(this.reload()){

					return this.skip(n - available) + available;

				}else{

					return available;

				}

			}else{

				this.counter += n;
				return n;

			}

		}

		return 0;

	}

	/**
	 * ストリームから読み出した合計サイズ.
	 *
	 * @return 読み出した合計サイズ．
	 */
	public int size(){

		return this.size;

	}

	/**
	 * トレイラを返す.
	 * チャンク形式ストリームに付加されるトレイラを返す.
	 *
	 * @return トレイラを格納したマップ
	 */
	public Map<String, String> getTrailers(){

		return this.trailers;

	}

	/**
	 * @return
	 * @throws IOException
	 */
	private boolean reload() throws IOException{

		// 現在のチャンクからすべてのデータを読み出していたら
		if(this.remains == 0){

			// 次のチャンクを解析
			this.analyze();

			// ストリームの終わりに達していたら終了
			if(this.isEOF){

				return false;

			}

		}

		// チャンクから読み出す
		this.readed = this.in.read(this.buffer, 0, this.remains);
		this.remains -= this.readed;
		this.counter = 0;

		return true;

	}

	/**
	 * チャンクを解析する．
	 * 次のチャンクに関する情報を取得する.
	 *
	 * @throws IOException ストリームからの読み込みエラーが発生した場合.
	 */
	private void analyze() throws IOException{

		try{

			final List<Byte> head = new ArrayList<Byte>();
			Read: while(true){

				final int buf = this.in.read();
				switch(buf){
				case CR:

					continue;

				case LF:

					if(head.size() != 0){

						break Read;

					}

					break;

				case EOF:

					this.isEOF = true;
					return;

				default:

					head.add((byte)buf);

				}

			}

			// チャンクヘッダをバイト配列から文字列へ変換する.
			final byte[] bhead = new byte[head.size()];
			for(int i = 0; i != head.size(); ++i){

				bhead[i] = head.get(i);

			}
			final String shead = new String(bhead, "UTF-8");

			// チャンクヘッダをサイズとパラメータへ分割する.
			final String[] sp = shead.split(";");

			// チャンクサイズの取得
			final String size = sp[0].trim();
			this.remains = Integer.parseInt(size, 16);
			this.buffer = new byte[this.remains];

			// 最終チャンク（サイズが0）の場合
			if(this.remains == 0){

				// これ以降ストリームには空行で終わるトレイラが付く
				final BufferedReader input = new BufferedReader(new InputStreamReader(new HeaderInputStream(this.in)));
				String buf;
				while((buf = input.readLine()) != null){

					if(buf.equals("")){

						break;

					}

					final String[] kv = buf.split(":");
					if(kv.length != 1){

						final String key = kv[0].trim().toLowerCase();
						final String value = kv[1].trim();

						this.trailers.put(key, value);

					}

				}

				this.isEOF = true;

			}

		}catch(IOException e){

			System.err.println("ChunkedStream.decode: " + e.getLocalizedMessage() + "(" + e.hashCode() + ")");
			throw e;

		}catch(NumberFormatException e){

			System.err.println("ChunkedStream.decode: " + e.getLocalizedMessage() + "(" + e.hashCode() + ")");
			throw new IOException("不正なデータを受信しました");

		}

	}

}
