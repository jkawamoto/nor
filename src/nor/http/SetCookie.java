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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SetCookieヘッダをラップするクラス．
 * SetCookieヘッダは以下のようなフォーマットになっている．
 * <p>
 * Set-Cookie: num=123456; expires=Sun, 10-Jun-2001 12:00:00 GMT; path=/HTTP/
 * </p>
 *
 * @author Junpei Kawamoto
 *
 */
public class SetCookie implements Iterable<SetCookie.Entry>{

	private static String SP = ";";
	private static String EQ = "=";
	private static String SECURE = "secure";
	private static String EXPIRES = "expires";
	private static String PATH = "path";
	private static String DOMAIN = "domain";
	private static Pattern ENTRYP_ATTERN = Pattern.compile("\\s*([^=]+)=([^;]+)");

	public class Entry{

		private String key = null;
		private String value = null;
		private String date = null;
		private String path = null;
		private String domain = null;
		private boolean secure = false;

		private String src;

		public Entry(final String src){

			this.src = src;

			// 初期設定
			this.secure = false;

			for(final String e : src.split(SP)){


				if(SECURE.equalsIgnoreCase(e.trim())){

					this.secure = true;

				}else{

					final Matcher m = ENTRYP_ATTERN.matcher(e);
					if(m.find()){

						final String key = m.group(1);
						final String value = m.group(2);

						if(EXPIRES.equalsIgnoreCase(key)){

							this.date = value;

						}else if(PATH.equalsIgnoreCase(key)){

							this.date = value;

						}else if(DOMAIN.equalsIgnoreCase(key)){

							this.domain = value;

						}else{

							this.key = key;
							this.value = value;

						}

					}

				}

			}

		}

		public String getKey(){

			return this.key;

		}

		public String getValue(){

			return this.value;

		}

		public String getData(){

			return this.date;

		}

		public String getPath(){

			return this.path;

		}

		public String getDomain(){

			return this.domain;

		}

		public boolean getSecure(){

			return this.secure;

		}

		public void setKey(final String key){

			this.key = key;
			this.update();

		}

		public void setValue(final String value){

			this.value = value;
			this.update();

		}

		public void setDate(final String date){

			this.date = date;
			this.update();

		}

		public void setPath(final String path){

			this.path = path;
			this.update();

		}

		public void setDomain(final String domain){

			this.domain = domain;
			this.update();

		}

		public void setSecure(final boolean secure){

			this.secure = secure;
			this.update();

		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString(){

			return this.src;

		}

		private void update(){

			final StringBuffer buf = new StringBuffer();

			buf.append(this.key);
			buf.append(EQ);
			buf.append(this.value);

			if(this.date != null){

				buf.append(SP);

				buf.append(EXPIRES);
				buf.append(EQ);
				buf.append(this.date);

			}

			if(this.path != null){

				buf.append(SP);

				buf.append(PATH);
				buf.append(EQ);
				buf.append(this.path);

			}

			if(this.domain != null){

				buf.append(SP);

				buf.append(DOMAIN);
				buf.append(EQ);
				buf.append(this.domain);

			}

			if(this.secure){

				buf.append(SP);

				buf.append(SECURE);

			}

			this.src = buf.toString();

		}


	}

	private final HttpHeader header;
	private final List<Entry> entries = new ArrayList<Entry>();

	/**
	 * ロガー
	 */
	private static final Logger LOGGER = Logger.getLogger(SetCookie.class.getName());

	//============================================================================
	//  コンストラクタ
	//============================================================================
	private SetCookie(final HttpHeader header){

		this.header = header;
		for(final String e : header.get(HeaderName.SetCookie).split("\n")){

			this.entries.add(new Entry(e));

		}

	}

	//============================================================================
	//  public メソッド
	//============================================================================
	public void add(final Entry e){

		this.entries.add(e);
		this.update();

	}

	public Entry get(final int i){

		return this.entries.get(i);

	}

	/**
	 * ヘッダ内容をクリアする．
	 */
	public void clear(){
		LOGGER.entering(SetCookie.class.getName(), "clear");

		this.entries.clear();
		this.header.remove(HeaderName.SetCookie);

		LOGGER.exiting(SetCookie.class.getName(), "clear");
	}

	public void remove(final Entry e){

		this.entries.remove(e);
		this.update();

	}

	@Override
	public Iterator<Entry> iterator() {

		return this.entries.iterator();

	}

	public int size(){

		return this.entries.size();

	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		LOGGER.entering(SetCookie.class.getName(), "toString");

		final StringBuffer buf = new StringBuffer();
		if(this.entries.size() != 0){

			for(final Entry e : this.entries){

				buf.append(e.toString());
				buf.append(", ");

			}
			buf.setLength(buf.length()-2);

		}

		final String ret = buf.toString();
		LOGGER.exiting(SetCookie.class.getName(), "toString", ret);
		return ret;
	}

	//============================================================================
	//  private methods
	//============================================================================
	private void update(){

		this.header.set(HeaderName.SetCookie, this.toString());

	}

	//============================================================================
	//  public static methods
	//============================================================================
	public static SetCookie get(final HttpHeader header){

		return new SetCookie(header);

	}

}


