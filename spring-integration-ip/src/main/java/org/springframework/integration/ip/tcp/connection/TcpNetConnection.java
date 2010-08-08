/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.ip.tcp.connection;

import java.net.Socket;
import java.net.SocketTimeoutException;

import org.springframework.commons.serializer.InputStreamingConverter;
import org.springframework.integration.Message;
import org.springframework.integration.ip.tcp.SocketIoUtils;
import org.springframework.integration.ip.tcp.converter.SoftEndOfStreamException;

/**
 * A TcpConnection that uses and underlying {@link Socket}.
 * 
 * @author Gary Russell
 * @since 2.0
 *
 */
public class TcpNetConnection extends AbstractTcpConnection {

	private final Socket socket; 
	
	/**
	 * Constructs a TcpNetConnection for the socket.
	 * @param socket the socket
	 * @param server if true this connection was created as
	 * a result of an incoming request.
	 */
	public TcpNetConnection(Socket socket, boolean server) {
		super(server);
		this.socket = socket;
		getConnectionId();
	}
	
	/**
	 * Closes this connection.
	 */
	public void close() {
		try {
			this.socket.close();
		} catch (Exception e) {}
		super.close();
	}

	public boolean isOpen() {
		return !this.socket.isClosed();
	}

	@SuppressWarnings("unchecked")
	public void send(Message<?> message) throws Exception {
		Object object = mapper.fromMessage(message);
		this.outputConverter.convert(object, this.socket.getOutputStream());
		if (logger.isDebugEnabled())
			logger.debug("Message sent " + message);
	}

	public String getHostAddress() {
		return this.socket.getInetAddress().getHostAddress();
	}

	public String getHostName() {
		return this.socket.getInetAddress().getHostName();
	}

	public Object getPayload() throws Exception {
		return this.inputConverter.convert(this.socket.getInputStream());
	}

	public int getPort() {
		return this.socket.getPort();
	}
	
	/**
	 * If there is no listener, and this connection is not for single use, 
	 * this method exits. When there is a listener, the method runs in a
	 * loop reading input from the connections's stream, data is converted
	 * to an object using the {@link InputStreamingConverter} and the listener's
	 * {@link TcpListener#onMessage(Message)} method is called. For single use
	 * connections with no listener, the socket is closed after its timeout
	 * expires. If data is received on a single use socket with no listener, 
	 * a warning is logged.
	 */
	public void run() {
		if (this.listener == null && !this.singleUse) {
			logger.debug("TcpListener exiting - no listener and not single use");
			return;
		}
		Message<?> message = null;
		boolean okToRun = true;
		logger.debug("Reading...");
		while (okToRun) {
			try {
				message = this.mapper.toMessage(this);
			} catch (Exception e) {
				this.close();
				if (!(e instanceof SoftEndOfStreamException)) {
					if (e instanceof SocketTimeoutException && this.singleUse) {
						logger.debug("Closing single use socket after timeout");
					} else {
						logger.error("Read exception " +
									 this.getConnectionId() + " " +
									 e.getClass().getSimpleName() + 
								     ":" + e.getCause() + ":" + e.getMessage());
					}
				}
				break;
			}
			if (logger.isDebugEnabled())
				logger.debug("Message received " + message);
			try {
				if (listener == null) {
					logger.warn("Unexpected message - no inbound adapter registered with connection " + message);
					continue;
				}
				listener.onMessage(message);
			} catch (Exception e) {
				if (e instanceof NoListenerException) {
					if (this.singleUse) {
						logger.debug("Closing single use socket after inbound message " + this.connectionId);
						this.close();
						okToRun = false;
					} else {
						logger.warn("Unexpected message - no inbound adapter registered with connection " + message);
					}
				} else {
					logger.error("Exception sending meeeage: " + message, e);				
				}
			}
			/*
			 * For single use sockets, we close after receipt if we are on the client
			 * side, or the server side has no outbound adapter registered
			 */
			if (this.singleUse && this.server && this.sender == null) {
				logger.debug("Closing single use socket after inbound message " + this.connectionId);
				this.close();
				okToRun = false;
			}
		}
	}

	public String getConnectionId() {
		if (this.connectionId == null) {
			this.connectionId = SocketIoUtils.getSocketId(this.socket);
		}
		return this.connectionId;
	}


}
