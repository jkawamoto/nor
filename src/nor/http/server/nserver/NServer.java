package nor.http.server.nserver;

import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class NServer {

	public static final int Timeout;
	public static final int BufferSize;
	public static final int MinimusThreads;

	private NServer(){

	}


	static{

		final Logger LOGGER = Logger.getLogger(NServer.class);

		final String classname = NServer.class.getName();
		BufferSize = Integer.valueOf(System.getProperty(String.format("%s.BufferSize", classname)));
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));
		MinimusThreads = Integer.valueOf(System.getProperty(String.format("%s.MinimusThreads", classname)));

		LOGGER.config("<class init>", "Load a constant: BufferSize = {0}", BufferSize);
		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: MinimusThreads = {0}", MinimusThreads);

	}

}
