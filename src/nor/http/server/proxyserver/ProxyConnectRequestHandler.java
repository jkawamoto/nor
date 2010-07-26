package nor.http.server.proxyserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;
import nor.http.error.HttpException;
import nor.http.server.HttpConnectRequestHandler;
import nor.util.log.Logger;

public class ProxyConnectRequestHandler implements HttpConnectRequestHandler{

	private final Router router;

	private static final Logger LOGGER = Logger.getLogger(ProxyConnectRequestHandler.class);

	public ProxyConnectRequestHandler(final Router router){

		this.router = router;

	}

	@Override
	public SocketChannel doRequest(final HttpRequest request) throws HttpException {
		assert request != null;

		final Pattern pat = Pattern.compile("(.+):(\\d+)");
		final Matcher m = pat.matcher(request.getPath());

		if(m.find()){

			try{

				final String host = m.group(1);
				final int port = Integer.valueOf(m.group(2));

				final Proxy p = this.router.query(request.getPath());
				if(p == Proxy.NO_PROXY){

					System.out.println("connecting to " + host);

					final InetSocketAddress addr = new InetSocketAddress(host, port);
					final SocketChannel ch = SocketChannel.open(addr);

					System.out.println("connected to " +  host);


					return ch;

				}else{

					/* TODO: 以下は未完成
					 */

					final SocketChannel ch = SocketChannel.open(p.address());

					// Send a CONNECT request
					final InputStream input = Channels.newInputStream(ch);
					final OutputStream output = Channels.newOutputStream(ch);

					request.output(output);
					output.flush();

					final HttpResponse response = request.createResponse(input);
					response.output(output);

					return ch;

				}


			}catch(final IOException e){

				LOGGER.catched(Level.FINE, "doRequest", e);
				throw new HttpException(Status.BadGateway, e);

			}

		}else{

			throw new HttpException(Status.BadRequest);

		}

	}

}
