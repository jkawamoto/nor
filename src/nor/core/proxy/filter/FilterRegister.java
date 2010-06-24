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
package nor.core.proxy.filter;

/**
 * ストリームフィルタの登録先．
 *
 * @author Junpei Kawamoto
 * @since 0.1
 */
public interface FilterRegister {

	/**
	 * EditingByteFilter を登録します．
	 *
	 * @param filter 登録する EditingByteFilter フィルタ
	 */
	public abstract void add(final EditingByteFilter filter);

	/**
	 * EditingStringFilter を登録します．
	 *
	 * @param filter 登録する EditingStringFilter フィルタ
	 */
	public abstract void add(final EditingStringFilter filter);

	/**
	 * ReadonlyByteFilter を登録します．
	 *
	 * @param filter 登録する ReadonlyByteFilter フィルタ
	 */
	public abstract void add(final ReadonlyByteFilter filter);

	/**
	 * ReadonlyStringFilter を登録します．
	 *
	 * @param filter 登録する ReadonlyStringFilter フィルタ
	 */
	public abstract void add(final ReadonlyStringFilter filter);

}