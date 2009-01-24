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

/*
 * CR LF CR LFか LF LFがきたら終了
 *
 * Stateパターンを作る＞最終目標
 *
 */

/**
 * ヘッダのみを読み込むストリーム．
 * ヘッダとボディは空行により分けられている．
 * このストリームは空行まで読み込むとEOFを返し，それ以降の読み込みを行わない．
 *
 * @author KAWAMOTO Junpei
 *
 */
class HeaderInputStream extends InputStream{

	private static final int LF = 0x0A;
	private static final int CR = 0x0D;

	private final InputStream _in;

	/** 状態変数
	 *  0		通常
	 *  1		1回目のCR
	 *  2		1回目のLF
	 *  3		2回目のCR
	 *  4		2回目のLF
	 *  ただし、いきなり改行がくる可能性もある（ヘッダが空の場合）従って初期状態は2から始まる．
	 */
	private int _state = 2;

	public HeaderInputStream(final InputStream in){

		this._in = in;

	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {

		int ret = -1;
		if(this._state != 4){

			ret = this._in.read();
			if(ret == CR){

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

			}else if(ret == LF){

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

	/**
	 * 状態をクリアします．
	 *
	 */
	public void clearState(){

		this._state = 0;

	}


}


