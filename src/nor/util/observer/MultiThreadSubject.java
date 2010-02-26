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

import java.util.ArrayList;
import java.util.List;


public class MultiThreadSubject<Param, ObserverType extends Observer<Param>> implements Subject<Param, ObserverType>{

	private final List<ObserverType> _observers = new ArrayList<ObserverType>();


	@Override
	public void attach(final ObserverType observer) {

		if(!this._observers.contains(observer)){

			this._observers.add(observer);

		}

	}

	@Override
	public void detach(final ObserverType observer) {

		this._observers.remove(observer);

	}

	@Override
	public void notify(final Param param) {

		final NotifierManager m = new NotifierManager();
		for(final ObserverType o : this._observers){

			final Thread th = new Thread(new Notifier(m, o, param));
			th.setName(String.format("%s [%s]", o, param));
			th.start();

		}

		m.waitSync();

	}

	private class NotifierManager{

		private int _count = 0;

		public synchronized void addSync(){

			++this._count;

		}

		public synchronized void delSync(){

			--this._count;
			this.notify();

		}

		public void waitSync(){

			while(this._count != 0){

				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}

			}

		}

	}

	private class Notifier implements Runnable{

		private final NotifierManager _manager;
		private final ObserverType _observer;
		private final Param _param;

		public Notifier(final NotifierManager manager, final ObserverType observer, final Param param){

			this._manager = manager;
			this._observer = observer;
			this._param = param;

		}

		@Override
		public void run() {

			this._manager.addSync();
			this._observer.update(this._param);
			this._manager.delSync();

		}

	}

}


