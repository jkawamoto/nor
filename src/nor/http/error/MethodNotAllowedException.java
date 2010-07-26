package nor.http.error;

import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Method;
import nor.http.Status;

@SuppressWarnings("serial")
public class MethodNotAllowedException extends HttpException{

	private final Method[] allowed;

	public MethodNotAllowedException(final Method [] allowed){
		this(null, null, allowed);
	}

	public MethodNotAllowedException(final String message, final Method [] allowed){
		this(message, null, allowed);
	}


	public MethodNotAllowedException(final Throwable cause, final Method [] allowed){
		this(null, cause, allowed);
	}

	public MethodNotAllowedException(final String message, final Throwable cause, final Method [] allowed){
		super(Status.MethodNotAllowed, message, cause);

		this.allowed = allowed;
	}

	public HttpResponse createResponse(final HttpRequest request){

		final HttpResponse ret = super.createResponse(request);
		final HttpHeader header = ret.getHeader();
		for(final Method m : this.allowed){

			header.add(HeaderName.Allow, m.toString());

		}

		return ret;

	}

}
