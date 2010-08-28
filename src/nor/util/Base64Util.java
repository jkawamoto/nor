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
package nor.util;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

/**
 *
 * @author Junpei Kawamoto
 *
 */
public class Base64Util {

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(Base64Util.class.getName());

	/**
	 *
	 */
	private static final String ENCODING = "base64";

	/**
	 *
	 */
	private static final String CHARSET = "utf-8";

	/**
	 *
	 * @param value
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String decode(final String value) throws MessagingException, IOException {
		LOGGER.entering(Base64Util.class.getName(), "decode", value);
		assert value != null;

		String tmp = value;
		while((tmp.length() % 4) != 0){

			tmp += "=";

		}

		final String ret = new String(decode(tmp.getBytes()), CHARSET);

		LOGGER.exiting(Base64Util.class.getName(), "decode", ret);
		return ret;
	}

	/**
	 *
	 * @param value
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static byte[] decode(final byte[] value) throws MessagingException, IOException {
		LOGGER.entering(Base64Util.class.getName(), "decode", value);
		assert value != null;

		final InputStream in = MimeUtility.decode(new ByteArrayInputStream(value), ENCODING);

		final byte[] buf = new byte[1024];
		final ByteArrayOutputStream out = new ByteArrayOutputStream();

		int len;
		while((len = in.read(buf)) != -1) {

			out.write(buf, 0, len);

		}

		final byte[] ret = out.toByteArray();

		LOGGER.exiting(Base64Util.class.getName(), "decode", ret);
		return ret;
	}

	/**
	 *
	 * @param value
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String encode(final String value)throws MessagingException, IOException {
		LOGGER.entering(Base64Util.class.getName(), "encode", value);
		assert value != null;

		final String ret = encode(value.getBytes());

		LOGGER.exiting(Base64Util.class.getName(), "encode", ret);
		return ret;
	}

	/**
	 *
	 * @param value
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	public static String encode(final byte[] value)throws MessagingException, IOException {
		LOGGER.entering(Base64Util.class.getName(), "encode", value);
		assert value != null;

		final ByteArrayOutputStream bao = new ByteArrayOutputStream();
		final OutputStream out = MimeUtility.encode(bao, ENCODING);

		out.write(value);
		out.close();

		final String ret = bao.toString(CHARSET);

		LOGGER.exiting(Base64Util.class.getName(), "encode", value);
		return ret;
	}

	public static String[] decodeRawText(final String text){

		Pattern pat = Pattern.compile("[A-Za-z0-9+/=]+");
		Matcher m = pat.matcher(text);

		final List<String> list = new ArrayList<String>();
		while(m.find()){

			final String target = m.group(0);
			try {

				list.add(Base64Util.decode(target));

			} catch (MessagingException e){

				// デコードエラーの場合は元のデータを用いる
				list.add(target);

			} catch (IOException e){

				// デコードエラーの場合は元のデータを用いる
				list.add(target);

			}

		}

		final String[] ret = new String[list.size()];
		list.toArray(ret);

		return ret;

	}

}
