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


public interface Subject<Param, ObserverType extends Observer<Param>> {
	
	/**
	 * 通知先を追加します．
	 * このSubjectが通知すべきObserverを追加します．追加されたすべてのObserverに対し
	 * notifyによる通知が届きます．
	 * 
	 * @param observer 新たに通知先に加えるObserverインスタンス
	 */
	public void attach(final ObserverType observer);

	/**
	 * 通知先の登録を解除します．
	 * このSubjectの通知先からObserverを除外します．登録されていないObserverインスタンスに対して
	 * このメソッドが呼ばれた場合，何も起こりません．
	 * 
	 * @param observer 通知先から除外するObserverインスタンス
	 */
	public void detach(final ObserverType observer);
	
	/**
	 * Observerに通知を行います．
	 * このSubjectに登録されているすべてのObserverに対し通知が行われます．
	 * 通知の順序は不定です．
	 * 
	 * @param param 通知の際に送信されるパラメータインスタンス
	 */
	public void notify(final Param param);


}


