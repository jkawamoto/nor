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

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import nor.util.log.EasyLogger;



/**
 * HTTP標準のエラーレスポンスに対するレスポンスオブジェクトを生成する．
 * HTTP標準におけるエラーステータスは，ステータスコード400番台と500番台に割り当てられている．
 * このクラスは，これらのエラーを指定することで対応するレスポンスオブジェクトを
 * 生成するメソッドを提供する．
 *
 * エラーレスポンスに登録する，エラーコード，エラーメッセージ，メッセージボディを記述した
 * テキストファイルは外部プロパティファイルにより設定することが可能である．
 *
 * 外部プロパティファイルのファイル名は，ErrorResponseFactory.propertiesに固定されている．
 * このプロパティファイルに，
 * <pre>
 * NotFound.file = NotFound
 * </pre>
 * のように，各ステータス別にfileを設定する．
 * 指定するファイルには，レスポンスとして返すべき「すべての文字列」を記述する．
 * すなわち，レスポンスレコード及び，ヘッダ，メッセージボディが記述される．
 *
 * @author KAWAMOTO Junpei
 *
 */
public class ErrorResponseBuilder {

	/**
	 * ロガー．
	 */
	private static final EasyLogger LOGGER = EasyLogger.getLogger(ErrorResponseBuilder.class);

	private static String serverName;

	public static void setServerName(final String serverName){

		ErrorResponseBuilder.serverName = serverName;

	}


	/**
	 * エラーステータスに対応するエラーレスポンスオブジェクトを生成する．
	 * この実装では，エラーレスポンスオブジェクトは毎回生成し，再利用は行わない．
	 * その為，返されるインスタンスは変更可能である．
	 *
	 * @param request
	 * @param status レスポンスを生成するエラーステータス
	 * @return 生成されたエラーレスポンス
	 */
	public static HttpResponse create(final HttpRequest request, final Status status){
		LOGGER.entering("create", request, status);
		assert request != null;
		assert status != null;

		HttpResponse ret = null;

		try{

			ret = request.createResponse(status);

			final HttpHeader header = ret.getHeader();
			header.add(HeaderName.ContentType, "text/html; charset=utf-8");
			header.add(HeaderName.Connection, "close");
			header.add(HeaderName.Server, serverName);

		}catch (final HttpError e){

			throw new RuntimeException(e);

		}

		LOGGER.exiting("create", ret);
		assert ret != null;
		return ret;

	}

	/**
	 * エラーステータスに対応するエラーレスポンスオブジェクトを生成する．
	 * この実装では，エラーレスポンスオブジェクトは毎回生成し，再利用は行わない．
	 * その為，返されるインスタンスは変更可能である．
	 *
	 * @param request
	 * @param status レスポンスを生成するエラーステータス
	 * @param body メッセージボディ
	 * @return 生成されたエラーレスポンス
	 */
	public static HttpResponse create(final HttpRequest request, final Status status, final String body){
		LOGGER.entering("create", request, status, body);
		assert request != null;
		assert status != null;
		assert body != null;

		HttpResponse ret = null;

		try{

			ret = request.createResponse(status);

			final HttpHeader header = ret.getHeader();
			header.add(HeaderName.ContentType, "text/html; charset=utf-8");
			header.add(HeaderName.Connection, "close");
			header.add(HeaderName.Server, serverName);
			header.add(HeaderName.ContentLength, Integer.toString(body.getBytes().length));

			ret.getBody().setStream(new ByteArrayInputStream(body.getBytes()));

		}catch (final HttpError e){

			throw new RuntimeException(e);

		}

		LOGGER.exiting("create", ret);
		return ret;

	}

	public static HttpResponse create(final HttpRequest request, final Status status, final Exception e){

		final StringWriter body = new StringWriter();
		e.printStackTrace(new PrintWriter(body));

		// TODO: 出力を整形する
		return ErrorResponseBuilder.create(request, status, body.toString());

	}

}
