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
package nor.http.server.local;

import java.util.Calendar;
import java.util.Date;

import nor.http.ContentType;
import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;
import nor.util.log.Logger;

public class TextResource extends Resource{

	// ロガー
	private static final Logger LOGGER = Logger.getLogger(TextResource.class);

	private static final ContentType DefaultContentType = new ContentType("text/plain");

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
		super(name);
		LOGGER.entering("<init>", name, text, type);
		assert text != null;
		assert type != null;

		this.text = text;
		this.type = type;

		this.modified = Calendar.getInstance().getTime();

		LOGGER.exiting("<init>");
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

		this(name, text, DefaultContentType);

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
		LOGGER.entering("getText");

		final String ret = this.text;

		LOGGER.exiting("getText", ret);
		return ret;

	}

	/**
	 * リソース文字列を設定する．
	 *
	 * @param text このリソースに設定する文字列
	 */
	public void setText(final String text){
		LOGGER.entering("setText", text);
		assert text != null;

		this.text = text;
		this.modified = Calendar.getInstance().getTime();

		LOGGER.exiting("setText");
	}


	@Override
	public HttpResponse doGet(final String path, final HttpRequest request){
		LOGGER.entering("doGet", path, request);
		assert path != null;
		assert request != null;

		final HttpResponse ret = request.createResponse(Status.OK, this.text);
		final HttpHeader header = ret.getHeader();
		header.add(HeaderName.ContentType, this.type.toString());

		LOGGER.exiting("doGet", ret);
		return ret;

	}

	@Override
	public String toString(){

		return this.text + "(" + this.modified + ")";

	}

}


