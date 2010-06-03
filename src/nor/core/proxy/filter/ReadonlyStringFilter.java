package nor.core.proxy.filter;

import java.io.Closeable;

public interface ReadonlyStringFilter extends Closeable{

	/**
	 *
	 * @param msg
	 * @param in
	 * @return
	 */
	public void update(final String in);

}
