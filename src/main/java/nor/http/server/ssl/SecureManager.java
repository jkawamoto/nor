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
package nor.http.server.ssl;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;

public class SecureManager {
	// ロガー
	private static final Logger LOGGER = Logger.getLogger(SecureManager.class.getName());


	private final SSLContext context;

	public SecureManager(){

		SSLContext context = null;
		try{

			final KeyStore ks = KeyStore.getInstance("JKS");

			FileInputStream file = null;

			try {

				file = new java.io.FileInputStream("./ssl/arthra_keystore");
				ks.load(file, "dayomon2".toCharArray());

				final KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks , "dayomon2".toCharArray());

				context = SSLContext.getInstance ("TLS");
				context.init( kmf.getKeyManagers(), null, null);


			} catch (NoSuchAlgorithmException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			} catch (KeyManagementException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}finally{

				if(file != null){

					file.close();

				}

			}

		} catch (KeyStoreException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}catch (IOException e){

		}finally{

			this.context = context;

		}

	}

	public SecureSession createSession(final InputStream in, final OutputStream out) throws IOException{

		final SSLEngine engine = context.createSSLEngine();
		return new SecureSession(engine, in, out);

	}

	public class SecureSession{

		private final SSLEngine _engine;
		private final InputStream _netIn;
		private final OutputStream _netOut;

		private final InputStream _appIn;
		private final OutputStream _appOut;

		private static final int BUFFER_SIZE = 1024*64;

		private SecureSession(final SSLEngine engine, final InputStream netIn, final OutputStream netOut) throws IOException{

			this._engine = engine;
			this._engine.setEnableSessionCreation(true);
			this._engine.setUseClientMode(false);
			this._engine.setNeedClientAuth(false);
			this._engine.setWantClientAuth(false);

			this._netIn = netIn;
			this._netOut = netOut;

			this.handShake();

			this._appIn = new SecureInputStream();
			this._appOut = new SecureOutputStream();

		}

		public InputStream getInputStream(){

			return this._appIn;

		}

		public OutputStream getOutputStream(){

			return this._appOut;

		}

		private void closing(){

			try{

				int bufferSize = BUFFER_SIZE;
				final ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
				final SSLEngineResult result = _engine.wrap(ByteBuffer.wrap(new byte[0]), buffer);

				if(result.bytesProduced() != 0){

					_netOut.write(buffer.array(), 0, result.bytesProduced());

				}

			}catch(IOException e){

				LOGGER.warning("たぶん閉じてた");

			}



		}

		private void handShake() throws IOException{

			final byte[] netData = new byte[BUFFER_SIZE];
			while(true){

				// クライアントからのデータ取得
				final int netDataSize = this._netIn.read(netData);
				final ByteBuffer netDataBuffer = ByteBuffer.wrap(netData, 0, netDataSize);
				final ByteBuffer appDataBuffer = ByteBuffer.allocate(BUFFER_SIZE);

				UNWRAP : while(true){

					// アプリケーションデータへの変換
					final SSLEngineResult result = this._engine.unwrap(netDataBuffer, appDataBuffer);
					//System.out.println(">> " + result);

					switch (result.getStatus()) {
					case BUFFER_OVERFLOW:
						System.out.println("overflow");
						break;
					case BUFFER_UNDERFLOW:
						System.out.println("underflow");
						break;
					}

					switch(result.getHandshakeStatus()){
					case NEED_TASK:
						// 移譲タスクの実行
						this.doDelegatedTask();
						break;

					case NEED_WRAP:
						// データ送信が必要
						break UNWRAP;

					case NOT_HANDSHAKING:
						// ハンドシェーク中ではない
						return;

					case FINISHED:
						// ハンドシェークの終了
						return;

					}

				}

				netDataBuffer.clear();
				WRAP : while(true){

					// ネットデータへの変換
					final SSLEngineResult result = this._engine.wrap(ByteBuffer.wrap(new byte[0]), netDataBuffer);
					//System.out.println("<< " + result);

					switch (result.getStatus()) {
					case BUFFER_OVERFLOW:
						System.out.println("overflow");
						break;
					case BUFFER_UNDERFLOW:
						System.out.println("underflow");
						break;
					}

					// クライアントへデータの送信
					if(result.bytesProduced() != 0){

						this._netOut.write(netDataBuffer.array(), 0, result.bytesProduced());
						this._netOut.flush();

						netDataBuffer.clear();

					}

					switch(result.getHandshakeStatus()){
					case NEED_TASK:
						// 移譲タスクの実行
						this.doDelegatedTask();
						break;

					case NEED_UNWRAP:
						//データの取得が必要
						break WRAP;

					case NOT_HANDSHAKING:
						// ハンドシェーク中ではない
						return;

					case FINISHED:
						// ハンドシェークの終了
						return;

					}

				}

			}

		}

		private void doDelegatedTask(){

			for(Runnable task; (task = this._engine.getDelegatedTask()) != null;){

				task.run();

			}

		}

		private class SecureInputStream extends InputStream{

			private ByteBuffer _inBuffer = ByteBuffer.allocate(_engine.getSession().getApplicationBufferSize());
			private boolean _isClosed = false;

			public SecureInputStream() throws IOException{

				this.readNetData();

			}

			@Override
			public int read() throws IOException {
				LOGGER.entering(SecureInputStream.class.getName(), "read");

				int ret = 0;
				if(_inBuffer.remaining() == 0){

					if(this._isClosed){

						ret = -1;

					}else{

						this.readNetData();
						ret = this.read();

					}

				}else{

					ret = (int)_inBuffer.get();

				}

				LOGGER.exiting(SecureInputStream.class.getName(), "read", ret);
				return ret;

			}

			@Override
			public void close() throws IOException {
				LOGGER.entering(SecureInputStream.class.getName(), "close");

				// 終了ハンドシェーク処理の追加
				if(!_engine.isInboundDone()){

					//_engine.closeInbound();
					//closing();

				}

				//_netIn.close();

				LOGGER.exiting(SecureInputStream.class.getName(), "close");

			}

			private void readNetData() throws IOException{

				final byte[] netData = new byte[_engine.getSession().getPacketBufferSize()];
				final int netDataSize = _netIn.read(netData);
				final ByteBuffer netDataBuffer = ByteBuffer.wrap(netData, 0, netDataSize);

				this._inBuffer.clear();

				//System.out.println("read net data");
				//System.out.println(netDataSize);

				int totalReadSize = 0;
				UNWRAP : while(true){

					// アプリケーションデータへの変換
					final SSLEngineResult result = _engine.unwrap(netDataBuffer, this._inBuffer);

					//System.out.println(result);
					switch (result.getStatus()) {
					case BUFFER_OVERFLOW:
						System.out.println("overflow");
						this._inBuffer = ByteBuffer.allocate(_engine.getSession().getApplicationBufferSize());
						break;

					case BUFFER_UNDERFLOW:
						System.out.println("underflow");
						break;

					case OK:
						//System.out.println("ok");
						//System.out.println(result.bytesConsumed());

						totalReadSize += result.bytesConsumed();
						//System.out.println("total");
						//System.out.println(totalReadSize);
						//System.out.println(this._inBuffer);
						if(netDataSize == totalReadSize){

							break UNWRAP;
						}

						break;

					case CLOSED:

						//System.out.println("close");
						//System.out.println(result.bytesConsumed());
						closing();

						if(_engine.isInboundDone()){

							this._isClosed = true;

						}

						break UNWRAP;

					}

				}

				this._inBuffer.limit(this._inBuffer.position());
				this._inBuffer.position(0);

			}

		}

		private class SecureOutputStream extends ByteArrayOutputStream{

			@Override
			public void flush() throws IOException {
				LOGGER.entering(SecureOutputStream.class.getName(), "flush");

				final byte[] rawAppData = this.toByteArray();
				final ByteBuffer appData = ByteBuffer.wrap(rawAppData);
				int netDataSize = BUFFER_SIZE;

				int totalDataSize = 0;
				WRAP : while(true){

					final ByteBuffer netData = ByteBuffer.allocate(netDataSize);
					final SSLEngineResult result = _engine.wrap(appData, netData);
					//System.out.println("<< " + result);

					switch (result.getStatus()) {
					case BUFFER_OVERFLOW:

						//System.out.println("overflow");
						netDataSize *= 2;
						break;

					case BUFFER_UNDERFLOW:

						System.out.println("underflow : ＜起こりえない＞");
						assert(false);
						break;

					case OK:
						//System.out.println("ok");
						//System.out.println(result);
						//System.out.println(rawAppData.length);

						// クライアントへデータの送信
						if(result.bytesProduced() != 0){

							_netOut.write(netData.array(), 0, result.bytesProduced());
							_netOut.flush();

						}

						totalDataSize += result.bytesConsumed();
						if(totalDataSize == rawAppData.length){

							break WRAP;

						}
					}

				}

				this.reset();


				LOGGER.exiting(SecureOutputStream.class.getName(), "flush");

			}

			@Override
			public void close() throws IOException {
				LOGGER.entering(SecureOutputStream.class.getName(), "close");

				// 終了ハンドシェーク
				if(!_engine.isInboundDone()){

					//_engine.closeOutbound();
					//closing();

				}

				//_netOut.close();

				LOGGER.exiting(SecureOutputStream.class.getName(), "close");

			}



		}

	}

}

