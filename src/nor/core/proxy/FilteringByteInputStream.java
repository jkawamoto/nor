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
package nor.core.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import nor.core.proxy.filter.EditingByteFilter;
import nor.core.proxy.filter.ReadonlyByteFilter;

class FilteringByteInputStream extends InputStream{

	private final ReadableByteChannel in;

	private final List<EditingByteFilter> editingFilters;
	private final List<ReadonlyByteFilter> readonlyFilters;

	private ByteBuffer buffer;

	private boolean isEOF = false;

	public FilteringByteInputStream(final InputStream in,final List<EditingByteFilter> editingFilters, final List<ReadonlyByteFilter> readonlyFilters){

		this.in = Channels.newChannel(in);

		this.editingFilters = editingFilters;
		this.readonlyFilters = readonlyFilters;

		this.buffer = ByteBuffer.allocate(1024*64);
		this.buffer.limit(0);

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

		// 転送が完了しているか
		for(final ReadonlyByteFilter f : this.readonlyFilters){

			f.close();

		}

		for(final EditingByteFilter f: this.editingFilters){

			f.close();

		}

		this.in.close();

	}


	@Override
	public int available() throws IOException {

		//return this.size - this.current;
		return this.buffer.limit() - this.buffer.position();

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

		this.buffer.clear();
		if(this.in.read(this.buffer) < 0){

			this.isEOF = true;
			return false;

		}

		this.buffer.flip();


		for(final ReadonlyByteFilter f : this.readonlyFilters){

			f.update(this.buffer);
			this.buffer.rewind();

		}

		for(final EditingByteFilter f : this.editingFilters){

			this.buffer = f.update(this.buffer);
			this.buffer.flip();

		}

		return true;

	}

}
