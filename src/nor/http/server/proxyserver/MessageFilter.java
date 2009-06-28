package nor.http.server.proxyserver;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import nor.http.Header;
import nor.http.Message;
import nor.http.Body2.IOStreams;
import nor.util.observer.Observer;

interface MessageFilter<InfoType> extends Observer<InfoType>{

	/**
	 * 新たなHTTPリクエストが送信される前に呼ばれる．
	 *
	 * @param request 送信されようとしているHTTPリクエスト
	 */
	public void update(final InfoType info);

	abstract class Info {

		private final Message _msg;

		private static final ExecutorService _executors = Executors.newCachedThreadPool();

		/**
		 * ロガー
		 */
		private static final Logger LOGGER = Logger.getLogger(Info.class.getName());

		Info(final Message msg){

			this._msg = msg;

		}

		public void addPreTransferListener(final TransferListener listener){
			assert listener != null;

			try {

				final IOStreams s = _msg.getBody().getIOStreams();
				_executors.equals(new Runnable(){

					@Override
					public void run() {

						listener.update(s.in, s.out);
						try {

							s.close();

						} catch (IOException e) {

							LOGGER.warning("Cannot close " + this + " caused by " + e.getLocalizedMessage());

						}

					}

				});

			} catch (IOException e) {

				LOGGER.severe("Cannot get IOStreams caused by " + e.getLocalizedMessage());

			}

		}

		public String getHeadline(){

			return this._msg.getHeadLine();

		}

		public Header getHeader(){

			return this._msg.getHeader();

		}

	}

}
