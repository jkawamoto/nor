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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * 転送されるストリームデータをファイルに保存するフィルタ．
 *
 * @author Junpei Kawamoto
 * @since 0.1.20100629
 *
 */
public class StoringToFileFilter extends ReadonlyByteFilterAdapter{

	private final File dest;
	private final File tmp;
	private final WritableByteChannel out;

	private boolean alive = true;

	private List<CloseEventListener> listeners = new ArrayList<CloseEventListener>();

	private static final String TMP = ".tmp";

	/**
	 * 保存先ファイル名を指定して StoringToFileFilter を作成する．
	 *
	 * @param dest 保存先のファイル
	 * @throws IOException 保存先ファイルの作成にエラーが発生した場合
	 */
	public StoringToFileFilter(final File dest) throws IOException{

		this.dest = dest;

		int i = 1;
		File cand = new File(this.dest.getAbsolutePath() + TMP);
		while(cand.exists()){

			cand = new File(String.format("%s.%d%s", this.dest.getAbsolutePath(), i++, TMP));

		}
		this.tmp = cand;

		this.out = new FileOutputStream(tmp).getChannel();

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.ReadonlyByteFilter#update(java.nio.ByteBuffer)
	 */
	@Override
	public final void update(final ByteBuffer in) {

		if(this.alive){

			try{

				out.write(in);

			}catch(final IOException e){

				e.printStackTrace();
				this.alive = false;

			}

		}

	}

	/* (非 Javadoc)
	 * @see nor.core.proxy.filter.ReadonlyByteFilterAdapter#close()
	 */
	@Override
	public final void close(){

		try{

			out.close();

		}catch(IOException e){

			e.printStackTrace();
			this.alive = false;

		}

		if(this.alive && !dest.exists()){

			tmp.renameTo(dest);

		}else{

			tmp.delete();


		}

		for(final CloseEventListener l : this.listeners){

			l.close(this.alive);

		}

	}

	/**
	 * 転送終了イベントのリスナを登録する．
	 *
	 * @param listener 登録するイベントリスナ
	 */
	public void addListener(final CloseEventListener listener){

		this.listeners.add(listener);

	}

	/**
	 * 転送終了イベントのリスナを削除する．
	 *
	 * @param listener 登録解除するイベントリスナ
	 */
	public void removeListener(final CloseEventListener listener){

		this.listeners.remove(listener);

	}

	/**
	 * 転送中に作成された一時ファイルを削除する．
	 * このフィルタは転送中に，tmp という拡張子を持つ一時ファイルを作成します．
	 * このメソッドは，指定したディレクトリないにある .tmp ファイルを削除します．
	 *
	 * @param dir 一時ファイルが保存されているディレクトリ
	 */
	public static void deleteTemplaryFiles(final File dir){

		final File[] temps = dir.listFiles(new FilenameFilter(){

			@Override
			public boolean accept(final File dir, final String name) {

				return name.endsWith(TMP);

			}

		});

		for(final File tmp : temps){

			tmp.delete();

		}

	}

	/**
	 * 転送終了イベントのリスナが実装すべきインタフェース.
	 *
	 * @author Junpei Kawamoto
	 *
	 */
	public interface CloseEventListener{

		/**
		 * 転送終了を通知する．
		 *
		 * @param succeeded 転送が成功した場合 true
		 */
		public void close(final boolean succeeded);

	};

}
