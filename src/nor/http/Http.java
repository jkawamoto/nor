package nor.http;

public class Http {

	public static String VERSION = "1.1";
	public static String SERVERNAME = "Nor";

	static final String CHUNKED = "chunked";
	static final String GZIP = "gzip";
	static final String DEFLATE = "deflate";

	static final String REQUEST_LINE_TEMPLATE = "%s %s HTTP/%s";
		static final String REQUEST_LINE_PATTERN = "^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$";

	static final String RESPONSE_LINE_TEMPLATE = "HTTP/%s %d %s";
	static final String REQPONSE_LINE_PATTERN = "^HTTP/(\\S{3})\\s+(\\d{3})\\s*(.*)$";

}
