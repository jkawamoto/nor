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

import nor.util.CopyingInputStream;

/**
 * ヘッダのみを読み込む入力ストリームフィルタ．
 * ヘッダとボディは空行により分けられている．
 * このストリームは空行まで読み込むとEOFを返し，それ以降の読み込みを行わない．
 *
 * @author KAWAMOTO Junpei
 *
 */
class HeaderInputStream extends InputStream{

	/**
	 * 入力元ストリーム
	 */
	private final CopyingInputStream _in;

	/**
	 * 状態変数．
	 *  CR LF CR LF か LF LFがきたら読み込みを終了する．
	 *  	値		状態
	 *  	0		通常
	 *  	1		1回目のCR
	 *  	2		1回目のLF (初期状態)
	 *  	3		2回目のCR
	 *  	4		2回目のLF
	 *  ヘッダが空の場合，いきなりCR LFが来る．そのため，初期状態は2から始まる．
	 */
	private int _state = 2;

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * 入力ストリームinからヘッダ部分のみを読み込むHeaderInputStreamを作成する．
	 *
	 * @param in 入力対象のストリーム
	 */
	public HeaderInputStream(final InputStream in){

		this._in = new CopyingInputStream(in);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {

		int ret = -1;
		if(this._state != 4){

			ret = this._in.read();
			if(ret == Chars.CR){

				switch(this._state){
				case 0:

					this._state = 1;
					break;

				case 1:

					this._state = 0;
					break;

				case 2:

					this._state = 3;
					break;

				case 3:

					this._state = 0;

				}

			}else if(ret == Chars.LF){

				switch(this._state){
				case 0:

					this._state = 2;
					break;

				case 1:

					this._state = 2;
					break;

				case 2:

					this._state = 4;
					break;

				case 3:

					this._state = 4;

				}

			}else{

				this._state = 0;

			}

		}

		return ret;

	}

	/* (非 Javadoc)
	 * @see java.io.InputStream#markSupported()
	 */
	@Override
	public boolean markSupported() {

		return false;

	}

	/* (非 Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
	@Override
	public long skip(long n) throws IOException {

		long i = 0;
		for(; i != n; ++i){

			if(this.read() == -1){

				break;

			}

		}

		return i;

	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){

		return new String(this._in.copy());

	}

	/**
	 * 状態をクリアする．
	 *
	 */
	public void clearState(){

		this._state = 2;

	}

}

