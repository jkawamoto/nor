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
import java.util.logging.Logger;

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
		Default, FirstCR, FirstLF, SecondCR, SecondLF
	}
	private State state = State.SecondCR;

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(HeaderInputStream.class.getName());

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
		LOGGER.entering(HeaderInputStream.class.getName(), "read");

		int ret = -1;
		switch(this.state){
		case Default:

			ret = this.in.read();
			if(ret == CR){

				this.state = State.FirstCR;

			}
			break;

		case FirstCR:

			ret = this.in.read();
			if(ret == LF){

				this.state = State.FirstLF;

			}else{

				this.state = State.Default;

			}
			break;

		case FirstLF:

			ret = this.in.read();
			if(ret == CR){

				this.state = State.SecondCR;

			}else{

				this.state = State.Default;

			}
			break;

		case SecondCR:

			ret = this.in.read();
			if(ret == LF){

				this.state = State.SecondLF;

			}else{

				this.state = State.Default;

			}
			break;

		}
		if(ret == -1){

			this.state = State.SecondLF;

		}

		LOGGER.exiting(HeaderInputStream.class.getName(), "read", ret);
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


