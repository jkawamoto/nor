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


/**
 *	HTTP/1.1 プロトコルにおけるエラーレスポンスを抽象化したクラスを提供します．
 *	このパッケージが提供するクラスを使用することで，簡単にエラーレスポンスを作成することができます．
 *
 *	エラーは Java 言語における例外を用いてハンドリングされ，HttpExeption クラスがそのルートクラスになります．
 *	開発者は，適切なエラーコードを用いて HttpException クラスを作成しスローすることで，
 *	フレームワークはエラーレスポンスを作成します．
 *
 *	また，よく利用されるエラーについてはショートカットとなるクラスが用意されています．
 *
 *	@since 0.1
 *	@version 0.1
 */
package nor.http.error;