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

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * A client connection factory that creates {@link TcpNioConnection}s.
 * @author Gary Russell
 * @since 2.0
 *
 */
public class TcpNioClientConnectionFactory extends
		AbstractClientConnectionFactory {

	protected boolean usingDirectBuffers;
	
	private Selector selector;
	
	protected Map<SocketChannel, TcpNioConnection> connections = new ConcurrentHashMap<SocketChannel, TcpNioConnection>();
	
	protected BlockingQueue<SocketChannel> newChannels = new LinkedBlockingQueue<SocketChannel>();

	
	/**
	 * Creates a TcpNioClientConnectionFactory for connections to the host and port.
	 * @param host the host
	 * @param port the port
	 */
	public TcpNioClientConnectionFactory(String host, int port) {
		super(host, port);
	}

	/**
	 * Obtains a connection - if {@link #setSingleUse(boolean)} was called with
	 * true, a new connection is returned; otherwise a single connection is
	 * reused for all requests while the connection remains open.
	 */
	public TcpConnection getConnection() throws Exception {
		int n = 0;
		while (this.selector == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			if (n++ > 600) {
				throw new Exception("Factory failed to start");
			}
		}
		if (this.theConnection != null && this.theConnection.isOpen()) {
			return this.theConnection;
		}
		logger.debug("Opening new socket channel connection to " + this.host + ":" + this.port);
		SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(this.host, this.port));
		setSocketAttributes(socketChannel.socket());
		TcpNioConnection connection = new TcpNioConnection(socketChannel, false);
		connection.setUsingDirectBuffers(this.usingDirectBuffers);
		if (this.taskExecutor == null) {
			connection.setTaskExecutor(Executors.newSingleThreadExecutor());
		} else {
			connection.setTaskExecutor(this.taskExecutor);
		}
		TcpConnection wrappedConnection = wrapConnection(connection);
		initializeConnection(wrappedConnection, socketChannel.socket());
		socketChannel.configureBlocking(false);
		if (this.soTimeout > 0) {
			connection.setLastRead(System.currentTimeMillis());
		}
		this.connections.put(socketChannel, connection);
		newChannels.add(socketChannel);
		selector.wakeup();
		if (!this.singleUse) {
			this.theConnection = wrappedConnection;
		}
		return wrappedConnection;
	}

	/**
	 * When set to true, connections created by this factory attempt
	 * to use direct buffers where possible.
	 * @param usingDirectBuffers
	 * @see ByteBuffer
	 */
	public void setUsingDirectBuffers(boolean usingDirectBuffers) {
		this.usingDirectBuffers = usingDirectBuffers;
	}

	public void close() {
	}

	public void run() {
		logger.debug("Read selector running for connections to " + host + ":" + port);
		try {
			this.selector = Selector.open();
			while (this.active) {
				int selectionCount = selector.select(this.soTimeout);
				SocketChannel newChannel;
				while ((newChannel = newChannels.poll()) != null) {
					newChannel.register(this.selector, SelectionKey.OP_READ, connections.get(newChannel));
				}
				if (logger.isTraceEnabled())
					logger.trace("Connection " + host + ":" + port + " SelectionCount: " + selectionCount);
				long now = 0;
				if (this.soTimeout > 0) {
					Iterator<SocketChannel> it = connections.keySet().iterator();
					now = System.currentTimeMillis();
					while (it.hasNext()) {
						SocketChannel channel = it.next();
						if (!channel.isOpen()) {
							logger.debug("Removing closed channel");
							it.remove();
						} else {
							TcpNioConnection connection = this.connections.get(channel);
							if (now - connection.getLastRead() > this.soTimeout) {
								logger.warn("Timing out TcpNioConnection " + connection.getConnectionId());
								connection.timeout();
							}
						}
					}
				}
				if (selectionCount > 0) {
					Set<SelectionKey> keys = selector.selectedKeys();
					Iterator<SelectionKey> iterator = keys.iterator();
					while (iterator.hasNext()) {
						final SelectionKey key = iterator.next();
						iterator.remove();
						if (!key.isValid()) {
							logger.debug("Selection key no longer valid");
						}
						else if (key.isReadable()) {
							key.interestOps(key.interestOps() - key.readyOps());
							final TcpNioConnection connection; 
							connection = (TcpNioConnection) key.attachment();
							connection.setLastRead(System.currentTimeMillis());
							this.taskExecutor.execute(new Runnable() {
								public void run() {
									try {
										connection.readPacket();
									} catch (Exception e) {
										if (connection.isOpen()) {
											logger.error("Exception on read " +
													connection.getConnectionId() + " " +
													e.getMessage());
											connection.close();
										} else {
											logger.debug("Connection closed");
										}
									}
									if (key.channel().isOpen()) {
										key.interestOps(SelectionKey.OP_READ);
										selector.wakeup();
									}
								}});
						}
						else {
							logger.error("Unexpected key: " + key);
						}
					}
				}			
			}
		} catch (Exception e) {
			logger.error("Exception in reader thread", e);
		}
		logger.debug("Read selector exiting for connections to " + host + ":" + port);
	}

	public boolean isRunning() {
		return this.active;		
	}

}
