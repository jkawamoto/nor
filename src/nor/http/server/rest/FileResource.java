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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import nor.http.ContentType;
import nor.http.ErrorResponseBuilder;
import nor.http.ErrorStatus;
import nor.http.HttpError;
import nor.http.HttpRequest;
import nor.http.HttpResponse;

// TODO: これはRead-onlyファイル。その他の種類も作成する。本来FileResourceはJavaにおけるFileクラスを表したものであるべき。
/**
 * ファイルと関連付けられたリソースを表すクラス．
 *
 *
 * @author KAWAMOTO Junpei
 *
 */
public class FileResource extends Resource{

	/**
	 * リソースの名前
	 */
	private final String _name;

	/**
	 * 関連付けられたファイル
	 */
	private final File _file;

	/**
	 * コンテンツタイプ
	 */
	private final ContentType _type;

	/**
	 * 日付フォーマット
	 *
	 */
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss zzz", java.util.Locale.US);

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(FileResource.class.getName());

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
	public FileResource(final String name, final File file, final ContentType type){
		LOGGER.entering(FileResource.class.getName(), "<init>", new Object[]{ name, file, type });
		assert name != null;
		assert file != null;
		assert type != null;

		this._name = name;
		this._file = file;
		this._type = type;

		LOGGER.exiting(FileResource.class.getName(), "<init>");

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final String name, final File file, final String type){

		this(name, file, new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final String name, final String file, final ContentType type){

		this(name, new File(file), type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param name リソースの名前
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final String name, final String file, final String type){

		this(name, new File(file), new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final File file, final ContentType type){

		this(file.getName(), file, type);

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final File file, final String type){

		this(file.getName(), file, new ContentType(type));

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final String file, final ContentType type){
		LOGGER.entering(FileResource.class.getName(), "<init>", new Object[]{file, type});
		assert file != null;
		assert type != null;

		this._file = new File(file);
		this._name = this._file.getName();
		this._type = type;

		LOGGER.exiting(FileResource.class.getName(), "<init>");

	}

	/**
	 * コンストラクタ
	 *
	 * @param file リソースが扱うファイル
	 * @param type リソースのコンテンツタイプ
	 */
	public FileResource(final String file, final String type){

		this(file, new ContentType(type));

	}

	//====================================================================
	//  public メソッド
	//====================================================================
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.rest.ResourceAdapter#toGet(java.lang.String, jp.ac.kyoto_u.i.soc.db.j.kawamoto.httpserver.HttpRequest)
	 */
	@Override
	public HttpResponse toGet(final String path, final HttpRequest request){
		LOGGER.entering(FileResource.class.getName(), "toGet", new Object[]{path, request});
		assert path != null;
		assert request != null;

		HttpResponse ret = null;
		if(this._file.canRead()){

			final StringBuilder header = new StringBuilder();
			header.append("HTTP/1.1 200 OK\n");
			header.append("Content-Type: ");
			header.append(this._type);
			header.append("\n");
			header.append("Last-Modified: ");
			header.append(DATE_FORMAT.format(new Date(this._file.lastModified())));
			header.append("\n");
			// アプリケーション名
			header.append("Server: ");
			header.append(System.getProperty("app.name"));
			header.append("\n");
			header.append("Content-Length: ");
			header.append(this._file.length());
			header.append("\n");
			header.append("Date: ");
			header.append(DATE_FORMAT.format(Calendar.getInstance().getTime()));
			header.append("\n\n");

			try {

				final InputStream fileIn = new FileInputStream(this._file);
				final InputStream headerIn = new ByteArrayInputStream(header.toString().getBytes());
				final InputStream in = new SequenceInputStream(headerIn, fileIn);

				ret = request.createResponse(in);

				in.close();
				headerIn.close();
				fileIn.close();

			} catch (FileNotFoundException e) {

				LOGGER.warning(e.getLocalizedMessage());

			} catch (IOException e) {

				LOGGER.warning(e.getLocalizedMessage());

			} catch (HttpError e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}

		}

		if(ret == null){

			ret = ErrorResponseBuilder.create(request, ErrorStatus.NotFound);

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

}
