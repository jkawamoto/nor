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
package nor.http.server.nserver;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import nor.network.SelectionEventHandlerAdapter;
import nor.network.SelectionWorker;
import nor.util.log.Logger;

/**
*
* @author Junpei Kawamoto
* @since 0.2
*/
class Delegator implements Closeable{

	private final DelegationHandler h1, h2;

	private static final Logger LOGGER = Logger.getLogger(Delegator.class);

	//============================================================================
	// Construction
	//============================================================================
	public Delegator(final SelectionKey from, final SelectableChannel to, final SelectionWorker selector) throws IOException{

		final ByteBuffer b1 = ByteBuffer.allocate(NServer.BufferSize);
		final ByteBuffer b2 = ByteBuffer.allocate(NServer.BufferSize);

		this.h1 = new DelegationHandler(from, b1, b2);
		this.h2 = new DelegationHandler(to, b2, b1, selector);
		this.h1.setAnother(this.h2);
		this.h2.setAnother(this.h1);

	}

	//============================================================================
	// Public methods
	//============================================================================
	@Override
	public void close(){

		this.h1.close();
		this.h2.close();

	}

	//============================================================================
	// Private inner class
	//============================================================================
	private final class DelegationHandler extends SelectionEventHandlerAdapter implements Closeable{

		private final SelectionKey key;
		private final ByteBuffer read;
		private final ByteBuffer write;

		private DelegationHandler another = null;
		private boolean closing = false;

		//============================================================================
		// Constructor
		//============================================================================
		public DelegationHandler(final SelectableChannel ch, final ByteBuffer read, final ByteBuffer write, final SelectionWorker selector) throws IOException{

			this.key = selector.register(ch, SelectionKey.OP_READ, this);

			this.read = read;
			this.write = write;

		}

		public DelegationHandler(final SelectionKey key, final ByteBuffer read, final ByteBuffer write){

			this.key = key;
			this.key.attach(this);
			this.addOps(SelectionKey.OP_READ);

			this.read = read;
			this.write = write;

		}

		//============================================================================
		// Public methods
		//============================================================================
		public void setAnother(final DelegationHandler another){

			this.another = another;

		}

		@Override
		public void onRead(final ReadableByteChannel ch){

			try {

				LOGGER.finer(this.getClass(), "onRead", "Read from {0} and write to {1}", ch, this.read);
				this.read.clear();
				final int ret = ch.read(this.read);
				if(ret == -1){

					this.removeOps(SelectionKey.OP_READ);
					this.readyToClose();

				}else if(ret > 0){

					this.read.flip();

					this.removeOps(SelectionKey.OP_READ);
					if(this.another != null){

						this.another.addOps(SelectionKey.OP_WRITE);

					}

				}

			} catch (final IOException e) {

				LOGGER.warning(this.getClass(), "onRead", e.getMessage());
				LOGGER.catched(Level.FINE, this.getClass(), "onRead", e);

				this.removeOps(SelectionKey.OP_READ);
				this.readyToClose();

			}

		}

		@Override
		public void onWrite(final WritableByteChannel ch){

			try {

				LOGGER.finer(this.getClass(), "onWrite", "Read from {0} and write to {1}", this.write, ch);
				ch.write(this.write);

				if(this.write.limit() - this.write.position() == 0){

					/*
					 * Finished to send all data included in the buffer.
					 */
					this.removeOps(SelectionKey.OP_WRITE);
					if(this.another != null){

						this.another.addOps(SelectionKey.OP_READ);

					}

				}

			} catch (final IOException e) {

				LOGGER.warning(this.getClass(), "onWrite", e.getMessage());
				LOGGER.catched(Level.FINE, this.getClass(), "onWrite", e);

				this.removeOps(SelectionKey.OP_WRITE);
				this.another.readyToClose();

			}

		}

		@Override
		public void close(){

			LOGGER.fine(this.getClass(), "close", "Close the delegation. (key = {0})", this.key);

			this.key.interestOps(0);
			this.key.cancel();
			this.key.attach(null);

			try{

				this.key.channel().close();

			}catch(final IOException e){

				LOGGER.warning("close", e.getMessage());
				LOGGER.catched(Level.FINE, "close", e);

			}

		}

		//============================================================================
		// Private methods
		//============================================================================
		private void addOps(final int ops){

			if(this.key.isValid()){

				this.key.interestOps(this.key.interestOps() | ops);

			}

		}

		private void removeOps(final int ops){

			if(this.key.isValid()){

				this.key.interestOps(this.key.interestOps() & ~ops);

			}

		}

		private void readyToClose(){

			if(this.another.closing){

				/*
				 * Both handlers are ready to close.
				 */
				this.close();
				this.another.close();

			}else{

				this.closing = true;

			}

		}

	}

}
