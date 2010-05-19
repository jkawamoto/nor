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
package nor.http;


/**
 * HTTP標準のステータス．
 *
 * @author KAWAMOTO Junpei
 *
 */
public enum Status{

	Continue(100, "Continue"),
	SwitchingProtocols(101, "Switching Protocols"),
	Processing(102, "Processing"),

	/**
	 * 200 - OK
	 */
	OK(200, "OK"),
	Created(201, "Created"),
	Accepted(202, "Accepted"),
	NonAuthoritativeInformation(203, "Non-Authoritative Information"),
	NoContent(204, "No Content"),
	ResetContent(205, "Reset Content"),
	PartialContent(206, "Partial Content"),
	MultiStatus(207, "Multi-Status"),
	IMUsed(226, "IM Used"),

	MultipleChoices(300, "Multiple Choices"),
	MovedPermanently(301, "Moved Permanently"),
	Found(302, "Found"),
	/**
	 * 303 - See Other
	 */
	SeeOther(303, "See Other"),
	NotModified(304, "Not Modified"),
	UseProxy(305, "Use Proxy"),
	TemporaryRedirect(307, "Temporary Redirect"),

	/**
	 * 400 - Bad Request - 要求が不正です。
	 */
	BadRequest(400, "Bad Request"),

	/**
	 * 401 - Unauthorized - 認証されていません。
	 */
	Unauthorized(401, "Unauthorized"),

	/**
	 * 402 - Payment Required - 支払いが必要です。
	 */
	PaymentRequired(402, "Payment Required"),

	/**
	 * 403 - Forbidden - アクセスが認められていません。
	 */
	Forbidden(403, "Forbidden"),

	/**
	 * 404 - Not Found - 見つかりません。
	 */
	NotFound(404, "Not Found"),

	/**
	 * 405 - Method Not Allowed - 指定したメソッドはサポートされていません。
	 */
	MethodNotAllowed(405, "Method Not Allowed"),

	/**
	 * 406 - Not Acceptable - 許可されていません。
	 */
	NotAcceptable(406, "Not Acceptable"),

	/**
	 * 407 - Proxy Authentication Required - プロキシ認証が必要です。
	 */
	ProxyAuthenticationRequired(407, "Proxy Authentication Required"),

	/**
	 * 408 - Request Timeout - リクエストがタイムアウトしました。
	 */
	RequestTimeout(408, "Request Timeout"),

	/**
	 * 409 - Conflict - リクエストがコンフリクト（衝突・矛盾）しました。
	 */
	Conflict(409, "Conflict"),

	/**
	 * 410 - Gone - 要求されたコンテンツは無くなってしまいました。
	 */
	Gone(410, "Gone"),

	/**
	 * 411 - Length Required - Content-Length ヘッダを付加して要求してください。
	 */
	LengthRequired(411, "Length Required"),

	/**
	 * 412 - Precondition Failed - If-...ヘッダで指定された条件に合致しませんでした。
	 */
	PreconditionFailed(412, "Precondition Failed"),

	/**
	 * 413 - Request Entity Too Large - 要求されたエンティティが大きすぎます。
	 */
	RequestEntityTooLarge(413, "Request Entity Too Large"),

	/**
	 * 414 - Request-URI Too Long - 要求された URI が長すぎます。
	 */
	RequestURITooLong(414, "Request-URI Too Long"),

	/**
	 * 415 - Unsupported Media Type - サポートされていないメディアタイプです。
	 */
	UnsupportedMediaType(415, "Unsupported Media Type"),

	/**
	 * 416 - Requested Range Not Satisfiable - 要求されたレンジが不正です。
	 */
	RequestedRangeNotSatisfiable(416, "Requested Range Not Satisfiable"),

	/**
	 * 417 - Expectation Failed - Expectヘッダで指定された拡張要求は失敗しました。
	 */
	ExpectationFailed(417, "Expectation Failed"),

	/**
	 * 500 - Internal Server Error - サーバーで予期しないエラーが発生しました。
	 */
	InternalServerError(500, "Internal Server Error"),

	/**
	 * 501 - Not Implemented - 実装されていません。
	 */
	NotImplemented(501, "Not Implemented"),

	/**
	 * 502 - Bad Gateway - ゲートウェイが不正です。
	 */
	BadGateway(502, "Bad Gateway"),

	/**
	 * 503 - Service Unavailable - サービスは利用可能ではありません。
	 */
	ServiceUnavailable(503, "Service Unavailable"),

	/**
	 * 504 - Gateway Timeout - ゲートウェイがタイムアウトしました。
	 */
	GatewayTimeout(504, "Gateway Timeout"),

	/**
	 * 505 - HTTP Version Not Supported - このHTTPバージョンはサポートされていません。
	 */
	HTTPVersionNotSupported(505, "HTTP Version Not Supported");

	private final int code;
	private final String msg;

	private Status(final int code, final String msg){

		this.code = code;
		this.msg = msg;

	}

	public int getCode(){

		return this.code;

	}

	public String getMessage(){

		return this.msg;

	}


	public String toString(){

		return String.format("%d %s", this.code, this.msg);

	}

	public static Status valueOf(final int code){

		for(final Status s : Status.values()){

			if(s.code == code){

				return s;

			}

		}

		return null;

	}

}
