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
package nor.http;

import java.io.IOException;
import java.util.Properties;
import java.util.regex.Pattern;

public class Http {

	/**
	 * プロトコルバージョン．
	 */
	public static final String Version;

	/**
	 * サーバ名．
	 */
	public static final String ServerName;

	static final String CHUNKED = "chunked";
	static final String GZIP = "gzip";
	static final String DEFLATE = "deflate";

	static final String RequestLineTemplate;
	static final Pattern RequestLinePattern;

	static final String ResponseLineTemplate;
	static final Pattern ResponseLinePattern;

	private Http(){

	}

	static{

		// Load property file.
		final Properties prop = new Properties();
		try {

			prop.load(Http.class.getResourceAsStream("res/constant.conf"));

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(1);

		}

		// Set constants.
		final String classname = Http.class.getName();
		Version = prop.getProperty(String.format("%s.Version", classname));
		ServerName = prop.getProperty(String.format("%s.ServerName", classname));


		RequestLineTemplate = prop.getProperty(String.format("%s.RequestLineTemplate", classname));
		RequestLinePattern = Pattern.compile(prop.getProperty(String.format("%s.RequestLinePattern", classname)));
		ResponseLineTemplate = prop.getProperty(String.format("%s.ResponseLineTemplate", classname));
		ResponseLinePattern = Pattern.compile(prop.getProperty(String.format("%s.ResponseLinePattern", classname)));

	}

}
