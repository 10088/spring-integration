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

package org.springframework.integration.xml.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.transform.dom.DOMResult;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.Message;
import org.springframework.integration.core.GenericMessage;
import org.springframework.integration.core.MessageChannel;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.xml.config.StubResultFactory.StubStringResult;
import org.springframework.integration.xml.util.XmlTestUtil;
import org.springframework.xml.transform.StringResult;

/**
 * @author Jonas Partner
 * @author Mark Fisher
 */
public class XsltPayloadTransformerParserTests {

	private String doc = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><order><orderItem>test</orderItem></order>";

	private ApplicationContext applicationContext;

	private PollableChannel output;


	@Before
	public void setUp() {
		applicationContext = new ClassPathXmlApplicationContext(getClass().getSimpleName() + "-context.xml", getClass());
		output = (PollableChannel) applicationContext.getBean("output");
	}


	@Test
	public void testWithResourceProvided() throws Exception {
		MessageChannel input = (MessageChannel) applicationContext.getBean("withResourceIn");
		GenericMessage<Object> message = new GenericMessage<Object>(XmlTestUtil.getDomSourceForString(doc));
		input.send(message);
		Message<?> result = output.receive(0);
		assertTrue("Payload was not a DOMResult", result.getPayload() instanceof DOMResult);
		Document doc = (Document) ((DOMResult) result.getPayload()).getNode();
		assertEquals("Wrong payload", "test", doc.getDocumentElement().getTextContent());
	}

	@Test
	public void testWithTemplatesProvided() throws Exception {
		MessageChannel input = (MessageChannel) applicationContext.getBean("withTemplatesIn");
		GenericMessage<Object> message = new GenericMessage<Object>(XmlTestUtil.getDomSourceForString(doc));
		input.send(message);
		Message<?> result = output.receive(0);
		assertTrue("Payload was not a DOMResult", result.getPayload() instanceof DOMResult);
		Document doc = (Document) ((DOMResult) result.getPayload()).getNode();
		assertEquals("Wrong payload", "test", doc.getDocumentElement().getTextContent());
	}

	@Test
	public void testWithTemplatesAndResultTransformer() throws Exception {
		MessageChannel input = (MessageChannel) applicationContext.getBean("withTemplatesAndResultTransformerIn");
		GenericMessage<Object> message = new GenericMessage<Object>(XmlTestUtil.getDomSourceForString(doc));
		input.send(message);
		Message<?> result = output.receive(0);
		assertEquals("Wrong payload type", String.class, result.getPayload().getClass());
		String strResult = (String)result.getPayload();
		assertEquals("Wrong payload", "testReturn", strResult);
	}

	@Test
	public void testWithResourceProvidedAndStubResultFactory() throws Exception {
		MessageChannel input = (MessageChannel) applicationContext.getBean("withTemplatesAndResultFactoryIn");
		GenericMessage<Object> message = new GenericMessage<Object>(XmlTestUtil.getDomSourceForString(doc));
		input.send(message);
		Message<?> result = output.receive(0);
		assertTrue("Payload was not a StubStringResult", result.getPayload() instanceof StubStringResult);
	}

	@Test
	public void testWithResourceAndStringResultType() throws Exception {
		MessageChannel input = (MessageChannel) applicationContext.getBean("withTemplatesAndStringResultTypeIn");
		GenericMessage<Object> message = new GenericMessage<Object>(XmlTestUtil.getDomSourceForString(doc));
		input.send(message);
		Message<?> result = output.receive(0);
		assertTrue("Payload was not a StringResult", result.getPayload() instanceof StringResult);
	}
	
	
		


	
}
