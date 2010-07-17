/*
 *  Copyright (C) 2010 Junpei Kawamoto
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
package nor.http.server.nserver2;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import nor.util.log.Logger;

class PortListener implements Closeable{

	private final ThreadManager manager;
	private final SelectionWorker selector;
	private final SelectionEventHandler handler;
	private final SelectionKey key;

	private static final Logger LOGGER = Logger.getLogger(PortListener.class);

	//============================================================================
	//  Constructor
	//============================================================================
	public PortListener(final String host, final int port, final ThreadManager manager, final SelectionWorker selector) throws IOException{

		this.manager = manager;
		this.selector = selector;
		this.handler = new ListenWorkerHandler();

		// Create a server socket channel
		final ServerSocketChannel channel = ServerSocketChannel.open();

		// Bind the address to the channel
		final InetSocketAddress addr = new InetSocketAddress(host, port);
		channel.socket().bind(addr);

		// Register this worker to the select worker
		this.key = this.selector.register(channel, SelectionKey.OP_ACCEPT, this.handler);

		LOGGER.info("<init>", "Bind socket to {0}", addr);

	}

	//============================================================================
	//  Public methods
	//============================================================================
	@Override
	public void close() throws IOException {

		this.key.cancel();
		this.key.attach(null);

	}

	//============================================================================
	//  Private inner class
	//============================================================================
	private final class ListenWorkerHandler extends SelectionEventHandlerAdapter{

		@Override
		public void onAccept(final SelectableChannel ch){

			try{

				final ServerSocketChannel serverChannel = (ServerSocketChannel)ch;
				final SocketChannel socketChannel = serverChannel.accept();
				if(socketChannel != null){

					LOGGER.finest("onAccept", "Receive an accsptable key from {0}", socketChannel.socket());

					/*
					 * Create a new connection, register it to the selector, and add it to the connection queue.
					 */
					final Connection con = new Connection(socketChannel, PortListener.this.selector);
					PortListener.this.manager.offer(con);

				}else{

					LOGGER.finest("Receive an accsptable but null key");

				}

			}catch(final IOException e){

				LOGGER.warning(this.getClass(), "onAccept", e.getMessage());
				LOGGER.catched(Level.FINE, "onAccept", e);

			}

		}

	}

}
