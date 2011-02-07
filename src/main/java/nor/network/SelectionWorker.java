/*
 *  Copyright (C) 2010, 2011 Junpei Kawamoto
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nor.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Properties;
import java.util.logging.Level;

import nor.util.log.Logger;

public class SelectionWorker implements Runnable{

	private final Selector selector;

	private static final Logger LOGGER = Logger.getLogger(SelectionWorker.class);

	//============================================================================
	// Constants
	//============================================================================
	private static final int Timeout;

	//============================================================================
	// Class constructor
	//============================================================================
	static{

		final String classname = SelectionWorker.class.getName();
		final Properties defaults = new Properties();
		try {

			defaults.load(SelectionWorker.class.getResourceAsStream("res/default.conf"));

		} catch (final IOException e) {

			LOGGER.severe("<class init>", "Cannot load default configs ({0})", e);

		}

		final String tout = String.format("%s.Timeout", classname);
		Timeout = Integer.valueOf(System.getProperty(tout, defaults.getProperty(tout)));

		LOGGER.config("<class init>", "Load a constant: Timeout = {0}", Timeout);

	}

	//============================================================================
	// Constructor
	//============================================================================
	public SelectionWorker() throws IOException{

		this.selector = Selector.open();

	}

	//============================================================================
	// Public methods
	//============================================================================
	@Override
	public void run(){

		Thread.currentThread().setName("Selection Thread");
		LOGGER.finer("run", "Starts listening");

		while(!Thread.currentThread().isInterrupted()){

			try{

				final int nc = this.selector.select(Timeout);
				LOGGER.finest("run", "Begin a selection ({0} selected keys, {1} registrated keys)", nc, this.selector.keys().size());
				if(nc == 0){
					this.onIdle();
					continue;
				}

				for(final SelectionKey key : this.selector.selectedKeys()){

					LOGGER.finer("run", "Selected key is {0}", key);
					final SelectionEventHandler handler = (SelectionEventHandler)key.attachment();
					if(key.isValid()){

						if(key.isAcceptable()){

							final ServerSocketChannel ch = (ServerSocketChannel)key.channel();
							handler.onAccept(ch);

						}else if(key.isConnectable()){

							final SocketChannel ch = (SocketChannel)key.channel();
							handler.onConnect(ch);

						}else if(key.isReadable()){

							final ReadableByteChannel ch = (ReadableByteChannel)key.channel();
							handler.onRead(ch);

						}else if(key.isWritable()){

							final WritableByteChannel ch = (WritableByteChannel)key.channel();
							handler.onWrite(ch);

						}

					}

				}

				this.selector.selectedKeys().clear();
				LOGGER.finest("run", "Ends the selection");

			}catch(final ClosedSelectorException e){

				LOGGER.severe("run", e.getMessage());
				LOGGER.catched(Level.FINE, "run", e);

			}catch(final CancelledKeyException e){

				LOGGER.severe("run", e.getMessage());
				LOGGER.catched(Level.FINE, "run", e);

			}catch(final IOException e){

				LOGGER.severe("run", e.getMessage());
				LOGGER.catched(Level.FINE, "run", e);

			}

		}

		try {

			this.selector.close();

		} catch (IOException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}

	}

	/**
	 * Register a channel with interesting operations and a handler.
	 * @param channel Selectable channel to be registerd
	 * @param ops Interesting operations (OR conjuncted)
	 * @param handler A handler object.
	 * @return A selection key representing this registration
	 * @throws IOException
	 */
	public SelectionKey register(final SelectableChannel channel, final int ops, final SelectionEventHandler handler) throws IOException{

		channel.configureBlocking(false);
		final SelectionKey key = channel.register(this.selector, ops, handler);

		return key;

	}

	public PortListener createPortListener(final String host, final int port) throws IOException{

		return createPortListener(new InetSocketAddress(host, port));

	}

	public PortListener createPortListener(final SocketAddress addr) throws IOException{

		return new PortListener(addr, this);

	}

	//============================================================================
	// Protected methods
	//============================================================================
	protected void onIdle(){


	}

}
