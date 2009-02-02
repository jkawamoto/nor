/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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
 * HTTPヘッダフィールド名．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class HeaderName {

	public static final String ContentLength = "content-length";
	public static final String ContentType = "content-type";
	public static final String ContentEncoding = "content-encoding";
	public static final String CacheControl = "cache-control";
	public static final String Connection = "connection";
	public static final String Date = "date";
	public static final String Pragma = "pragma";
	public static final String Trailer = "trailer";
	public static final String TransferEncoding = "transfer-encoding";
	public static final String Upgrade = "upgrade";
	public static final String Via  = "via";
	public static final String Warning = "warning";
	public static final String Accept = "accept";
	public static final String AcceptCharset = "accept-charset";
	public static final String AcceptEncoding = "accept-encoding";
	public static final String AcceptLanguage = "accept-language";
	public static final String Authorization = "authorization";
	public static final String Expect = "expect";
	public static final String From = "from";
	public static final String Host = "host";
	public static final String IfMatch = "if-match";
	public static final String IfModifiedSince = "if-modified-since";
	public static final String IfNoneMatch = "if-none-match";
	public static final String IfRange = "if-range";
	public static final String IfUnmodifiedSince = "if-unmodified-since";
	public static final String MaxForwards = "max-forwards";
	public static final String ProxyAuthorization = "proxy-authorization";
	public static final String Range = "range";
	public static final String Referer = "referer";
	public static final String TE = "te";
	public static final String UserAgent = "user-agent";
	public static final String AcceptRanges = "accept-ranges";
	public static final String Age = "age";
	public static final String ETag = "etag";
	public static final String Location = "location";
	public static final String ProxyAuthenticate = "proxy-authenticate";
	public static final String RetryAfter = "retry-after";
	public static final String Server = "server";
	public static final String Vary = "vary";
	public static final String WWWAuthenticate = "www-authenticate";
	public static final String Allow = "allow";
	public static final String ContentLanguage = "content-language";
	public static final String ContentLocation = "content-location";
	public static final String ContentMD5 = "content-md5";
	public static final String ContentRange = "content-range";
	public static final String Expires = "expires";
	public static final String LastModified = "last-modified";

	public static final String KeepAlive = "keep-alive";
	public static final String Cookie = "cookie";
	public static final String SetCookie = "set-cookie";

	public static final String ProxyConnection = "proxy-connection";

	public static final String BodyType = "x-nor-bodytype";

}
