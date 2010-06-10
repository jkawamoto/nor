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

import nor.core.proxy.filter.EditingByteFilter;
import nor.core.proxy.filter.EditingStringFilter;
import nor.core.proxy.filter.FilterRegister;
import nor.core.proxy.filter.ReadonlyByteFilter;
import nor.core.proxy.filter.ReadonlyStringFilter;

class FilterRegisterImpl implements FilterRegister {

	private final List<EditingByteFilter> editingByteFilters = new ArrayList<EditingByteFilter>();
	private final List<EditingStringFilter> editingStringFilters = new ArrayList<EditingStringFilter>();
	private final List<ReadonlyByteFilter> readonlyByteFilters = new ArrayList<ReadonlyByteFilter>();
	private final List<ReadonlyStringFilter> readonlyStringFilters = new ArrayList<ReadonlyStringFilter>();

	/* (非 Javadoc)
	 * @see nor.core.proxy.FilterRegister#add(nor.core.proxy.filter.EditingByteFilter)
	 */
	public void add(final EditingByteFilter filter){

		this.editingByteFilters.add(filter);

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.FilterRegister#add(nor.core.proxy.filter.EditingStringFilter)
	 */
	public void add(final EditingStringFilter filter){

		this.editingStringFilters.add(filter);

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.FilterRegister#add(nor.core.proxy.filter.ReadonlyByteFilter)
	 */
	public void add(final ReadonlyByteFilter filter){

		this.readonlyByteFilters.add(filter);

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.FilterRegister#add(nor.core.proxy.filter.ReadonlyStringFilter)
	 */
	public void add(final ReadonlyStringFilter filter){

		this.readonlyStringFilters.add(filter);

	}


	List<EditingByteFilter> getEditingByteFilters(){

		return this.editingByteFilters;

	}

	List<EditingStringFilter> getEditingStringFilters(){

		return this.editingStringFilters;

	}


	List<ReadonlyByteFilter> getReadonlyByteFilters(){

		return this.readonlyByteFilters;

	}

	List<ReadonlyStringFilter> getReadonlyStringFilters(){

		return this.readonlyStringFilters;

	}

	boolean readonly(){

		return (this.editingByteFilters.size() == 0) && (this.editingStringFilters.size() == 0);

	}

}
