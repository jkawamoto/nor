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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import nor.util.log.Logger;

class SelectionWorker implements Runnable, Closeable{

	private final Selector selector;

	private boolean running = true;

	private static final int Timeout = 60000;

	private static final Logger LOGGER = Logger.getLogger(SelectionWorker.class);



	public SelectionWorker() throws IOException{

		this.selector = Selector.open();

	}

	@Override
	public void run(){

		Thread.currentThread().setName("Selection Thread");
		LOGGER.finer("run", "Starts listening");

		try{

			while(this.running){

				final int nc = this.selector.select(Timeout);
				LOGGER.finest("run", "Begin a selection ({0} selected keys, {1} registrated keys)", nc, selector.keys().size());

				for(final SelectionKey key : this.selector.selectedKeys()){

					final SelectionEventHandler handler = (SelectionEventHandler)key.attachment();
					if(key.isValid()){

						if(key.isAcceptable()){

							handler.onAccept(key.channel());

						}else if(key.isConnectable()){

							handler.onConnect(key.channel());

						}else{

							if(key.isReadable()){

								handler.onRead(key.channel());

							}
							if(key.isWritable()){

								handler.onWrite(key.channel());

							}

						}

					}

				}

				this.selector.selectedKeys().clear();
				LOGGER.finest("run", "Ends the selection");

			}

		}catch(final IOException e){

			e.printStackTrace();

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
