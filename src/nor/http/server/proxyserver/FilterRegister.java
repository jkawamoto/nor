package nor.http.server.proxyserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nor.http.Header;
import nor.http.Request;
import nor.http.Response;
import nor.http.Body2.IOStreams;

public class FilterRegister {

	private final Response _response;

	private final List<TransferListener> _postFilters = new ArrayList<TransferListener>();

	FilterRegister(final Response response){

		this._response = response;

	}


	public void addPreTransferListener(final TransferListener listener){


		try {

			final IOStreams s = _response.getBody().getIOStreams();
			final Thread th = new Thread(new Runnable(){

				@Override
				public void run() {


					listener.update(s);

					s.close();


				}

			});
			th.start();

		} catch (IOException e) {

			// TODO 自動生成された catch ブロック
			e.printStackTrace();

		}



	}

	public void addPostTransferListener(final TransferListener listener){

		this._postFilters.add(listener);

	}

	public Request getRequest(){

		return this._response.getRequest();

	}

	public Header getResponseHeader(){

		return this._response.getHeader();

	}

}
