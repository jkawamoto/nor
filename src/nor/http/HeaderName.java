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
 * HTTP/1.1 で定義されるヘッダ．
 * 各ヘッダの詳細は，
 * <a href="http://www.ietf.org/rfc/rfc2068.txt"/>RFC 2068: Hypertext Transfer Protocol -- HTTP/1.1</a>
 * を参照してください．
 *
 * @author Junpei Kawamoto
 */
public enum HeaderName {

	ContentLength("content-length"),

	ContentType("content-type"),

	ContentEncoding("content-encoding"),

	CacheControl("cache-control"),

	Connection("connection"),

	Date("date"),

	Pragma("pragma"),

	Trailer("trailer"),

	TransferEncoding("transfer-encoding"),

	Upgrade("upgrade"),

	Via ("via"),

	Warning("warning"),

	Accept("accept"),

	AcceptCharset("accept-charset"),

	AcceptEncoding("accept-encoding"),

	AcceptLanguage("accept-language"),

	Authorization("authorization"),

	Expect("expect"),

	From("from"),

	Host("host"),

	IfMatch("if-match"),

	IfModifiedSince("if-modified-since"),

	IfNoneMatch("if-none-match"),

	IfRange("if-range"),

	IfUnmodifiedSince("if-unmodified-since"),

	MaxForwards("max-forwards"),

	ProxyAuthorization("proxy-authorization"),

	Range("range"),

	Referer("referer"),

	TE("te"),

	UserAgent("user-agent"),

	AcceptRanges("accept-ranges"),

	Age("age"),

	ETag("etag"),

	Location("location"),

	ProxyAuthenticate("proxy-authenticate"),

	RetryAfter("retry-after"),

	Server("server"),

	Vary("vary"),

	WWWAuthenticate("www-authenticate"),

	Allow("allow"),

	ContentLanguage("content-language"),

	ContentLocation("content-location"),

	ContentMD5("content-md5"),

	ContentRange("content-range"),

	Expires("expires"),

	LastModified("last-modified"),

	KeepAlive("keep-alive"),

	Cookie("cookie"),

	SetCookie("set-cookie"),

	ProxyConnection("proxy-connection");


	///////////////////////////////////////////////////////////////////////////////////////////////
	private final String name;

	private HeaderName(final String name){

		this.name = name;

	}

	/**
	 * 文字列として一致するか．
	 *
	 * @param anotherString 比較する文字列
	 * @return 与えられた文字列がヘッダ名と一致する場合 true
	 */
	public boolean equals(final String anotherString){

		return this.name.equalsIgnoreCase(anotherString);

	}

	/* (非 Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString(){

		return this.name;

	}


}
