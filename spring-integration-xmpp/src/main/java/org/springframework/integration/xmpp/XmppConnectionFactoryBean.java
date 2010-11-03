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
package org.springframework.integration.xmpp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * This class configures an {@link org.jivesoftware.smack.XMPPConnection} object. 
 * This object is used for all scenarios to talk to a Smack server.
 *
 * @author Josh Long
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @see org.jivesoftware.smack.XMPPConnection
 * @since 2.0
 */
public class XmppConnectionFactoryBean extends AbstractFactoryBean<XMPPConnection> implements SmartLifecycle{

	private final ConnectionConfiguration connectionConfiguration;
	
	private volatile String resource = "Smack"; // default value used by Smack
	
	private volatile String user;
		
	private volatile String password;
	
	private volatile String subscriptionMode = "accept_all";
	
	private volatile XMPPConnection connection;
	
	private volatile boolean autoStartup;
	
	private volatile boolean started;

	public XmppConnectionFactoryBean(ConnectionConfiguration connectionConfiguration) {
		Assert.notNull(connectionConfiguration, "'connectionConfiguration' must not be null");
		this.connectionConfiguration = connectionConfiguration;
	}
	
	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public void setSubscriptionMode(String subscriptionMode) {
		this.subscriptionMode = subscriptionMode;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setResource(String resource) {
		this.resource = resource;
	}
	
	@Override
	public Class<? extends XMPPConnection> getObjectType() {
		return XMPPConnection.class;
	}

	@Override
	protected XMPPConnection createInstance() throws Exception {
		connection = new XMPPConnection(connectionConfiguration);
		
		return connection;
	}

	@Override
	public void start() {
		try {
			connection.connect();
			if (StringUtils.hasText(user)){
				connection.login(user, password, resource);
				
				Assert.isTrue(connection.isAuthenticated(), "Failed to authenticate user: " + user);
				
				if (StringUtils.hasText(this.subscriptionMode)) {
					Roster.SubscriptionMode subscriptionMode = Roster.SubscriptionMode.valueOf(this.subscriptionMode);
					connection.getRoster().setSubscriptionMode(subscriptionMode);
				}	
			}
			else {
				connection.loginAnonymously();
			}
			this.started = true;
		} catch (Exception e) {
			throw new BeanInitializationException("Failed to connect to " + this.connectionConfiguration.getHost(), e);
		}
	}

	@Override
	public void stop() {
		if (this.isRunning()){
			this.connection.disconnect();
			this.started = false;
		}
	}

	@Override
	public boolean isRunning() {
		return this.started;
	}

	@Override
	public int getPhase() {
		return Integer.MIN_VALUE;
	}

	@Override
	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	@Override
	public void stop(Runnable callback) {
		callback.run();
	}
}
