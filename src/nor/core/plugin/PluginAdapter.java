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
package nor.core.plugin;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;

public abstract class PluginAdapter implements Plugin{

	@Override
	public MessageHandler[] messageHandlers(){

		final MessageHandler h = this.messageHandler();
		if(h != null){

			return new MessageHandler[]{h};

		}else{

			return null;

		}

	}

	@Override
	public RequestFilter[] requestFilters() {

		final RequestFilter f = this.requestFilter();
		if(f != null){

			return new RequestFilter[]{f};

		}else{

			return null;

		}

	}

	@Override
	public ResponseFilter[] responseFilters() {

		final ResponseFilter f = this.responseFilter();
		if(f != null){

			return new ResponseFilter[]{f};

		}else{

			return null;

		}

	}

	@Override
	public void close(){

	}

	protected MessageHandler messageHandler(){

		return null;

	}

	protected RequestFilter requestFilter() {

		return null;

	}

	protected ResponseFilter responseFilter() {

		return null;

	}

}
