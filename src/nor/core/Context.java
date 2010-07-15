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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.log.Logger;


class Context{

	/**
	 * 現在のゲートウェイのホスト名
	 */
	private final String host;

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Context.class);

	private static final String GatewayRegexWindows = "\\s*(?:\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(?:\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(?:\\d+\\.\\d+\\.\\d+\\.\\d+)";
	private static final String GatewayRegexLinux = "\\s*(?:\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s*(?:\\d+\\.\\d+\\.\\d+\\.\\d+)";

	private static final String Windows = "Windows";
	private static final String Linux = "Linux";

	//============================================================================
	//  コンストラクタ
	//============================================================================
	public Context(){
		LOGGER.entering("<init>");

		String host = null;
		try{

			final Process netstat = new ProcessBuilder("netstat", "-rn").start();
			final BufferedReader in = new BufferedReader(new InputStreamReader(netstat.getInputStream()));

			Pattern ipPat = null;
			final String os = System.getProperty("os.name");
			if(os.startsWith(Windows)){

				ipPat = Pattern.compile(GatewayRegexWindows);

			}else if(os.startsWith(Linux)){

				ipPat = Pattern.compile(GatewayRegexLinux);

			}

			if(ipPat != null){

				for(String buf; (buf = in.readLine()) != null;){

					final Matcher m = ipPat.matcher(buf);
					if(m.find()){

						final String ip = m.group(1);
						if(!"0.0.0.0".equals(ip)){

							final InetAddress addr = InetAddress.getByName(ip);
							host = addr.getCanonicalHostName();
							break;

						}

					}

				}

			}

			in.close();
			netstat.destroy();

		}catch(final IOException e){

			LOGGER.warning("<init>", e.getMessage());
			LOGGER.catched(Level.FINE, "<init>", e);

		}finally{

			this.host = host;
			LOGGER.info("<init>", "Gateway Host: {0}", this.host);

		}

		LOGGER.exiting("<init>");
	}

	//============================================================================
	//  public メソッド
	//============================================================================
	public String getHostName(){
		LOGGER.entering("getHostName");

		final String ret = this.host;

		LOGGER.exiting("getHostName", ret);
		return ret;
	}

}


