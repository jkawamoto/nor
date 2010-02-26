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
 * HTTP/1.1で定義されるメソッド．
 *
 * @author KAWAMOTO Junpei
 *
 */
public enum Method {

	/**
	 * GETメソッド．
	 * <p>
	 * GET メソッドは、 Request-URI で識別される (エンティティ形式の) 情報ならなんでも回収する事を意味する。
	 * もし、 Request-URI がデータ生産プロセス{data-producing process} を参照するのであれば、それはレスポンスの
	 * エンティティとして返されるであろうとして生産されるデータであり、もしそのテキストがプロセスの出力として
	 * 生じるのでなければ、プロセスのソーステキストではない。
	 * </p>
	 * <p>
	 * リクエストメッセージに If-Modified-Since, If-Unmodified-Since, If-Match, If-None-Match, If-Range の
	 * いずれかのヘッダフィールドを含んでいる場合、GET メソッドの意味論は条件付き GET に変わる。
	 * 条件付き GET メソッドは、エンティティがその条件付きヘッダフィールドによって表される状況下でのみ転送されるようにリクエストする。
	 * 条件付き GET メソッドは、キャッシュされるエンティティに複数のリクエストを要求する事や、
	 * クライアントによってすでに保持されているデータを転送する事無く清新できるようにする事で、
	 * 不必要なネットワークの使用を減らそうというものである。
	 * </p>
	 * <p>
	 * リクエストメッセージに Range ヘッダフィールドを含んでいる場合、GET メソッドの意味論は "部分的 GET" に変わる。
	 * 部分的 GET は、section 14.35 で示されるように、転送されるエンティティの一部のみを要求する。
	 * 部分的 GET メソッドは、クライアントによって既に保持されているデータを転送する事無くエンティティを部分的に取得させて、
	 * 完全なものにできるようにする事で、不必要なネットワークの使用を減らそうというものである。
	 * <p>
	 */
	GET,

	/**
	 * HEADメソッド．
	 * <p>
	 * HEAD メソッドは、サーバがレスポンスにおいてメッセージボディを返し<em>てはならない</em>事を除けば GET と同一である。
	 * HEAD リクエストへのレスポンスにおける HTTP ヘッダに含まれる外部情報は、GET リクエストへのレスポンスで送られる情報と
	 * 同一である<em>べきである</em>。
	 * このメソッドは、エンティティボディ自身を転送する事なしにリクエストによって意味されるエンティティに付いての
	 * 外部情報を得るために使用される。このメソッドは、ハイパーテキストリンクの正当性、アクセス可能性、
	 * 最近の修正のテストのために、しばしば使用される。
	 * </p>
	 * <p>
	 * HEAD リクエストへのレスポンスは、そのレスポンスに含まれる情報はリソースから前もってキャッシュされたエンティティを
	 * 更新するために使う事が<em>できる</em>、という意味でキャッシュ可能である<em>かもしれない</em>。
	 * もし、新しいフィールド値がキャッシュされたエンティティは (Content-Length, Content-MD5, ETag, Last-Modified 各値の
	 * 変更によって示されるように) 現在のエンティティと違うという事を示すならば、キャッシュはそのキャッシュエンティティを
	 * 新鮮でないものとして扱わ<em>なければならない</em>。
	 * </p>
	 */
	HEAD,

	/**
	 * POSTメソッド．
	 * <p>
	 * POST メソッドは、サーバがリクエストライン内の Request-URI により識別されるリソースへの新しい従属{subordinate}
	 * として、リクエストに同封されるエンティティを受け入れる事を要求するために使用される。
	 * POST は以下の機能のカバーするための画一的メソッドとして設計されている。
	 * </p>
	 * <ul>
	 * 	<li>既存リソースの注釈</li>
	 * 	<li>掲示板、ニュースグループ、メーリングリスト、あるいはその類の記事グループへのメッセージの投稿</li>
	 * 	<li>フォーム提出の結果のような、データ処理プロセス {data-handling process} へのデータブロックの供給</li>
	 * 	<li>追加動作を通したデータベースの拡張</li>
	 * </ul>
	 * <p>
	 * POST メソッドによって実行される実際の機能はサーバによって決定され、通常は Request-URI に依存する。
	 * ポストされたエンティティは、ファイルがディレクトリに従属し、ニュース記事がそれがポストされたニュースグループに従属し、
	 * レコードがそのデータベースに従属しているという事と同じ形で、その URI に従属する。
	 * </p>
	 * <p>
	 * POST メソッドによって実行される動作は、URI によって識別されうるリソースという結果にはならないかもしれない。
	 * この場合、200 (OK) か 204 (No Content) が適切なレスポンスステータスであり、それはレスポンスが結果を記述した
	 * エンティティを含んでいるかどうかに依存する。
	 * </p>
	 * <p>
	 * リソースがオリジンサーバで既に生成されている場合、レスポンスは 201 (Created) であり、リクエストのステータス、
	 * 新しいリソースへの参照、Location ヘッダ (section <a href="#Sec14.30">14.30</a>) を記述したエンティティを
	 * 含む<em>べきである</em>。
	 * </p>
	 * <p>
	 * レスポンスが適切な Cache-Control や Expires ヘッダフィールドを含んでいなければ、このメソッドのレスポンスは
	 * キャッシュ可能ではない。
	 * しかしながら、303 (See Other) レスポンスは、ユーザエージェントにキャッシュ可能なリソースの検索を指示するために使用される。
	 * </p>
	 * <p>
	 * POST リクエストは、section <a href="#Sec8.2">8.2</a> にあるメッセージ転送要求に従わ<em>なければならない</em>。
	 * </p>
	 * <p>
	 * セキュリティの考察については、section <a href="#Sec15.1.3">15.1.3</a> 参照。
	 * </p>
	 */
	POST,
	PUT,
	DELETE,
	OPTIONS,
	TRACE,
	CONNECT,
	PATCH,
	LINK,
	UNLINK;

	public String toString(){

		return this.name().toLowerCase();

	}

	public static Method getMethod(final String value){

		if(GET.toString().equalsIgnoreCase(value)){

			return GET;

		}else if(HEAD.toString().equalsIgnoreCase(value)){

			return HEAD;

		}else if(POST.toString().equalsIgnoreCase(value)){

			return POST;

		}else if(PUT.toString().equalsIgnoreCase(value)){

			return PUT;

		}else if(DELETE.toString().equalsIgnoreCase(value)){

			return DELETE;

		}else if(OPTIONS.toString().equalsIgnoreCase(value)){

			return OPTIONS;

		}else if(TRACE.toString().equalsIgnoreCase(value)){

			return TRACE;

		}else if(CONNECT.toString().equalsIgnoreCase(value)){

			return CONNECT;

		}else if(PATCH.toString().equalsIgnoreCase(value)){

			return PATCH;

		}else if(LINK.toString().equalsIgnoreCase(value)){

			return LINK;

		}else if(UNLINK.toString().equalsIgnoreCase(value)){

			return UNLINK;

		}

		return null;

	}

	public boolean equalsIgnoreCase(final String value){

		return this.name().equalsIgnoreCase(value);

	}

}


