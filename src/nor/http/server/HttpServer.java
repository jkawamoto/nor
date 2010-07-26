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
package nor.http.server;

import java.io.Closeable;
import java.io.IOException;

public interface HttpServer extends Closeable{

	/**
	 * 待ち受けるホスト名とポート番号を指定してサーバを起動する．
	 * サーバは別スレッドによって動作します．このメソッドはサーバが起動後すぐに処理を戻します．
	 *
	 * @param hostname 待ち受けホスト名
	 * @param port 待ち受けポート番号
	 * @throws IOException I/O エラーが発生した場合
	 */
	public void start(final String hostname, final int port) throws IOException;

	/**
	 * サーバを閉じる．
	 *
	 * @throws IOException I/O エラーが発生した場合．
	 */
	@Override
	public void close() throws IOException;

}
