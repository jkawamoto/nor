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
package nor.http.server.rest;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import nor.http.ContentType;
import nor.http.HttpError;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

public class TextResource extends Resource{

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(TextResource.class.getName());

	private static final ContentType DEFAULT_CONTENT_TYPE = new ContentType("text/plain");

	private final String _name;

	private String _text;

	private final ContentType _type;

	private Date _modified;

	/**
	 * 日付フォーマット
	 *
	 */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱う文字列
	 * @param type リソースのコンテンツタイプ
	 */
	public TextResource(final String name, final String text, final ContentType type){
		LOGGER.entering(TextResource.class.getName(), "<init>", new Object[]{name, text, type});
		assert name != null;
		assert text != null;
		assert type != null;

		this._name = name;
		this._text = text;
		this._type = type;

		this._modified = Calendar.getInstance().getTime();

		LOGGER.exiting(TextResource.class.getName(), "<init>");
	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱う文字列
	 * @param type リソースのコンテンツタイプ
	 */
	public TextResource(final String name, final String text, final String type){

		this(name, text, new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱う文字列
	 * @param type リソースのコンテンツタイプ
	 */
	public TextResource(final String name, final String text){

		this(name, text, DEFAULT_CONTENT_TYPE);

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/**
	 * リソース文字列を取得する．
	 *
	 * @return このリソースに設定されている文字列
	 */
	public String getText(){
		LOGGER.entering(TextResource.class.getName(), "getText");

		final String ret = this._text;

		LOGGER.exiting(TextResource.class.getName(), "getText", ret);
		return ret;

	}

	/**
	 * リソース文字列を設定する．
	 *
	 * @param text このリソースに設定する文字列
	 */
	public void setText(final String text){
		LOGGER.entering(TextResource.class.getName(), "setText", text);
		assert text != null;

		this._text = text;
		this._modified = Calendar.getInstance().getTime();

		LOGGER.exiting(TextResource.class.getName(), "setText");
	}


	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.ResourceAdapter#toGet(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toGet(final String path, final HttpRequest request){
		LOGGER.entering(FileResource.class.getName(), "toGet", new Object[]{path, request});
		assert path != null;
		assert request != null;

		HttpResponse ret = null;

		final StringBuilder header = new StringBuilder();
		header.append("HTTP/1.1 200 OK\n");
		header.append("Content-Type: ");
		header.append(this._type);
		header.append("\n");
		header.append("Last-Modified: ");
		header.append(DATE_FORMAT.format(this._modified));
		header.append("\n");
		header.append("Server: arthra\n");
		header.append("Content-Length: ");
		header.append(this._text.length());
		header.append("\n");
		header.append("Date: ");
		header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
		header.append("\n\n");

		try {

			final InputStream headerIn = new ByteArrayInputStream(header.toString().getBytes());
			final InputStream bodyIn = new ByteArrayInputStream(this._text.getBytes());
			final InputStream in = new SequenceInputStream(headerIn, bodyIn);

			ret = request.createResponse(in);
			in.close();
			bodyIn.close();
			headerIn.close();

		} catch (FileNotFoundException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (IOException e) {

			LOGGER.warning(e.getLocalizedMessage());

		} catch (HttpError e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}


		if(ret == null){

			final String error = "HTTP/1.1 404 Not Found\n\n";
			try {

				final InputStream in = new ByteArrayInputStream(error.getBytes());

				ret = request.createResponse(in);
				in.close();

			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (HttpError e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

		}

		LOGGER.exiting(FileResource.class.getName(), "toGet", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#getName()
	 */
	@Override
	public String getName(){
		LOGGER.entering(FileResource.class.getName(), "getName");

		final String ret = this._name;

		LOGGER.exiting(FileResource.class.getName(), "getName", ret);
		return ret;
	}

	@Override
	public String toString(){

		return this._text + "(" + this._modified + ")";


	}

}


