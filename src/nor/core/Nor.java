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
package nor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.core.plugin.Plugin;
import nor.core.proxy.ProxyServer;
import nor.http.server.local.TextResource;
import nor.http.server.proxyserver.Router;
import nor.util.log.Logger;


/**
 * アプリケーションのコアシステム．
 * 実行中は唯一つのインスタンスのみを持つ．また，メイン関数もこのクラスで定義される．
 *
 * @author Junpei Kawamoto
 *
 */
public class Nor{

	/**
	 * ローカルプロキシ
	 */
	private ProxyServer proxy;

	private Router router = new Router();

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
	private static final Logger LOGGER = Logger.getLogger(Nor.class);

	//============================================================================
	//  Constants
	//============================================================================
	private static final String ConfigFileTemplate = "%s.conf";
	private static final String DefaultConfigFile = "res/default.conf";
	private static final String LoggindConfigFile = "res/logging.conf";

	//============================================================================
	//  Constractor
	//============================================================================
	/**
	 * @throws IOException
	 * @throws FileNotFoundException
	 *
	 */
	private Nor(){
		LOGGER.entering("<init>");

		final Class<?> c = this.getClass();

		// Load logging config
		if(!System.getProperties().containsKey("java.util.logging.config.file")){

			try {

				Logger.loadConfig(c.getResourceAsStream(LoggindConfigFile));

			} catch (final SecurityException e) {

				LOGGER.warning("<init>", e.getMessage());
				LOGGER.catched(Level.FINE, "<init>", e);

			} catch (final IOException e) {

				LOGGER.warning("<init>", e.getMessage());
				LOGGER.catched(Level.FINE, "<init>", e);

			}

		}

		// Load default constants values
		final Properties def = new Properties();
		try {

			def.load(c.getResourceAsStream(DefaultConfigFile));
			def.putAll(System.getProperties());
			System.getProperties().putAll(def);

		} catch (final IOException e) {

			LOGGER.warning("<init>", e.getMessage());
			LOGGER.catched(Level.FINE, "<init>", e);

		}

		// Load application configs
		this.confDir = new File(String.format("./config/%s/", this.context.getHostName()));
		this.confDir.mkdirs();

		this.config = new Config(new File(this.confDir, String.format("%s.conf", this.getClass().getCanonicalName())));

		LOGGER.exiting("<init>");
	}


	//============================================================================
	//  Private methods
	//============================================================================
	//----------------------------------------------------------------------------
	//  準備
	//----------------------------------------------------------------------------
	private void init(){
		LOGGER.entering("init");

		/*
		 * Create a proxy server.
		 */
		this.proxy = new ProxyServer(Nor.class.getSimpleName(), this.router);

		/*
		 * Add plugins which are on the classpath
		 */
		for(final Plugin p : ServiceLoader.load(Plugin.class)){

			if(this.config.isEnable(p)){

				p.setExternalProxies(this.router);

				final File config = new File(this.confDir, String.format(ConfigFileTemplate, p.getClass().getCanonicalName()));
				p.load(config);
				this.proxy.attach(p);
				this.plugins.add(p);

				LOGGER.info("init", "Loading a plugin {0}", p.getClass().getName());

			}

		}

		/*
		 * Load a routing table.
		 */
		final String routings = this.config.get("nor.routing");
		if(routings != null){

			final Pattern pat = Pattern.compile("([^;=]+)=([^;]+)");
			final Matcher m = pat.matcher(routings);
			while(m.find()){

				try{

					final String regex = m.group(1);
					final URL url = new URL(m.group(2));
					this.router.put(regex, url);

				}catch(final MalformedURLException e){

					e.printStackTrace();

				}

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

			p.setExternalProxies(this.router);

			final File config = new File(this.confDir, String.format(ConfigFileTemplate, p.getClass().getCanonicalName()));
			p.load(config);

			this.proxy.attach(p);
			this.plugins.add(p);

			LOGGER.info("loadPlugin", "Loading a plugin {0}", p.getClass().getName());

		}catch(final ClassNotFoundException e){

			LOGGER.warning("loadPlugin", e.getMessage());
			throw new IllegalArgumentException(e);

		} catch (final InstantiationException e) {

			LOGGER.warning("loadPlugin", e.getMessage());
			throw new IllegalArgumentException(e);

		} catch (final IllegalAccessException e) {

			LOGGER.warning("loadPlugin", e.getMessage());
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

		final String addr = this.config.getListenAddress();
		final int port = this.config.getListenPort();

		/*
		 * Register the PAC file.
		 */
		this.proxy.localResourceRoot().add(new TextResource("/nor/core/proxy.pac", this.proxy.getPAC(addr, port, true), "application/x-javascript-config"));

		/*
		 * Start the web server.
		 */
		this.proxy.start(addr, port);

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

			p.close();

			// final File config = new File(this.confDir, String.format(ConfigFileTemplate, p.getClass().getCanonicalName()));
			// p.save(config);

		}

		// 設定の保存
		// this.config.store();

		LOGGER.exiting("close");
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
		LOGGER.entering("main", (Object[])args);

		LOGGER.info("main", "Start up...");

		// 唯一のインスタンスを作成
		final Nor nor = new Nor();

		// 初期化
		nor.init();

		// コマンドライン引数の解釈
		for(int i = 0; i < args.length; ++i){

			if(args[i].equals("-r") && ++i != args.length){

				nor.router.put(".*", new URL(args[i]));

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

			// 終了を待つ
			System.in.read();

			// 終了処理
			nor.close();

		} catch (final IOException e) {

			LOGGER.severe("main", e.getMessage());

		}
		LOGGER.info("main", "End.");

		LOGGER.exiting("main");
	}

}
