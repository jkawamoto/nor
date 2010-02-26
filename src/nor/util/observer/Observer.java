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
package nor.util.observer;

/**
 * Observerパターンにおける，Observerを表すインタフェースです．
 * このインタフェースはObserverパターンの実装の簡略化の為に用意されたものです．
 * Observerを実装したクラスは同パッケージ内のSubjectクラスに登録され監視することが
 * 可能になります．
 *
 * @param <Param> Observerが受け取るパラメータの型
 * @author KAWAMOTO Junpei
 *
 */
public interface Observer<Param> {

	/**
	 * Subjectからの通知を受け取る．
	 * Subjectに対しnotifyが呼ばれると，登録されているObserverのupdateへ
	 * パラメータ型で指定されているクラスをパラメータとして通知されます．
	 *
	 * @param param 通知に対するパラメータ
	 */
	public void update(final Param param);

}
