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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import nor.core.plugin.Plugin;
import nor.util.log.EasyLogger;

/**
 * @author KAWAMOTO Junpei
 *
 */
public class Config {

	/**
	 * プロパティオブジェクト
	 *
	 */
	private final Properties prop;

	/**
	 * 設定保存ファイル
	 */
	private final File configFile;

	/**
	 *
	 * @author KAWAMOTO Junpei
	 *
	 */
	private static final class Key{

		public static final String LISTEN_ADDRESS = "nor.address";
		public static final String LISTEN_PORT = "nor.port";

	}

	private static final class Template{

		public static final String PLUGIN_ENABEL = "%s.enable";

	}

	/**
	 * ロガー
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(Config.class);

	//============================================================================
	//  コンストラクタ
	//============================================================================
	/**
	 * 設定を読み込んでオブジェクトを作成する．
	 * @param filename 設定ファイル名
	 */
	Config(final File configFile){
		LOGGER.entering(Config.class.getName(), "<init>", configFile);
		assert configFile != null;

		this.configFile = configFile;
		this.prop = new Properties();
		if(configFile.exists()){

			try {

				this.prop.load(new FileInputStream(configFile));

			} catch (final IOException e) {

				LOGGER.warning(e.getMessage());
				this.loadDefault();

			}

		}else{

			this.loadDefault();

		}

		LOGGER.exiting(Config.class.getName(), "<init>");
	}

	//============================================================================
	//  public メソッド
	//============================================================================
	/**
	 *
	 * @return
	 */
	public String getListenAddress(){
		LOGGER.entering("getListenAddress");

		final String ret = this.prop.getProperty(Key.LISTEN_ADDRESS);

		LOGGER.exiting("getListenAddress", ret);
		return ret;

	}

	/**
	 *
	 * @return
	 */
	public int getListenPort(){
		LOGGER.entering("getListenPort");

		final int ret = Integer.valueOf(this.prop.getProperty(Key.LISTEN_PORT));

		LOGGER.exiting("getListenPort", ret);
		return ret;

	}

	/**
	 * 指定のプラグインが有効か否かを返す．
	 * 設定ファイルに該当モジュールの設定が無い場合は有効に設定される．
	 * @param seedname 有効か否かを問い合わせるモジュール名
	 * @return 有効の場合trueを返す
	 */
	public boolean isEnable(final Plugin plugin){
		LOGGER.entering(Config.class.getName(), "isEnabel", plugin);
		assert plugin != null;

		final boolean ret = Boolean.parseBoolean(this.prop.getProperty(String.format(Template.PLUGIN_ENABEL, plugin.getClass().getName()), "true"));

		LOGGER.exiting(Config.class.getName(), "isEnable", ret);
		return ret;
	}








	//============================================================================
	//  package private メソッド
	//============================================================================
	/**
	 *
	 */
	void store() throws FileNotFoundException, IOException{
		LOGGER.entering("save");

		this.prop.store(new FileOutputStream(this.configFile), "");

		LOGGER.exiting("save");
	}

	/**
	 *
	 * @param seedname
	 * @param enable
	 */
	void setEnable(final Plugin plugin, boolean enable){
		LOGGER.entering("setEnable", plugin, enable);
		assert plugin != null;

		this.prop.setProperty(String.format(Template.PLUGIN_ENABEL, plugin.getClass().getName()), Boolean.toString(enable));

		LOGGER.exiting("setEnable");
	}

	/**
	 *
	 * @param address
	 */
	void setListenAddress(final String address){
		LOGGER.entering("setListenAddress", address);
		assert address != null;

		this.prop.setProperty(Key.LISTEN_ADDRESS, address);

		LOGGER.exiting("setListenAddress");
	}

	/**
	 *
	 * @param port
	 */
	void setListenPort(final int port){
		LOGGER.entering("setListenPort", port);
		assert port > 0;

		this.prop.setProperty(Key.LISTEN_PORT, Integer.toString(port));

		LOGGER.exiting("setListenPort");
	}


	//============================================================================
	//  private メソッド
	//============================================================================
	/**
	 *
	 */
	private void loadDefault(){
		LOGGER.entering("loadDefault");

		this.setListenAddress("127.0.0.1");
		this.setListenPort(8080);
		try {

			this.store();

		} catch (final FileNotFoundException e) {

			LOGGER.warning(e.getMessage());

		} catch (final IOException e) {

			LOGGER.warning(e.getMessage());

		}

		LOGGER.exiting("loadDefault");
	}

}


