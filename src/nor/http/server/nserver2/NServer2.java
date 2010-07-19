package nor.http.server.nserver2;

import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class NServer2 {

	public static final int Timeout;
	public static final int BufferSize;
	public static final int MinimusThreads;

	private NServer2(){

	}


	static{

		final Logger LOGGER = Logger.getLogger(NServer2.class);

		final String classname = NServer2.class.getName();
		BufferSize = Integer.valueOf(System.getProperty(String.format("%s.BufferSize", classname)));
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));
		MinimusThreads = Integer.valueOf(System.getProperty(String.format("%s.MinimusThreads", classname)));

		LOGGER.config("<class init>", "Load a constant: BufferSize = {0}", BufferSize);
		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: MinimusThreads = {0}", MinimusThreads);

	}

}
