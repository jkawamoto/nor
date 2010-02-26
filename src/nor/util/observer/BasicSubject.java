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
// $Id: BasicSubject.java 411 2010-01-11 09:51:02Z kawamoto $
package nor.util.observer;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Observerパターンにおける，Subjectを表すクラスです．
 * SubjectクラスはObserverの登録及び解除，そして通知に関するメソッドを提供し，
 * 利用者が簡単にObserverパターンを利用できるようにするユーティリティクラスです．
 * 
 * 主な利用方法は，通知を行うクラスが，このSubjectクラスフィールドを持ち，
 * attach，detach，notifyといった関連メソッドを委譲します．
 * 
 * @param <Param> Observerに通知するパラメータの型
 * @author KAWAMOTO Junpei
 *
 */
public final class BasicSubject<Param, ObserverType extends Observer<Param>> implements Subject<Param, ObserverType>{

	/**
	 * ロガー
	 */ 
	private static final Logger LOGGER = Logger.getLogger(BasicSubject.class.getName());

	/**
	 * Observer集合
	 */
	private final Set<ObserverType> _ovservers = new HashSet<ObserverType>();


	/**
	 * 新しいSubjectを作成します．
	 * 通知先として何も登録されていない新しいSubjectを作成します．
	 * 通知先が登録されていない時にnotifyが呼ばれた場合，何も行われません．
	 * 
	 */
	private BasicSubject(){
		LOGGER.entering(BasicSubject.class.getName(), "<init>");
		LOGGER.exiting(BasicSubject.class.getName(), "<init>");
		
	}
	
	
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.observer.Subject#attach(jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.observer.Observer)
	 */
	@Override
	public final void attach(final ObserverType observer){
		LOGGER.entering(BasicSubject.class.getName(), "attach", observer);
		assert observer != null;
		
		this._ovservers.add(observer);

		LOGGER.exiting(BasicSubject.class.getName(), "attach");
		
	}

	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.observer.Subject#detach(jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.observer.Observer)
	 */
	@Override
	public final void detach(final ObserverType observer){
		LOGGER.entering(BasicSubject.class.getName(), "detach", observer);
		assert observer != null;
		
		this._ovservers.remove(observer);
		
		LOGGER.exiting(BasicSubject.class.getName(), "detach");

	}
	
	/* (non-Javadoc)
	 * @see jp.ac.kyoto_u.i.soc.db.j.kawamoto.util.observer.Subject#notify(java.lang.Object)
	 */
	@Override
	public final void notify(final Param param){
		LOGGER.entering(BasicSubject.class.getName(), "notify", param);
		assert param != null;
		
		for(Observer<Param> observer : this._ovservers){
			
			observer.update(param);
			
		}
		
		LOGGER.exiting(BasicSubject.class.getName(), "notify");

	}
	
	/**
	 * Subjectを作成する．
	 * このメソッドを利用した場合，型推論が利用でき，
	 * <pre>
	 *   Subject<E> sub = Subject.create();
	 * </pre>
	 * と記述できる．
	 * 
	 * @param <K> Observerに通知するパラメータの型
	 * @return 新しいSubjectオブジェクト
	 */
	public static <Param, ObserverType extends Observer<Param>> BasicSubject<Param, ObserverType> create(){
		
		return new BasicSubject<Param, ObserverType>();
		
	}
	
}


