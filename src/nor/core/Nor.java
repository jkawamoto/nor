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
package nor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.regex.Pattern;

import nor.core.plugin.Plugin;
import nor.core.proxy.ProxyServer;
import nor.util.log.EasyLogger;


/**
 * アプリケーションのコアシステム．
 * 実行中は唯一つのインスタンスのみを持つ．また，メイン関数もこのクラスで定義される．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class Nor{

	/**
	 * 唯一のインスタンス
	 */
	private static Nor instance;


	/**
	 * ローカルプロキシ
	 */
	private ProxyServer proxy;

	private final File confDir;

	/**
	 * 設定
	 */
	private final Config config;

	/**
	 * システムコンテキスト
	 */
	private final Context context = new Context();

	/**
	 * プラグインのリスト
	 */
	private final List<Plugin> plugins = new ArrayList<Plugin>();

	/**
	 * ロガー．
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(Nor.class);

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 *
	 */
	private Nor(){
		LOGGER.entering("<init>");

		this.confDir = new File(String.format("./config/%s/", this.context.getMAC()));
		if(!this.confDir.exists()){

			this.confDir.mkdirs();

		}

		this.config = new Config(new File(this.confDir, this.getClass().getCanonicalName() + ".conf"));

		LOGGER.exiting("<init>");
	}


	//============================================================================
	//  private メソッド
	//============================================================================
	//----------------------------------------------------------------------------
	//  準備
	//----------------------------------------------------------------------------
	private void init(){
		LOGGER.entering("init");

		this.proxy = new ProxyServer(Nor.class.getSimpleName());

		// クラスパス上にあるプラグインを追加
		for(final Plugin p : ServiceLoader.load(Plugin.class)){

			if(this.config.isEnable(p)){

				p.load(this.confDir);
				this.proxy.attach(p);
				this.plugins.add(p);

			}

		}

		LOGGER.exiting("init");
	}

	/**
	 * 完全修飾名を指定してプラグインを読み込む．
	 * このメソッドで読み込まれたプラグインは設定ファイルに関係なく強制的に有効となる．
	 *
	 * @param classname
	 * @throws IllegalArgumentException
	 */
	private void loadPlugin(final String classname) throws IllegalArgumentException{
		LOGGER.entering("loadPlugin", classname);

		try{

			final Class<?> c = Class.forName(classname);
			final Plugin p = (Plugin)c.newInstance();

			p.load(this.confDir);
			this.proxy.attach(p);
			this.plugins.add(p);

		}catch(final ClassNotFoundException e){

			LOGGER.warning(e.getMessage());
			throw new IllegalArgumentException(e);

		} catch (final InstantiationException e) {

			LOGGER.warning(e.getMessage());
			throw new IllegalArgumentException(e);

		} catch (final IllegalAccessException e) {

			LOGGER.warning(e.getMessage());
			throw new IllegalArgumentException(e);

		}

		LOGGER.exiting("loadPlugin");
	}

	//----------------------------------------------------------------------------
	//  システムの実行
	//----------------------------------------------------------------------------
	/**
	 * サーバを開始する．
	 * @throws IOException
	 */
	private void start() throws IOException{
		LOGGER.entering("start");

		this.proxy.start(this.config.getListenAddress(), this.config.getListenPort());

		LOGGER.exiting("start");
	}

	//----------------------------------------------------------------------------
	//  終了処理
	//----------------------------------------------------------------------------
	/**
	 * 終了処理を行う．
	 * @throws IOException ファイルの読み書きにエラーが起きた場合
	 *
	 */
	private void close() throws IOException{
		LOGGER.entering("close");

		// サーバの終了
		this.proxy.close();

		// プラグインの終了処理
		for(final Plugin p : this.plugins){

			try{

				p.close();
				p.save(this.confDir);

			}catch(final IOException e){

				LOGGER.warning(e.getMessage());

			}

		}

		// 設定の保存
		this.config.store();

		LOGGER.exiting("close");
	}


	//============================================================================
	//  static メソッド
	//============================================================================
	public static Nor instance(){
		LOGGER.entering(Nor.class.getName(), "getInstance");

		final Nor ret = instance;

		LOGGER.exiting(Nor.class.getName(), "getInstance", ret);
		return ret;
	}

	private static Nor create(){
		LOGGER.entering("create");

		Nor.instance = new Nor();

		LOGGER.exiting("create", Nor.instance);
		return Nor.instance;
	}


	//============================================================================
	//  main
	//============================================================================
	/**
	 * アプリケーションのエントリポイント．
	 * @param args 強制的に読み込むプラグインの完全修飾名リスト
	 * @throws MalformedURLException
	 */
	public static void main(final String[] args) throws MalformedURLException {
		LOGGER.entering("main", args);

		// ロギング設定ファイルがVM引数で与えられていなかった場合デフォルトを設定する
		if(System.getProperty("java.util.logging.config.file") == null){

			System.setProperty("java.util.logging.config.file", "logging.properties");

		}

		// 唯一のインスタンスを作成
		final Nor nor = Nor.create();

		// 初期化
		nor.init();

		// コマンドライン引数の解釈
		for(int i = 0; i < args.length; ++i){

			if(args[i].equals("-r") && ++i != args.length){

				nor.proxy.addRouting(Pattern.compile(".*"), new URL(args[i]));

			}else if(args[i].equals("-p") && ++i != args.length){

				// プラグインの読み込み
				for(final String classname : args[i].split(";")){

					try{

						nor.loadPlugin(classname);

					}catch(final IllegalArgumentException e){

						e.printStackTrace();

					}

				}

			}

		}

		try{

			// サーバの起動
			nor.start();

			System.in.read();

			// 終了処理
			nor.close();

		} catch (final IOException e) {

			LOGGER.severe(e.getMessage());

		}

		LOGGER.exiting("main");
	}

}


