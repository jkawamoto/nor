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
package nor.http.nserver;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TimeoutableInputStream extends FilterInputStream{

	protected TimeoutableInputStream(final InputStream in) {
		super(in);
	}

	@Override
	public synchronized int read() throws IOException {

		try{
			for(int i = 0; i != 6; i++){

				if(this.available() == 0){

					wait(100);

				}else if(this.available() == -1){

					return -1;

				}else{

					break;

				}

			};
			// TODO 自動生成されたメソッド・スタブ
			return super.read();

		}catch(final InterruptedException e){

			e.printStackTrace();
			return -1;

		}
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {

		try{

			for(int i = 0; i != 6; i++){

				if(this.available() == 0){

					wait(100);

				}else if(this.available() == -1){

					return -1;

				}else{

					break;

				}

			};
			// TODO 自動生成されたメソッド・スタブ
			System.out.println(this.available());
			return super.read(b, off, this.available());

		}catch(final InterruptedException e){

			e.printStackTrace();
			return -1;

		}
	}

//	@Override
//	public synchronized int read(byte[] b) throws IOException {
//
//		try{
//			for(int i = 0; i != 6; i++){
//
//				if(this.available() == 0){
//
//					wait(100);
//
//				}else if(this.available() == -1){
//
//					break;
//
//				}
//
//			};
//			// TODO 自動生成されたメソッド・スタブ
//			return super.read(b);
//
//		}catch(final InterruptedException e){
//
//			e.printStackTrace();
//			return -1;
//
//		}
//	}



}
