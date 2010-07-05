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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.util.log.Logger;


class Context{

	/**
	 * 現在のゲートウェイMACアドレス
	 */
	private final String mac;

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(Context.class);

	/**
	 * MACアドレス取得用の正規表現
	 */
	private static final Pattern GATEWAY_IP = Pattern.compile("Gateway.*((\\d{1,3}\\.){3}\\d{1,3})");

	//============================================================================
	//  コンストラクタ
	//============================================================================
	public Context(){
		LOGGER.entering("<init>");

		String gatewayMAC = null;
		try{

			String gatwayIP = null;

			final Process ipconfig = new ProcessBuilder("ipconfig").start();
			final BufferedReader r1 = new BufferedReader(new InputStreamReader(ipconfig.getInputStream()));

			boolean flag = false;
			for(String buf; (buf = r1.readLine()) != null;){

				if(buf.contains("Ethernet")){

					flag = true;

				}else if(flag){

					final Matcher m = GATEWAY_IP.matcher(buf);
					if(m.find()){

						gatwayIP = m.group(1);
						break;

					}

				}

			}

			r1.close();
			ipconfig.destroy();

			final Pattern MAC = Pattern.compile(gatwayIP + ".*(([0-9a-fA-F]{2}-){5}[0-9a-fA-F]{2})");
			final Process arp = new ProcessBuilder("arp", "-a").start();
			final BufferedReader r2 = new BufferedReader(new InputStreamReader(arp.getInputStream()));
			for(String buf; (buf = r2.readLine()) != null;){

				final Matcher m = MAC.matcher(buf);
				if(m.find()){

					gatewayMAC = m.group(1);
					break;

				}

			}

			r2.close();
			arp.destroy();

		}catch(IOException e){

			LOGGER.warning(e.getLocalizedMessage());

		}finally{

			LOGGER.info("Gateway MAC-Address: " + gatewayMAC);
			this.mac = gatewayMAC;

		}

		LOGGER.exiting("<init>");
	}

	//============================================================================
	//  public メソッド
	//============================================================================
	public String getMAC(){
		LOGGER.entering("getMAC");

		final String ret = this.mac;

		LOGGER.exiting("getMAC", ret);
		return ret;
	}

}


