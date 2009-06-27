package nor.http.server.proxyserver;

import java.io.InputStream;

public interface TransferredListener {

	public void update(final InputStream input, final int size);

}
