package nor.network;

import java.io.IOException;
import java.util.Properties;

import nor.util.log.Logger;

final class Network {

	public static final int Timeout;
	public static final int BufferSize;

	private Network(){}


	static{

		final Logger LOGGER = Logger.getLogger(Network.class);

		final String classname = Network.class.getName();
		final Properties defaults = new Properties();
		try {

			defaults.load(Network.class.getResourceAsStream("default.conf"));

		} catch (final IOException e) {

			LOGGER.severe("<class init>", "Cannot load default configs ({0})", e);

		}

		final String timeout = String.format("%s.Timeout", classname);
		Timeout = Integer.valueOf(System.getProperty(timeout, defaults.getProperty(timeout)));

		final String bsize = String.format("%s.BufferSize", classname);
		BufferSize = Integer.valueOf(System.getProperty(bsize, defaults.getProperty(bsize)));

		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);
		LOGGER.config("<class init>", "Load a constant: BufferSize = {0}", BufferSize);

	}

}
