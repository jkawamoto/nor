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

import java.io.Closeable;
import java.io.IOException;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;

public abstract class Plugin implements Closeable{

	// TODO: プラグイン設定管理用オブジェクトを追加

	public MessageHandler[] messageHandlers(){

		return new MessageHandler[0];

	}

	public RequestFilter[] requestFilters(){

		return new RequestFilter[0];

	}

	public ResponseFilter[] responseFilters(){

		return new ResponseFilter[0];

	}

	public void close() throws IOException{

	}

}
