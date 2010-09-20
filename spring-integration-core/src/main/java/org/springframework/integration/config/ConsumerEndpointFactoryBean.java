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
package org.springframework.integration.config;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.context.IntegrationContextUtils;
import org.springframework.integration.context.IntegrationObjectSupport;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.endpoint.PollingConsumer;
import org.springframework.integration.scheduling.PollerFactory;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @author Josh Long
 */
public class ConsumerEndpointFactoryBean
		implements FactoryBean<AbstractEndpoint>, BeanFactoryAware, BeanNameAware, BeanClassLoaderAware, InitializingBean, SmartLifecycle {

	private volatile MessageHandler handler;

	private volatile String beanName;

	private volatile String inputChannelName;

	private volatile PollerMetadata pollerMetadata;
	
	private volatile boolean autoStartup = true;

	private volatile MessageChannel inputChannel;

	private volatile ConfigurableBeanFactory beanFactory;
	
	private volatile ClassLoader beanClassLoader;

	private volatile AbstractEndpoint endpoint;

	private volatile boolean initialized;

	private final Object initializationMonitor = new Object();

	private final Object handlerMonitor = new Object();


	public void setHandler(MessageHandler handler) {
		Assert.notNull(handler, "handler must not be null");
		synchronized (this.handlerMonitor) {
			Assert.isNull(this.handler, "handler cannot be overridden");
			this.handler = handler;
		}
	}

	public void setInputChannel(MessageChannel inputChannel) {
		this.inputChannel = inputChannel;
	}

	public void setInputChannelName(String inputChannelName) {
		this.inputChannelName = inputChannelName;
	}

	public void setPollerMetadata(PollerMetadata pollerMetadata) {
		this.pollerMetadata = pollerMetadata;
	}
	
	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	public void setAutoStartup(boolean autoStartup) {
		this.autoStartup = autoStartup;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		Assert.isInstanceOf(ConfigurableBeanFactory.class, beanFactory, "a ConfigurableBeanFactory is required");
		this.beanFactory = (ConfigurableBeanFactory) beanFactory;
	}

	public void afterPropertiesSet() throws Exception {
		if (!this.beanName.startsWith("org.springframework") && this.handler instanceof IntegrationObjectSupport) {
			((IntegrationObjectSupport) this.handler).setComponentName(this.beanName);
		}
		this.initializeEndpoint();
	}

	public boolean isSingleton() {
		return true;
	}

	public AbstractEndpoint getObject() throws Exception {
		if (!this.initialized) {
			this.initializeEndpoint();
		}
		return this.endpoint;
	}

	public Class<?> getObjectType() {
		if (this.endpoint == null) {
			return AbstractEndpoint.class;
		}
		return this.endpoint.getClass();
	}

	private void initializeEndpoint() throws Exception {
		synchronized (this.initializationMonitor) {
			if (this.initialized) {
				return;
			}
			MessageChannel channel = null;
			if (StringUtils.hasText(this.inputChannelName)) {
				Assert.isTrue(this.beanFactory.containsBean(this.inputChannelName), "no such input channel '"
						+ this.inputChannelName + "' for endpoint '" + this.beanName + "'");
				channel = this.beanFactory.getBean(this.inputChannelName, MessageChannel.class);
			}
			if (this.inputChannel != null) {
				channel = this.inputChannel;
			}
			Assert.state(channel != null, "one of inputChannelName or inputChannel is required");
			if (channel instanceof SubscribableChannel) {
				Assert.isNull(this.pollerMetadata, "A poller should not be specified for endpoint '" + this.beanName
						+ "', since '" + channel + "' is a SubscribableChannel (not pollable).");
				this.endpoint = new EventDrivenConsumer((SubscribableChannel) channel, this.handler);
			}
			else if (channel instanceof PollableChannel) {
				PollingConsumer pollingConsumer = new PollingConsumer((PollableChannel) channel, this.handler);
				if (this.pollerMetadata == null) {
					this.pollerMetadata = IntegrationContextUtils.getDefaultPollerMetadata(this.beanFactory);
					Assert.notNull(this.pollerMetadata, "No poller has been defined for endpoint '" + this.beanName
							+ "', and no default poller is available within the context.");
				}
				pollingConsumer.setTrigger(this.pollerMetadata.getTrigger());
				pollingConsumer.setReceiveTimeout(this.pollerMetadata.getReceiveTimeout());
				
				PollerFactory pollerFactory = new PollerFactory(pollerMetadata);
				pollerFactory.setBeanFactory(this.beanFactory);
				pollerFactory.setBeanClassLoader(this.beanClassLoader);
				pollingConsumer.setPollerFactory(pollerFactory);
				this.endpoint = pollingConsumer;
			}
			else {
				throw new IllegalArgumentException("unsupported channel type: [" + channel.getClass() + "]");
			}
			this.endpoint.setBeanName(this.beanName);
			this.endpoint.setBeanFactory(this.beanFactory);
			this.endpoint.setAutoStartup(this.autoStartup);
			this.endpoint.afterPropertiesSet();
			this.initialized = true;
		}
	}


	/*
	 * SmartLifecycle implementation (delegates to the created endpoint)
	 */

	public boolean isAutoStartup() {
		return (this.endpoint != null) ? this.endpoint.isAutoStartup() : true;
	}

	public int getPhase() {
		return (this.endpoint != null) ? this.endpoint.getPhase() : 0;
	}

	public boolean isRunning() {
		return (this.endpoint != null) ? this.endpoint.isRunning() : false;
	}

	public void start() {
		if (this.endpoint != null) {
			this.endpoint.start();
		}
	}

	public void stop() {
		if (this.endpoint != null) {
			this.endpoint.stop();
		}
	}

	public void stop(Runnable callback) {
		if (this.endpoint != null) {
			this.endpoint.stop(callback);
		}
	}
}
