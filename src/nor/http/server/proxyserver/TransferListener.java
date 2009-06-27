package nor.http.server.proxyserver;

import java.io.InputStream;
import java.io.OutputStream;

public interface TransferListener{

	public void update(final InputStream input, final OutputStream output);

}
