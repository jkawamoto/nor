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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

import nor.core.proxy.filter.EditingStringFilter;
import nor.core.proxy.filter.ReadonlyStringFilter;
import nor.util.SequentialInputStream;

class FilteringCharacterInputStream extends SequentialInputStream{

	private final List<EditingStringFilter> editingFilters;
	private final List<ReadonlyStringFilter> readonlyFilters;

	private final Charset charset;
	private final BufferedReader rin;

	private ByteBuffer buffer;

	private boolean isEOF = false;



	protected FilteringCharacterInputStream(final InputStream in, final Charset charset, final List<EditingStringFilter> editingFilters, final List<ReadonlyStringFilter> readonlyFilters){
		super(in);

		this.charset = charset;
		if(this.charset == null){

			this.rin = new BufferedReader(new InputStreamReader(in));

		}else{

			this.rin = new BufferedReader(new InputStreamReader(in, charset));

		}

		this.editingFilters = editingFilters;
		this.readonlyFilters = readonlyFilters;


	}

	@Override
	public int read() throws IOException {

		if(this.isEOF){

			return -1;

		}

		if(this.available() == 0){

			if(!this.reload()){

				return -1;


			}

		}

		return this.buffer.get() & 0xff;

	}


	@Override
	public void close() throws IOException {

		for(final ReadonlyStringFilter f : this.readonlyFilters){

			f.close();

		}

		for(final EditingStringFilter f : this.editingFilters){

			f.close();

		}

		this.in.close();

	}


	@Override
	public int available() throws IOException {

		if(this.buffer == null){

			return 0;

		}else{

			return this.buffer.limit() - this.buffer.position();

		}

	}


	@Override
	public int read(byte[] b, int off, int len) throws IOException {

		if(off < 0){

			throw new IndexOutOfBoundsException("offが負");

		}else if(len < 0){

			throw new IndexOutOfBoundsException("lenが負");

		}else if(len > b.length - off){

			throw new IndexOutOfBoundsException("lenがb.length - offよりも大きい");

		}


		// ストリームの終わりに達していたら
		if(this.isEOF){

			return -1;

		}

		// バッファにデータが残っていない場合
		if(this.available() == 0){

			// ストリームの終わりに達していたら終了
			if(!this.reload()){

				return -1;

			}

		}

		// 有効サイズとlenの小さい方分だけコピーする
		final int available = this.available();
		if(len > available){


			this.buffer.get(b, off, available);
			return available;

		}else{

			this.buffer.get(b, off, len);
			return len;

		}

	}

	private boolean reload() throws IOException{

		String line = this.rin.readLine();
		if(line == null){

			this.isEOF = true;
			return false;

		}

		for(final ReadonlyStringFilter f : this.readonlyFilters){

			f.update(line);

		}

		for(final EditingStringFilter f : this.editingFilters){

			line = f.update(line);

		}

		line = line + "\n";
		if(this.charset != null){

			this.buffer = ByteBuffer.wrap(line.getBytes(this.charset));

		}else{

			this.buffer = ByteBuffer.wrap(line.getBytes());

		}

		return true;

	}


}


