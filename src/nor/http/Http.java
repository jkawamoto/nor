package nor.http;

import java.util.regex.Pattern;

public class Http {

	public static String VERSION = "1.1";
	public static String SERVERNAME = "Nor";

	static final String CHUNKED = "chunked";
	static final String GZIP = "gzip";
	static final String DEFLATE = "deflate";

	static final String REQUEST_LINE_TEMPLATE = "%s %s HTTP/%s";
	static final Pattern REQUEST_LINE_PATTERN = Pattern.compile("^(\\w+)\\s+(.+?)\\s+HTTP/([\\d.]+)$");

	static final String RESONSE_LINE_TEMPLATE = "HTTP/%s %d %s";
	static final Pattern RESONSE_LINE_PATTERN = Pattern.compile("^HTTP/(\\S{3})\\s+(\\d{3})\\s*(.*)$");

}
