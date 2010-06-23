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
package nor.core.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;

/**
 * すべてのプラグインの基底クラス．
 *
 *
 * プラグインは，メッセージハンドラ，リクエストフィルタ，レスポンスフィルタの三つの機能を提供することができます．
 *
 *
 * @author Junpei Kawamoto
 */
public abstract class Plugin implements Closeable{

	/**
	 *
	 */
	protected final Properties properties = new Properties();

	/**
	 * 設定ファイルを読み込む．
	 * このメソッドはフレームワークから呼び出され，プラグイン開発者が使用する必要はありません．
	 * また，オーバーライドすることもできません．
	 *
	 * 読み込まれた設定には，propertiesフィールドを通してアクセスできます．
	 *
	 * @param dir 設定ファイルが保管されているフォルダ
	 */
	public final void load(final File dir){

		final File conf = new File(dir, String.format("%s.conf", this.getClass().getCanonicalName()));
		if(conf.exists()){

			try {

				this.properties.load(new FileInputStream(conf));

			} catch (final FileNotFoundException e) {

				// TODO 自動生成された catch ブロック
				e.printStackTrace();

			} catch (final IOException e) {

				// TODO 自動生成された catch ブロック
				e.printStackTrace();

			}

		}

		this.init();

	}

	/**
	 * 設定ファイルを保存する．
	 * このメソッドはフレームワークから呼び出され，プラグイン開発者が使用する必要はありません．
	 * また，オーバーライドすることもできません．
	 *
	 * @param dir 設定ファイルを書き出すフォルダ
	 */
	public final void save(final File dir){

		final File conf = new File(dir, String.format("%s.prop", this.getClass().getCanonicalName()));
		try {

			this.properties.store(new FileOutputStream(conf), "");

		} catch (final FileNotFoundException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		} catch (final IOException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}

	}

	/**
	 * プラグインを初期化する．
	 * プラグインがロードされ，設定を読み込んだ後に呼び出されます．
	 */
	public void init(){

	}

	/**
	 * プラグインの終了処理を行う．
	 * システムが終了プロセスに入った場合に呼び出されます．
	 * 設定の保存はこのメソッドの終了後に行われます．
	 * したがって，このメソッド内で properties フィールドへデータを保存することができます．
	 */
	public void close(){

	}

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.MessageHandler メッセージハンドラ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.MessageHandler メッセージハンドラ}の配列，メッセージハンドラを提供しない場合は null．
	 */
	public MessageHandler[] messageHandlers(){

		return null;

	}

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.RequestFilter リクエストフィルタ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.RequestFilter リクエストフィルタ}の配列，リクエストフィルタを提供しない場合は null．
	 */
	public RequestFilter[] requestFilters(){

		return null;

	}

	/**
	 * プラグインが提供する，{@link nor.core.proxy.filter.ResponseFilter レスポンスフィルタ}の配列を返す．
	 *
	 * @return {@link nor.core.proxy.filter.ResponseFilter レスポンスフィルタ}の配列，レスポンスフィルタを提供しない場合は null．
	 */
	public ResponseFilter[] responseFilters(){

		return null;

	}

}
