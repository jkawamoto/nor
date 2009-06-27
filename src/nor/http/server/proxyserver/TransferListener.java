package nor.http.server.proxyserver;

import nor.http.Body2.IOStreams;

public interface TransferListener {

	public void update(final IOStreams streams);

}
