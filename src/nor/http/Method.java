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

/**
 * HTTP/1.1 で定義されるメソッド．
 * 各メソッドの詳細は，
 * <a href="http://www.ietf.org/rfc/rfc2068.txt"/>RFC 2068: Hypertext Transfer Protocol -- HTTP/1.1</a>
 * を参照してください．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public enum Method {

	/**
	 * GET メソッド．
	 */
	GET,

	/**
	 * HEAD メソッド．
	 */
	HEAD,

	/**
	 * POST メソッド．
	 */
	POST,

	/**
	 * PUT メソッド．
	 */
	PUT,

	/**
	 * DELETE メソッド．
	 */
	DELETE,

	/**
	 * OPTIONS メソッド．
	 */
	OPTIONS,

	/**
	 * TRACE メソッド．
	 */
	TRACE,

	/**
	 * CONNECT メソッド．
	 */
	CONNECT,

}


