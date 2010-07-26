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
package nor.network;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import nor.util.log.Logger;

public class SelectionWorker implements Runnable, Closeable{

	private boolean running = true;
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
		Timeout = Integer.valueOf(System.getProperty(String.format("%s.Timeout", classname)));

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

		try{

			while(this.running){

				try{

//					final int nc = this.selector.select(Timeout);
					final int nc = this.selector.selectNow();
					LOGGER.finest("run", "Begin a selection ({0} selected keys, {1} registrated keys)", nc, selector.keys().size());

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

				}catch(final CancelledKeyException e){

					LOGGER.severe("run", e.getMessage());
					LOGGER.catched(Level.FINE, "run", e);

				}

			}

		}catch(final IOException e){

			LOGGER.severe("run", e.getMessage());
			LOGGER.catched(Level.FINE, "run", e);

		}catch(final CancelledKeyException e){

			LOGGER.catched(Level.FINE, "run", e);

		}finally{

			try {

				this.selector.close();

			} catch (IOException e) {

				LOGGER.warning("run", e.getMessage());
				LOGGER.catched(Level.FINE, "run", e);

			}

		}

	}

	@Override
	public void close(){

		this.running = false;

	}

	public SelectionKey register(final SelectableChannel ch, final int ops, final SelectionEventHandler handler) throws IOException{

		ch.configureBlocking(false);
		final SelectionKey key = ch.register(this.selector, ops, handler);

		return key;

	}

}
