/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.config.SourcePollingChannelAdapterFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.xml.DomUtils;

/**
 * Base parser for inbound Channel Adapters that poll a source.
 * 
 * @author Mark Fisher
 */
public abstract class AbstractPollingInboundChannelAdapterParser extends AbstractChannelAdapterParser {

	@Override
	protected AbstractBeanDefinition doParse(Element element, ParserContext parserContext, String channelName) {
		String source = this.parseSource(element, parserContext);
		Assert.hasText(source, "failed to parse source");
		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(SourcePollingChannelAdapterFactoryBean.class);
		adapterBuilder.addPropertyReference("source", source);
		adapterBuilder.addPropertyReference("outputChannel", channelName);
		Element pollerElement = DomUtils.getChildElementByTagName(element, "poller");
		if (pollerElement != null) {
			IntegrationNamespaceUtils.configurePollerMetadata(pollerElement, adapterBuilder, parserContext);
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined(adapterBuilder, element, "auto-startup");
		return adapterBuilder.getBeanDefinition();
	}

	/**
	 * Subclasses must implement this method to parse the PollableSource instance
	 * which the created Channel Adapter will poll.
	 */
	protected abstract String parseSource(Element element, ParserContext parserContext);

}
