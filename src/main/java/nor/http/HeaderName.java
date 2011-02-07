/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
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

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP/1.1 で定義されるヘッダ．
 * 各ヘッダの詳細は，
 * <a href="http://www.ietf.org/rfc/rfc2068.txt"/>RFC 2068: Hypertext Transfer Protocol -- HTTP/1.1</a>
 * を参照してください．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public final class HeaderName {

	//====================================================================
	// Private class member
	//====================================================================
	private static final Map<String, HeaderName> map = new HashMap<String, HeaderName>();

	//====================================================================
	// Public class members
	//====================================================================
	public static final HeaderName ContentLength = new HeaderName("content-length");

	public static final HeaderName ContentType = new HeaderName("content-type");

	public static final HeaderName ContentEncoding = new HeaderName("content-encoding");

	public static final HeaderName CacheControl = new HeaderName("cache-control");

	public static final HeaderName Connection = new HeaderName("connection");

	public static final HeaderName Date = new HeaderName("date");

	public static final HeaderName Pragma = new HeaderName("pragma");

	public static final HeaderName Trailer = new HeaderName("trailer");

	public static final HeaderName TransferEncoding = new HeaderName("transfer-encoding");

	public static final HeaderName Upgrade = new HeaderName("upgrade");

	public static final HeaderName Via = new HeaderName("via");

	public static final HeaderName Warning = new HeaderName("warning");

	public static final HeaderName Accept = new HeaderName("accept");

	public static final HeaderName AcceptCharset = new HeaderName("accept-charset");

	public static final HeaderName AcceptEncoding = new HeaderName("accept-encoding");

	public static final HeaderName AcceptLanguage = new HeaderName("accept-language");

	public static final HeaderName Authorization = new HeaderName("authorization");

	public static final HeaderName Expect = new HeaderName("expect");

	public static final HeaderName From = new HeaderName("from");

	public static final HeaderName Host = new HeaderName("host");

	public static final HeaderName IfMatch = new HeaderName("if-match");

	public static final HeaderName IfModifiedSince = new HeaderName("if-modified-since");

	public static final HeaderName IfNoneMatch = new HeaderName("if-none-match");

	public static final HeaderName IfRange = new HeaderName("if-range");

	public static final HeaderName IfUnmodifiedSince = new HeaderName("if-unmodified-since");

	public static final HeaderName MaxForwards = new HeaderName("max-forwards");

	public static final HeaderName ProxyAuthorization = new HeaderName("proxy-authorization");

	public static final HeaderName Range = new HeaderName("range");

	public static final HeaderName Referer = new HeaderName("referer");

	public static final HeaderName TE = new HeaderName("te");

	public static final HeaderName UserAgent = new HeaderName("user-agent");

	public static final HeaderName AcceptRanges = new HeaderName("accept-ranges");

	public static final HeaderName Age = new HeaderName("age");

	public static final HeaderName ETag = new HeaderName("etag");

	public static final HeaderName Location = new HeaderName("location");

	public static final HeaderName ProxyAuthenticate = new HeaderName("proxy-authenticate");

	public static final HeaderName RetryAfter = new HeaderName("retry-after");

	public static final HeaderName Server = new HeaderName("server");

	public static final HeaderName Vary = new HeaderName("vary");

	public static final HeaderName WWWAuthenticate = new HeaderName("www-authenticate");

	public static final HeaderName Allow = new HeaderName("allow");

	public static final HeaderName ContentLanguage = new HeaderName("content-language");

	public static final HeaderName ContentLocation = new HeaderName("content-location");

	public static final HeaderName ContentMD5 = new HeaderName("content-md5");

	public static final HeaderName ContentRange = new HeaderName("content-range");

	public static final HeaderName Expires = new HeaderName("expires");

	public static final HeaderName LastModified = new HeaderName("last-modified");

	public static final HeaderName KeepAlive = new HeaderName("keep-alive");

	public static final HeaderName Cookie = new HeaderName("cookie");

	public static final HeaderName SetCookie = new HeaderName("set-cookie");

	public static final HeaderName ProxyConnection = new HeaderName("proxy-connection");


	//====================================================================
	// Private members
	//====================================================================
	private final String name;

	//====================================================================
	// Constructors
	//====================================================================
	private HeaderName(final String name){

		this.name = name.toLowerCase();
		HeaderName.map.put(this.toString(), this);

	}

	//====================================================================
	// Public methods
	//====================================================================
	/**
	 * 文字列として一致するか．
	 *
	 * @param anotherString 比較する文字列
	 * @return 与えられた文字列がヘッダ名と一致する場合 true
	 */
	public boolean equals(final String anotherString){

		return this.toString().equalsIgnoreCase(anotherString);

	}

	@Override
	public String toString(){

		return this.name;

	}

	//====================================================================
	// Public class methods
	//====================================================================
	public static HeaderName valueOf(final String name){

		final HeaderName ret = HeaderName.map.get(name.toLowerCase());
		if(ret == null){

			throw new IllegalArgumentException("HeaderName does not contain " + name);

		}

		return ret;

	}

	public static HeaderName[] values(){

		return HeaderName.map.values().toArray(new HeaderName[0]);

	}

}
