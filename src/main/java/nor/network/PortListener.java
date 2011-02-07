/*
 *  Copyright (C) 2011 Junpei Kawamoto
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

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import nor.util.log.Logger;

/**
* Port listener.
* This object is used from at least two threads; selection thread and handling connection threads.
*
* @author Junpei Kawamoto
* @since 0.2
*/
public class PortListener implements Closeable{

	private final SelectionKey key;

	private final List<AcceptEventHandler> handlers = new ArrayList<AcceptEventHandler>();

	private static final Logger LOGGER = Logger.getLogger(PortListener.class);

	//============================================================================
	//  Constructor
	//============================================================================
	/**
	 * Create a port listener.
	 * @param addr The address listened to
	 * @param manager
	 * @param selector The selector object to which this listener will be registerd.
	 * @throws IOException
	 */
	PortListener(final SocketAddress addr, /*final ConnectionManager manager,*/ final SelectionWorker selector) throws IOException{

		// Create a server socket channel
		final ServerSocketChannel channel = ServerSocketChannel.open();
		channel.configureBlocking(false);

		// Bind the address to the channel
		channel.socket().setReuseAddress(true);
		channel.socket().bind(addr);

		// Register this worker to the select worker
		this.key = selector.register(channel, SelectionKey.OP_ACCEPT, new SelectionEventHandlerAdapter(){

			// This method will be called from selection thread.
			@Override
			public void onAccept(final ServerSocketChannel ch){

				try{

					final SocketChannel socketChannel = ch.accept();
					if(socketChannel != null){

						LOGGER.finest("onAccept", "Receive an accsptable key from {0}", socketChannel.socket());

						// Notify
						final Connection con = new Connection(socketChannel, selector);
						for(final AcceptEventHandler h : PortListener.this.handlers){

							h.onAccept(con);

						}

					}else{

						LOGGER.finest("onAccept", "Receive an accsptable but null key");

					}

				}catch(final IOException e){

					LOGGER.warning(this.getClass(), "onAccept", e.getMessage());
					LOGGER.catched(Level.FINE, "onAccept", e);

				}

			}

		});

		LOGGER.info("<init>", "Bind socket to {0}", addr);

	}

	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public void close() throws IOException{

		final SelectableChannel ch = this.key.channel();
		this.key.cancel();
		this.key.attach(null);
		this.key.selector().wakeup();

		ch.close();

	}

	public void addHandler(final AcceptEventHandler h){

		this.handlers.add(h);

	}

	public void removeHandler(final AcceptEventHandler h){

		this.handlers.remove(h);

	}

	@Override
	public String toString(){

		return String.format("%s(key = %s)", this.getClass().getSimpleName(), this.key);

	}

	//============================================================================
	//  Public interface
	//============================================================================
	public interface AcceptEventHandler{

		public void onAccept(final Connection con);

	}

}
