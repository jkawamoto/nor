package nor.core.proxy.filter;

import java.io.Closeable;
import java.nio.ByteBuffer;

public interface ReadonlyByteFilter extends Closeable{


	public void update(final ByteBuffer in);

	@Override
	public void close();


}
