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
package nor.core.plugin;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;
import nor.util.Querable;

/**
 * すべてのプラグインの基底クラス．
 *
 * プラグインは，メッセージハンドラ，リクエストフィルタそしてレスポンスフィルタの三機能を提供することができます．
 * それぞれは，次のように働きます．
 * <pre>
 *
 * Client(Browser)
 *
 * </pre>
 *
 * プラグインの設定は， properties フィールドを用いてください．
 * 将来的には，コンピュータが接続しているネットワークごとに異なる設定を保存することができるようになります．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public abstract class Plugin implements Closeable{

	/**
	 * プラグインの設定が格納されます．
	 */
	protected final Properties properties = new Properties();

	protected Querable<Proxy> externalProxies;

	/**
	 * 設定ファイルを読み込む．
	 * このメソッドはフレームワークから呼び出されます．
	 * デフォルトでは，Properties 形式のファイルを仮定しており，フィールド properties に読み込みます．
	 * 異なる形式の設定ファイルを使用する場合は，このメソッドをオーバーライドしてください．
	 *
	 * @param dir 設定ファイル
	 */
	public void load(final File conf){

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
	 * このメソッドはフレームワークから呼び出されます．
	 * デフォルトでは，Properties 形式のファイルを仮定しており，フィールド properties の内容を書き出します．
	 * 異なる形式の設定ファイルを使用する場合は，このメソッドをオーバーライドしてください．
	 *
	 * @param dir 設定ファイル
	 */
	public void save(final File conf){

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

	/**
	 * リモートプロキシへテーブルを設定する．
	 * このメソッドはフレームワークから呼び出され，プラグイン開発者が使用する必要はありません．
	 * また，オーバーライドすることもできません．
	 *
	 * @param externalProxies 設定するリモートプロキシテーブル
	 */
	public final void setExternalProxies(final Querable<Proxy> externalProxies){

		this.externalProxies = externalProxies;

	}

	/**
	 * コネクションを開く．
	 * プラグインが独自にリモートサーバへ接続する場合，このメソッドを用いて下さい．
	 *
	 * @param url 接続先 URL
	 * @return URL コネクションオブジェクト
	 * @throws IOException I/O エラーが発生した場合
	 */
	protected URLConnection openConnection(final URL url) throws IOException{

		if(this.externalProxies != null){

			return url.openConnection(this.externalProxies.query(url.toString()));

		}else{

			return url.openConnection();

		}

	}


}
