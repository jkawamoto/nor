/**
 *  Copyright (C) 2009 KAWAMOTO Junpei
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
package nor.http.error;

import nor.http.Status;

public class NotFoundException extends HttpException{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public static final Status status = Status.NotFound;

	public NotFoundException(){
		this(null, null);
	}

	public NotFoundException(final String message){
		this(message, null);
	}


	public NotFoundException(final Throwable cause){
		this(null, cause);
	}

	public NotFoundException(final String message, final Throwable cause){
		super(status, message, cause);
	}

}
