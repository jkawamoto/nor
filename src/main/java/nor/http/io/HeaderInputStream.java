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

import java.io.IOException;
import java.io.InputStream;

import nor.util.log.Logger;

import static nor.http.io.Chars.*;

/**
 * ヘッダのみを読み込むストリーム．
 * ヘッダとボディは空行により分けられている．
 * このストリームは空行まで読み込むとEOFを返し，それ以降の読み込みを行わない．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HeaderInputStream extends InputStream{

	private final InputStream in;

	/** 状態変数
	 *  Default		通常
	 *  FirstCR		1回目のCR
	 *  FirstLF		1回目のLF
	 *  SecondCR	2回目のCR
	 *  SecondLF	2回目のLF
	 *  ただし、いきなり改行がくる可能性もある（ヘッダが空の場合）従って初期状態は SecondCR から始まる．
	 */
	private enum State{
		Default, FirstCR, FirstLF, SecondCR, SecondLF, NULL
	}
	private State state = State.SecondCR;
	private int nullCount = 0;

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(HeaderInputStream.class);

	//====================================================================
	//  Constractor
	//====================================================================
	public HeaderInputStream(final InputStream in){

		this.in = in;

	}

	//====================================================================
	//  Public methods
	//====================================================================
	/* (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
	@Override
	public int read() throws IOException {
		LOGGER.entering("read");

		if(this.state == State.SecondLF){

			return -1;

		}

		int ret = this.in.read();
		switch(this.state){
		case Default:

			switch(ret){
			case CR:

				this.state = State.FirstCR;
				break;

			case 0:

				this.state = State.NULL;
				break;

			}
			break;

		case FirstCR:

			switch(ret){
			case LF:

				this.state = State.FirstLF;
				break;

			case 0:

				this.state = State.NULL;
				break;

			default:

				this.state = State.Default;
				break;

			}
			break;

		case FirstLF:

			switch(ret){
			case CR:

				this.state = State.SecondCR;
				break;

			case 0:

				this.state = State.NULL;
				break;

			default:

				this.state = State.Default;
				break;

			}
			break;

		case SecondCR:

			switch(ret){
			case LF:

				this.state = State.SecondLF;
				break;

			case 0:

				this.state = State.NULL;
				break;

			default:

				this.state = State.Default;
				break;

			}
			break;

		case NULL:

			switch(ret){
			case 0:

				if(++nullCount > 100){

					ret = -1;

				}

			default:

				nullCount = 0;
				this.state = State.Default;
				break;

			}

		}
		if(ret == -1){

			this.state = State.SecondLF;

		}

		LOGGER.exiting("read", ret);
		if(ret == 0 && nullCount > 0) System.out.println(Thread.currentThread().getName() + " recieve null packet:" + nullCount);
		return ret;

	}

	public boolean markSupported() {
		return false;
	}

	public long skip(long n) throws IOException {

		long i = 0;
		for(; i != n; ++i){

			if(this.read() == -1){

				break;

			}

		}

		return i;

	}

	/**
	 * 状態をクリアします．
	 *
	 */
	public void clearState(){

		this.state = State.SecondCR;

	}


}


