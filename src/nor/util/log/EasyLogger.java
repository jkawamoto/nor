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
package nor.util.log;

public class EasyLogger extends LoggedObject{

	public EasyLogger(final String className){
		super(className);
	}

	public void entering(final String method, final Object ...params){
		super.entering(method, params);
	}

	public void exiting(final String method, final Object ...params){
		super.exiting(method, params);
	}

	public void warning(final String msg){

		this.LOGGER.warning(msg);

	}

}


