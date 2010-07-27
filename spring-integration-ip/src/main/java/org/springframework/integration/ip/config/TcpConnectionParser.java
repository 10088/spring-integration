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

package org.springframework.integration.ip.config;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.xml.IntegrationNamespaceUtils;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author Gary Russell
 * @since 2.0
 *
 */
public class TcpConnectionParser extends AbstractBeanDefinitionParser {

	@Override
	protected AbstractBeanDefinition parseInternal(Element element,
			ParserContext parserContext) {
		BeanDefinitionBuilder builder = null;
		String useNio = IpAdapterParserUtils.getUseNio(element);
		String type = element.getAttribute(IpAdapterParserUtils.TCP_CONNECTION_TYPE);
		if (!StringUtils.hasText(type)) {
			parserContext.getReaderContext().error(IpAdapterParserUtils.TCP_CONNECTION_TYPE + 
					" is required for a tcp connection", element);
		}
		if (type.equals("client")) {
			if (useNio.equals("true")) {
				builder = BeanDefinitionBuilder.genericBeanDefinition(
						TcpNioClientConnectionFactory.class);
				IpAdapterParserUtils.addHostAndPortToConstructor(element, builder, parserContext);				
			} else {
				builder = BeanDefinitionBuilder.genericBeanDefinition(
						TcpNetClientConnectionFactory.class);
				IpAdapterParserUtils.addHostAndPortToConstructor(element, builder, parserContext);	
			}
		} else if (type.equals("server")) {
			if (useNio.equals("true")) {
				builder = BeanDefinitionBuilder.genericBeanDefinition(
						TcpNioServerConnectionFactory.class);
				IpAdapterParserUtils.addPortToConstructor(element, builder, parserContext);
			} else {
				builder = BeanDefinitionBuilder.genericBeanDefinition(
						TcpNetServerConnectionFactory.class);
				IpAdapterParserUtils.addPortToConstructor(element, builder, parserContext);
			}
		} else {
			parserContext.getReaderContext().error(IpAdapterParserUtils.TCP_CONNECTION_TYPE + 
					" must be 'client' or 'server' for an IP channel adapter", element);
		}
		IpAdapterParserUtils.addCommonSocketOptions(builder, element);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,
				IpAdapterParserUtils.RECEIVE_BUFFER_SIZE);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.USING_DIRECT_BUFFERS);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.SO_KEEP_ALIVE);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.SO_LINGER);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.SO_TCP_NODELAY);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.SO_TRAFFIC_CLASS);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,
				IpAdapterParserUtils.POOL_SIZE);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.TASK_EXECUTOR);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.INPUT_CONVERTER);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, 
				IpAdapterParserUtils.OUTPUT_CONVERTER);
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element,
				IpAdapterParserUtils.SINGLE_USE);
	
		return builder.getBeanDefinition();
	}


}
