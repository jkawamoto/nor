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
package nor.core.proxy;

import java.util.ArrayList;
import java.util.List;

import nor.core.proxy.filter.ByteBodyFilter;
import nor.core.proxy.filter.TextBodyFilter;

public class FilterContainer {

	private final List<ByteBodyFilter> bFilters = new ArrayList<ByteBodyFilter>();
	private final List<TextBodyFilter> tFilters = new ArrayList<TextBodyFilter>();

	private boolean readonly = true;

	public void add(final ByteBodyFilter filter){

		this.bFilters.add(filter);
		this.readonly |= filter.readonly();

	}

	public void add(final TextBodyFilter filter){

		this.tFilters.add(filter);
		this.readonly |= filter.readonly();

	}

	boolean isReadonly(){

		return this.readonly;

	}

	List<ByteBodyFilter> getByteBodyFilters(){

		return this.bFilters;

	}

	List<TextBodyFilter> getTextBodyFilters(){

		return this.tFilters;

	}


}
