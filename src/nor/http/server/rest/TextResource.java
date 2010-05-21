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

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import nor.http.ContentType;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

public class TextResource extends Resource{

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(TextResource.class.getName());

	private static final ContentType DEFAULT_CONTENT_TYPE = new ContentType("text/plain");

	private final String name;

	private String text;

	private final ContentType type;

	private Date modified;

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

		this.name = name;
		this.text = text;
		this.type = type;

		this.modified = Calendar.getInstance().getTime();

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

		final String ret = this.text;

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

		this.text = text;
		this.modified = Calendar.getInstance().getTime();

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

		final HttpResponse ret = SimpleResponseBuilder.create(request, this.text, this.type);

		LOGGER.exiting(FileResource.class.getName(), "toGet", ret);
		return ret;

	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.Resource#getName()
	 */
	@Override
	public String getName(){
		LOGGER.entering(FileResource.class.getName(), "getName");

		final String ret = this.name;

		LOGGER.exiting(FileResource.class.getName(), "getName", ret);
		return ret;
	}

	@Override
	public String toString(){

		return this.text + "(" + this.modified + ")";


	}

}


