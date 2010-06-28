package nor.core.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;

import nor.core.proxy.filter.MessageHandlerAdapter;
import nor.http.HeaderName;
import nor.http.HttpHeader;
import nor.http.HttpRequest;
import nor.http.HttpResponse;
import nor.http.Status;

class LocalContentsHandler extends MessageHandlerAdapter{

	private final Map<String, Contents> map = new HashMap<String, Contents>();

	public LocalContentsHandler(){
		super("^/.*");
	}

	@Override
	public HttpResponse doRequest(final HttpRequest request, final MatchResult url) {

		HttpResponse ret = null;
		for(final String path : this.map.keySet()){

			if(path.equals(request.getPath())){

				final Contents c  = this.map.get(path);

				ret = request.createResponse(Status.OK, c.getData());

				final HttpHeader header = ret.getHeader();
				header.set(HeaderName.CacheControl, "no-cache");
				header.set(HeaderName.ContentType, c.getType());

			}

		}

		return ret;

	}

	public void put(final String path, final Contents contents){

		this.map.put(path, contents);

	}

	public static class Contents{

		private final String data;
		private final String type;

		public Contents(final String data, final String type){

			this.data = data;
			this.type = type;

		}

		public String getData(){

			return this.data;

		}

		public String getType(){

			return this.type;

		}

	}

}
