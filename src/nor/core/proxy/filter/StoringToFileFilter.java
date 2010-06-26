package nor.core.proxy.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class StoringToFileFilter extends ReadonlyByteFilterAdapter{

	private final File dest;
	private final File tmp;
	private final WritableByteChannel out;

	private boolean alive = true;

	private List<CloseEventListener> listeners = new ArrayList<CloseEventListener>();

	public StoringToFileFilter(final File dest) throws IOException{

		this.dest = dest;

		int i = 1;
		File cand = new File(this.dest.getAbsolutePath() + ".tmp");
		while(cand.exists()){

			cand = new File(String.format("%s.%d.tmp", this.dest.getAbsolutePath(), i++));

		}
		this.tmp = cand;

		this.out = new FileOutputStream(tmp).getChannel();

	}

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

	public void addListener(final CloseEventListener l){

		this.listeners.add(l);

	}

	public void removeListener(final CloseEventListener l){

		this.listeners.remove(l);

	}

	public interface CloseEventListener{

		public void close(final boolean succeeded);

	};

}
