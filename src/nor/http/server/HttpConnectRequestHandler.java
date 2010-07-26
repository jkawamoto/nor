package nor.http.server;

import java.nio.channels.SocketChannel;

import nor.http.HttpRequest;
import nor.http.error.HttpException;

public interface HttpConnectRequestHandler {

	public SocketChannel doRequest(final HttpRequest request) throws HttpException;

}
