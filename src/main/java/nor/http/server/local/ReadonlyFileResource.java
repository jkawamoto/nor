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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nor.http.ContentType;
import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.error.NotFoundException;
import nor.util.log.Logger;

/**
 * ファイルと関連付けられたリソースを表すクラス．
 *
 *
 * @author KAWAMOTO Junpei
 *
 */
public class ReadonlyFileResource extends Resource{

	/**
	 * 関連付けられたファイル
	 */
	private final File file;

	/**
	 * コンテンツタイプ
	 */
	private final ContentType type;

	/**
	 * 日付フォーマット
	 *
	 */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(ReadonlyFileResource.class);

	//====================================================================
	//  コンストラクタ
	//====================================================================
	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String name, final File file, final ContentType type){
		super(name);
		LOGGER.entering("<init>", name, file, type);
		assert name != null;
		assert file != null;
		assert type != null;

		this.file = file;
		this.type = type;

		LOGGER.exiting("<init>");
	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String name, final File file, final String type){

		this(name, file, new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String name, final String file, final ContentType type){

		this(name, new File(file), type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String name, final String file, final String type){

		this(name, new File(file), new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final File file, final ContentType type){

		this(file.getName(), file, type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final File file, final String type){

		this(file.getName(), file, type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String file, final ContentType type){

		this(new File(file), type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public ReadonlyFileResource(final String file, final String type){

		this(file, new ContentType(type));

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	@Override
	public HttpResponse doGet(final String path, final HttpRequest request) throws HttpException{
		LOGGER.entering("doGet", path, request);
		assert path != null;
		assert request != null;

		HttpResponse ret = null;
		if(this.file.canRead()){

			try{

				ret = request.createResponse(Status.OK, new FileInputStream(this.file), this.file.length());
				final HttpHeader header = ret.getHeader();
				header.add(HeaderName.ContentType, this.type.toString());
				header.add(HeaderName.LastModified, DATE_FORMAT.format(new Date(this.file.lastModified())));
				header.add(HeaderName.Date, DATE_FORMAT.format(Calendar.getInstance().getTime()));

			}catch(final FileNotFoundException e){

				throw new NotFoundException();

			}

		}

		LOGGER.exiting("doGet", ret);
		return ret;

	}

}
