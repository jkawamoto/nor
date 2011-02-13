package nor.core.plugin;

import java.io.File;
import java.io.IOException;

import nor.core.proxy.filter.MessageHandler;
import nor.core.proxy.filter.RequestFilter;
import nor.core.proxy.filter.ResponseFilter;

public abstract class PluginAdapter implements Plugin{

	/* (非 Javadoc)
	 * @see nor.core.plugin.Plugin#init(java.io.File, java.io.File)
	 */
	@Override
	public void init(final File common, final File local) throws IOException{

	}

	/* (非 Javadoc)
	 * @see nor.core.plugin.Plugin#close()
	 */
	@Override
	public void close() throws IOException{

	}

	/* (非 Javadoc)
	 * @see nor.core.plugin.Plugin#messageHandlers()
	 */
	@Override
	public MessageHandler[] messageHandlers(){

		return null;

	}

	/* (非 Javadoc)
	 * @see nor.core.plugin.Plugin#requestFilters()
	 */
	@Override
	public RequestFilter[] requestFilters(){

		return null;

	}

	/* (非 Javadoc)
	 * @see nor.core.plugin.Plugin#responseFilters()
	 */
	@Override
	public ResponseFilter[] responseFilters(){

		return null;

	}

}
