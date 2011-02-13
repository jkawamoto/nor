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
import java.util.logging.Level;

import nor.util.log.Logger;

public class SelectionWorker implements Runnable{

	private final Selector selector;

	private static final Logger LOGGER = Logger.getLogger(SelectionWorker.class);

	//============================================================================
	// Constructor
	//============================================================================
	public SelectionWorker() throws IOException{
		LOGGER.entering("<init>");

		this.selector = Selector.open();

		LOGGER.exiting("<init>");
	}

	//============================================================================
	// Public methods
	//============================================================================
	@Override
	public void run(){
		LOGGER.entering("run");

		Thread.currentThread().setName("Selection Thread");
		while(!Thread.currentThread().isInterrupted()){

			try{

				final int nc = this.selector.select(Network.Timeout);
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

		} catch (final IOException e) {

			LOGGER.catched(Level.WARNING, "run", e);

		}

		LOGGER.exiting("run");
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
		LOGGER.entering("register", channel, ops, handler);
		assert channel != null;
		assert ops >= 0;
		assert handler != null;

		channel.configureBlocking(false);
		final SelectionKey key = channel.register(this.selector, ops, handler);

		LOGGER.exiting("register", key);
		return key;
	}

	public PortListener createPortListener(final String host, final int port) throws IOException{

		return createPortListener(new InetSocketAddress(host, port));

	}

	public PortListener createPortListener(final SocketAddress addr) throws IOException{
		LOGGER.entering("createPortListener", addr);
		assert addr != null;

		final PortListener res = new PortListener(addr, this);

		LOGGER.exiting("createPortListener", res);
		return res;
	}

	//============================================================================
	// Protected methods
	//============================================================================
	protected void onIdle(){


	}

}
