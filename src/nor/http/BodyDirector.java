/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

//import org.ho.yaml.Yaml;

/**
 * @author KAWAMOTO Junpei
 *
 */
class BodyDirector {

	// urlprefix->type->subtype->builder
	private Map<String, Map<String, Map<String, BodyType>>> _builders = new HashMap<String, Map<String, Map<String, BodyType>>>();

	public BodyDirector(){

//		try {
//
//			final Map<String, Map<String, Map<String, String>>> config = Yaml.loadType(new File("bodydirector.yaml"), (new HashMap<String, Map<String, Map<String, String>>>()).getClass());
//
//			for(final String prefix : config.keySet()){
//
//				for(final String type : config.get(prefix).keySet()){
//
//					for(final String subtype : config.get(prefix).get(type).keySet()){
//
//						this.set(prefix, type, subtype, BodyType.valueOf(config.get(prefix).get(type).get(subtype)));
//
//					}
//
//				}
//
//			}
//
//
//		} catch (FileNotFoundException e) {
//
//			// TODO 自動生成された catch ブロック
//			e.printStackTrace();
//
//			// 初期設定
//			this.set("*", "*", "*", BodyType.BinaryBody);
//			this.set("*", "text", "*", BodyType.TextBody);
//			this.set("*", "*", "xhtml+xml", BodyType.TextBody);
//
//
//			try {
//				save();
//			} catch (IOException e1) {
//				// TODO 自動生成された catch ブロック
//				e1.printStackTrace();
//			}
//
//		}

	}

	public void save() throws IOException{

//		final Map<String, Map<String, Map<String, String>>> dump = new HashMap<String, Map<String, Map<String, String>>>();
//		for(final String prefix : this._builders.keySet()){
//
//			final Map<String, Map<String, String>> entry = new HashMap<String, Map<String, String>>();
//			for(final String type : this._builders.get(prefix).keySet()){
//
//				final Map<String, String> ins = new HashMap<String, String>();
//				for(final String subtype : this._builders.get(prefix).get(type).keySet()){
//
//
//					ins.put(subtype, this._builders.get(prefix).get(type).get(subtype).name());
//
//				}
//
//				entry.put(type, ins);
//
//			}
//
//			dump.put(prefix, entry);
//
//		}
//
//		Yaml.dump(dump, new File("bodydirector.yaml"));


	}


	public Body build(final Response response, final InputStream input) throws IOException{

		final int code = response.getCode();
		if((100 <= code && code < 200) || code == 204 || code == 304){

			// メッセージボディなし
			return new EmptyBody(response);

		}

		// urlはstartwithを呼び出す
		Map<String, Map<String, BodyType>> entry = null;
//		for(final String prefix : this._builders.keySet()){
//
//			if(response.getRequest().getPath().startsWith(prefix)){
//
//				entry = this._builders.get(prefix);
//				break;
//
//			}
//
//		}
		if(entry == null){

			// どのprefixにもマッチしなかった場合
			entry = this._builders.get("*");

		}

		Map<String, BodyType> ins = entry.get("*");
		if(entry.containsKey(response.getHeader().getContentType().getMIMEType())){

			ins = entry.get(response.getHeader().getContentType().getMIMEType());

		}

		BodyType bodytype = ins.get("*");
		if(ins.containsKey(response.getHeader().getContentType().getMIMESubtype())){

			bodytype = ins.get(response.getHeader().getContentType().getMIMESubtype());

		}

		return bodytype.create(response, input);

	}

	public void set(final String prefix, final String type, final String subtype, final BodyType bodytype){

		if(!this._builders.containsKey(prefix)){

			this._builders.put(prefix, new HashMap<String, Map<String, BodyType>>());

		}

		if(!this._builders.get(prefix).containsKey(type)){

			this._builders.get(prefix).put(type, new HashMap<String, BodyType>());

		}

		if(this._builders.get(prefix).get(type).containsKey(subtype)){

			this._builders.get(prefix).get(type).remove(subtype);

		}
		this._builders.get(prefix).get(type).put(subtype, bodytype);

	}

	public enum BodyType{

		BinaryBody{

			@Override
			public Body create(Response response, InputStream input) throws IOException {

				return new BinaryBody(response, input);

			}

		},

		TextBody{

			@Override
			public Body create(Response response, InputStream input) throws IOException {

				return new TextBody(response, input);

			}

		};


		protected abstract Body create(final Response response, final InputStream input) throws IOException;


	}

}
