package nor.http.server.proxyserver;

import nor.http.Header;
import nor.http.Request;
import nor.http.Body2.IOStreams;

public interface TransferListener {

	public void update(final Request request, final Header responseHeader, final IOStreams streams);

}
