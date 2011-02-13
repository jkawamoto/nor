/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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
package nor.core.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;

/**
 * プラグインが実装すべきインターフェイス
 *
 * プラグインは，メッセージハンドラ，リクエストフィルタそしてレスポンスフィルタの三機能を提供することができます．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface Plugin extends Closeable{

	/**
	 * 与えられた設定ファイルを元に初期化する．
	 * このメソッドはフレームワークから呼び出されます．
	 * nor では，ユーザが使用しているネットワーク環境ごとに別々の設定ファイルを使用します．
	 * そのため，設定を保存するためにはこのメソッドに渡されるファイルを使用して下さい．
	 *
	 * @param common ネットワーク環境に依らない共通設定ファイル
	 * @param local 現在使用しているネットワーク毎に異なる設定ファイル
	 */
	public void init(File common, File local) throws IOException;

	/**
	 * プラグインの終了処理を行う．
	 * システムが終了プロセスに入った場合に呼び出されます．
	 * プラグイン設定の保存はこのメソッドで行って下さい．
	 * 保存先のファイルはあらためて通知されないため，init メソッドの引数を記録しておいて下さい．
	 *
	 */
	@Override
	public void close() throws IOException;

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.MessageHandler メッセージハンドラ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.MessageHandler メッセージハンドラ}の配列，メッセージハンドラを提供しない場合は null．
	 */
	public MessageHandler[] messageHandlers();

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.RequestFilter リクエストフィルタ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.RequestFilter リクエストフィルタ}の配列，リクエストフィルタを提供しない場合は null．
	 */
	public RequestFilter[] requestFilters();

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.ResponseFilter レスポンスフィルタ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.ResponseFilter レスポンスフィルタ}の配列，レスポンスフィルタを提供しない場合は null．
	 */
	public ResponseFilter[] responseFilters();

}
