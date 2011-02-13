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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.logging.Level;

import nor.core.plugin.Plugin;
import nor.core.proxy.ProxyServer;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.error.HttpException;
import nor.http.server.local.TextResource;
import nor.http.server.proxyserver.ProxyRequestHandler;
import nor.http.server.proxyserver.Router;
import nor.util.io.Stream;
import nor.util.log.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.UnrecognizedOptionException;


/**
 * アプリケーションのコアシステム．
 * 実行中は唯一つのインスタンスのみを持つ．また，メイン関数もこのクラスで定義される．
 *
 * @author Junpei Kawamoto
 *
 */
public class Nor{

	/**
	 * Local proxy server.
	 */
	private ProxyServer server;

	/**
	 * Proxy request handler.
	 */
	private final ProxyRequestHandler handler;

	/**
	 * Request router.
	 */
	private final Router router = new Router();

	//
	private final File rootConfDir;
	private final File localConfDir;
	//

	private static final class Key{

		public static final String LISTEN_ADDRESS = "nor.address";
		public static final String LISTEN_PORT = "nor.port";

	}

	private static final class Template{

		public static final String PLUGIN_ENABEL = "%s.enable";

	}


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
	private static final String LoggindConfigFile = "logging.conf";

	//============================================================================
	//  Constractor
	//============================================================================
	private Nor(final String rootConfDir){
		assert rootConfDir != null;

		this.rootConfDir = new File(rootConfDir);
		this.localConfDir = new File(this.rootConfDir, this.context.getHostName());
		this.localConfDir.mkdirs();

		// Load common/local conf files
		final File base = new File(this.rootConfDir, "nor.conf");
		try {

			final Properties baseProp = new Properties();
			baseProp.load(new FileInputStream(base));
			System.getProperties().putAll(baseProp);

			final File local = new File(this.localConfDir, "nor.conf");
			if(!local.exists()){

				final InputStream in = this.getClass().getResourceAsStream("nor.conf");
				final OutputStream out = new FileOutputStream(local);

				Stream.copy(in, out);

				out.close();
				in.close();

			}

			final Properties localProp = new Properties();
			localProp.load(new FileInputStream(local));
			System.getProperties().putAll(localProp);

		} catch (final IOException e) {

			LOGGER.catched(Level.SEVERE, "Cannot load config file", e);

			final RuntimeException ne = new RuntimeException("Cannot start nor.");
			LOGGER.throwing("<init>", ne);
			throw ne;

		}

		// Create request handler
		this.handler = new ProxyRequestHandler(Nor.class.getSimpleName(), this.router);

	}

	//============================================================================
	//  Public methods
	//============================================================================
	public boolean enable(final Plugin plugin){
		LOGGER.entering("enable", plugin);
		assert plugin != null;

		final boolean ret = Boolean.parseBoolean(System.getProperty(String.format(Template.PLUGIN_ENABEL,
				plugin.getClass().getName()), "true"));

		LOGGER.exiting("enable", ret);
		return ret;
	}

	//============================================================================
	//  Private methods
	//============================================================================
	//----------------------------------------------------------------------------
	//  準備
	//----------------------------------------------------------------------------
	private void init(final List<URL> jarUrls) throws IOException{
		LOGGER.entering("init");

		/*
		 * Create a proxy server.
		 */
		this.server = new ProxyServer(this.handler, this.router);

		final String pluginPath = System.getProperty("nor.plugin");
		if(pluginPath != null){

			final File dir = new File(pluginPath);
			if(dir.isDirectory()){

				for(final File f : dir.listFiles()){

					try {

						jarUrls.add(f.toURI().toURL());

					} catch (final MalformedURLException e) {

						LOGGER.catched(Level.WARNING, "init", e);

					}

				}

			}

		}

		/*
		 * Load installed plugins
		 */
		for(final Plugin p : ServiceLoader.load(Plugin.class, new URLClassLoader(jarUrls.toArray(new URL[0])))){

			final String name = p.getClass().getName();
			if(this.enable(p)){

				final File common = new File(this.rootConfDir, String.format(ConfigFileTemplate, name));
				final File local = new File(this.localConfDir, String.format(ConfigFileTemplate, name));
				p.init(common, local);

				this.server.attach(p);
				this.plugins.add(p);

				LOGGER.info("init", "Loading a plugin {0}", p.getClass().getName());

			}

		}

		/*
		 * Load a routing table.
		 */
		final File route = new File(this.localConfDir, "route.conf");
		if(!route.exists()){

			final InputStream in = this.getClass().getResourceAsStream("route.conf");
			final BufferedReader r = new BufferedReader(new InputStreamReader(in));
			final PrintWriter w = new PrintWriter(new FileWriter(route));

			String buf;
			while((buf = r.readLine()) != null){

				w.println(buf);

			}
			w.close();
			r.close();

		}

		final Properties routings = new Properties();
		final Reader rin = new FileReader(route);
		routings.load(rin);
		rin.close();
		for(final Object key : routings.keySet()){

			final String skey = (String)key;
			this.router.put(skey, new URL(routings.getProperty(skey)));

		}

		LOGGER.exiting("init");
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

		final String addr = System.getProperty(Key.LISTEN_ADDRESS);
		final int port = Integer.valueOf(System.getProperty(Key.LISTEN_PORT));

		/*
		 * Register the PAC file.
		 */
		this.server.localResourceRoot().add(new TextResource("/nor/core/proxy.pac",
				this.server.getPAC(addr, port, true), "application/x-javascript-config"));

		/*
		 * Start the web server.
		 */
		this.server.start(addr, port);

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
		this.server.close();

		// プラグインの終了処理
		for(final Plugin p : this.plugins){

			p.close();

		}

		// 設定の保存
		// this.config.store();

		LOGGER.exiting("close");
	}

	//============================================================================
	//  Class members
	//============================================================================
	private static Nor nor;

	//============================================================================
	//  Class methods
	//============================================================================
	public static HttpResponse request(final HttpRequest request){

		try {

			return nor.handler.doRequest(request);

		} catch (HttpException e) {

			return e.createResponse(request);

		}

	}

	//============================================================================
	//  main
	//============================================================================
	/**
	 * アプリケーションのエントリポイント．
	 * @param args 強制的に読み込むプラグインの完全修飾名リスト
	 * @throws MalformedURLException
	 */
	@SuppressWarnings("static-access")
	public static void main(final String[] args){
		LOGGER.info("main", "Start up...");

		final Options ops = new Options();
		ops.addOption(OptionBuilder.withArgName("dir").hasArg()
				.withDescription("set directory which has config files").create("config"));

		ops.addOption(OptionBuilder.withArgName("file").hasArg()
				.withDescription("use given configu file for logging system").create("log"));

		final Option pluginsPath = OptionBuilder.withArgName("dir").hasArg()
		.withDescription("use given directory for a serch path of plugins").create("plugin_dir");
		ops.addOption(pluginsPath);

		final Option plugins = OptionBuilder.withArgName("file").hasArg()
		.withDescription("use given plugin file").create("plugin");
		ops.addOption(plugins);

		ops.addOption("help", false, "show this help");

		try {

			final Parser parser = new BasicParser();
			final CommandLine cmd = parser.parse(ops, args);

			if(cmd.hasOption("help")){

				final HelpFormatter help = new HelpFormatter();
				help.printHelp("nor", ops, true);
				System.exit(0);

			}

			// Configure about logging system.
			InputStream logStream;
			if(cmd.hasOption("log")){

				logStream = new FileInputStream(cmd.getOptionValue("log"));

			}else{

				final String file = System.getProperty("nor.log", LoggindConfigFile);
				logStream = Nor.class.getResourceAsStream(file);

			}
			Logger.loadConfig(logStream);
			logStream.close();

			// Create the application instance by given config directory
			if(cmd.hasOption("config")){

				Nor.nor = new Nor(cmd.getOptionValue("config"));

			}else{

				Nor.nor = new Nor(System.getProperty("nor.config", "config"));

			}

			// Load plugins
			final List<URL> pluginJar = new ArrayList<URL>();
			if(cmd.hasOption("plugin")){

				for(final String filename : cmd.getOptionValues("plugin")){

					final File f = new File(filename);
					pluginJar.add(f.toURI().toURL());

				}

			}
			if(cmd.hasOption("plugin_dir")){

				for(final String dirname : cmd.getOptionValues("plugin_dir")){

					final File dir = new File(dirname);
					if(dir.isDirectory()){

						for(final String filename : dir.list()){

							final File f = new File(filename);
							pluginJar.add(f.toURI().toURL());

						}

					}

				}

			}
			nor.init(pluginJar);


			nor.start();

			// Waiting for end
			System.in.read();

			// Closing
			nor.close();

		} catch (final UnrecognizedOptionException e){

			final HelpFormatter help = new HelpFormatter();
			help.printHelp("nor", ops, true);

		} catch (final Exception e) {

			LOGGER.catched(Level.SEVERE, "main", e);

		}

		LOGGER.info("main", "End.");

	}

}
