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
package nor.util;

import java.util.logging.Logger;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.*;

public final class Codec {
	// ロガー
	private static final Logger LOGGER = Logger.getLogger(Codec.class.getName());
	
	/**
	 * エンコードタイプ
	 */
	public static final String DEFAULT_ENCODE = "utf-8";

	
	private static final URLCodec URLCodec = new URLCodec();
	private static final BCodec BCodec = new BCodec();

	public static String urlEncode(final String str){
		
		String ret = str;
		try {
			
			ret = URLCodec.encode(str);

		} catch (EncoderException e) {
			
			LOGGER.severe(e.getLocalizedMessage());
			
		}
		
		// TODO: サイトによってエンコードが異なる．クエリ等にそれが書かれている．どうやってはんだん？
		
		return ret;
		
	}
	
	public static String urlDecode(final String str){
		
		String ret = str;
		try {

			ret = URLCodec.decode(str);

		} catch (DecoderException e) {
			
			LOGGER.severe(e.getLocalizedMessage());
			
		}
		
		return ret;
		
	}
	
	public static String base64Encode(final String str){

		return Codec.base64Encode(str, Codec.DEFAULT_ENCODE);
		
		
	}
	
	public static String base64Encode(final String str, final String enc){
		
		String ret = str;
		try {
			
			ret = BCodec.encode(str, enc);

		} catch (EncoderException e) {
			
			LOGGER.severe(e.getLocalizedMessage());
			
		}
	
		return ret;
		
		
	}

	public static String base64Decode(final String str){
		
		String ret = str;
		try {

			ret = BCodec.decode(str);

		} catch (DecoderException e) {
			
			LOGGER.severe(e.getLocalizedMessage());
			
		}
		
		return ret;
		
	}
	
	
}


