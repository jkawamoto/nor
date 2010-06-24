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
 * HTTP/1.1 で定義されるステータス．
 * 各ステータスの詳細は，
 * <a href="http://www.ietf.org/rfc/rfc2068.txt"/>RFC 2068: Hypertext Transfer Protocol -- HTTP/1.1</a>
 * を参照してください．
 *
 * @author Junpei Kawamoto
 */
public enum Status{

	/////////////////////////////////////////////////////////////////
	// 100 番台
	/////////////////////////////////////////////////////////////////
	/**
	 * 100 Continue.
	 */
	Continue(100, "Continue"),

	/**
	 * 101 Switching Protocols.
	 */
	SwitchingProtocols(101, "Switching Protocols"),

	/**
	 * 102 Processing.
	 */
	Processing(102, "Processing"),

	/////////////////////////////////////////////////////////////////
	// 200 番台
	/////////////////////////////////////////////////////////////////
	/**
	 * 200 OK
	 */
	OK(200, "OK"),

	/**
	 * 201 Created.
	 */
	Created(201, "Created"),

	/**
	 * 202 Accepted.
	 */
	Accepted(202, "Accepted"),

	/**
	 * 203 Non-Authoritative Information.
	 */
	NonAuthoritativeInformation(203, "Non-Authoritative Information"),

	/**
	 * 204 No Content.
	 */
	NoContent(204, "No Content"),

	/**
	 * 205 Reset Content.
	 */
	ResetContent(205, "Reset Content"),

	/**
	 * 206 Partial Content.
	 */
	PartialContent(206, "Partial Content"),

	/**
	 * 207 Multi-Status.
	 */
	MultiStatus(207, "Multi-Status"),

	/**
	 * 200 Connection established.
	 */
	ConnectionEstablished(200, "Connection established"),


	/////////////////////////////////////////////////////////////////
	// 300 番台
	/////////////////////////////////////////////////////////////////
	/**
	 * 300 Multiple Choices.
	 */
	MultipleChoices(300, "Multiple Choices"),

	/**
	 * 301 Moved Permanently.
	 */
	MovedPermanently(301, "Moved Permanently"),

	/**
	 * 302 Found.
	 */
	Found(302, "Found"),

	/**
	 * 303 See Other.
	 */
	SeeOther(303, "See Other"),

	/**
	 * 304 Not Modified.
	 */
	NotModified(304, "Not Modified"),

	/**
	 * 305 Use Proxy.
	 */
	UseProxy(305, "Use Proxy"),

	/**
	 * 307 Temporary Redirect.
	 */
	TemporaryRedirect(307, "Temporary Redirect"),


	/////////////////////////////////////////////////////////////////
	// 400 番台
	/////////////////////////////////////////////////////////////////
	/**
	 * 400 Bad Request（要求が不正です）．
	 */
	BadRequest(400, "Bad Request"),

	/**
	 * 401 Unauthorized（認証されていません）．
	 */
	Unauthorized(401, "Unauthorized"),

	/**
	 * 402 Payment Required（支払いが必要です）．
	 */
	PaymentRequired(402, "Payment Required"),

	/**
	 * 403 Forbidden（アクセスが認められていません）．
	 */
	Forbidden(403, "Forbidden"),

	/**
	 * 404 Not Found（見つかりません）．
	 */
	NotFound(404, "Not Found"),

	/**
	 * 405 Method Not Allowed（指定したメソッドはサポートされていません）．
	 */
	MethodNotAllowed(405, "Method Not Allowed"),

	/**
	 * 406 Not Acceptable（許可されていません）．
	 */
	NotAcceptable(406, "Not Acceptable"),

	/**
	 * 407 Proxy Authentication Required（プロキシを利用するには認証が必要です）．
	 */
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),

	/**
	 * 408 Request Timeout（リクエストがタイムアウトしました）．
	 */
	RequestTimeout(408, "Request Timeout"),

	/**
	 * 409 Conflict（リクエストが衝突・矛盾しました）．
	 */
	Conflict(409, "Conflict"),

	/**
	 * 410 Gone（要求されたコンテンツは無くなっています）．
	 */
	Gone(410, "Gone"),

	/**
	 * 411 Length Required（要求には Content-Length ヘッダが必要です）．
	 */
	LengthRequired(411, "Length Required"),

	/**
	 * 412 Precondition Failed（If-... ヘッダで指定された条件に合致しませんでした）．
	 */
	PreconditionFailed(412, "Precondition Failed"),

	/**
	 * 413 Request Entity Too Large（要求のエンティティが大きすぎます）．
	 */
	RequestEntityTooLarge(413, "Request Entity Too Large"),

	/**
	 * 414 Request-URI Too Long（リクエスト URI が長すぎます）．
	 */
	RequestURITooLong(414, "Request-URI Too Long"),

	/**
	 * 415 Unsupported Media Type（サポートされていないメディアタイプです）．
	 */
	UnsupportedMediaType(415, "Unsupported Media Type"),

	/**
	 * 416 Requested Range Not Satisfiable（要求されたレンジが不正です）．
	 */
	RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),

	/**
	 * 417 Expectation Failed（Expect ヘッダで指定された拡張要求は失敗しました）．
	 */
	ExpectationFailed(417, "Expectation Failed"),


	/////////////////////////////////////////////////////////////////
	// 500 番台
	/////////////////////////////////////////////////////////////////
	/**
	 * 500 Internal Server Error（サーバーで予期しないエラーが発生しました）．
	 */
	InternalServerError(500, "Internal Server Error"),

	/**
	 * 501 Not Implemented（実装されていません）．
	 */
	NotImplemented(501, "Not Implemented"),

	/**
	 * 502 Bad Gateway（ゲートウェイが不正です）．
	 */
	BadGateway(502, "Bad Gateway"),

	/**
	 * 503 Service Unavailable（サービスは利用可能ではありません）．
	 */
	ServiceUnavailable(503, "Service Unavailable"),

	/**
	 * 504 Gateway Timeout（ゲートウェイがタイムアウトしました）．
	 */
	GatewayTimeout(504, "Gateway Timeout"),

	/**
	 * 505 HTTP Version Not Supported（このHTTPバージョンはサポートされていません）．
	 */
	HTTPVersionNotSupported(505, "HTTP Version Not Supported");

	private final int code;
	private final String msg;

	private Status(final int code, final String msg){

		this.code = code;
		this.msg = msg;

	}

	/**
	 * ステータスコードを取得する．
	 *
	 * @return この定数のステータスコード
	 */
	public int getCode(){

		return this.code;

	}

	/**
	 * メッセージを取得する．
	 *
	 * @return この定数のメッセージ
	 */
	public String getMessage(){

		return this.msg;

	}

	/**
	 * 指定したコード番号を持つこの型の列挙型定数を返します．
	 *
	 * @param code レスポンスコード
	 * @return 指定したコード番号を持つこの型の列挙型定数
	 * @throws IllegalArgumentException 指定されたコード番号を持つ定数をこの列挙型が持っていない場合
	 */
	public static Status valueOf(final int code) throws IllegalArgumentException{

		for(final Status s : Status.values()){

			if(s.code == code){

				return s;

			}

		}

		throw new IllegalArgumentException(String.format("Status code %d is not defined in RFC 2068.", code));

	}

	/* (非 Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString(){

		return String.format("%d %s", this.code, this.msg);

	}

}
