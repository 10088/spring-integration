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

package org.springframework.integration.sftp.config;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.integration.sftp.inbound.SftpInboundSynchronizingMessageSource;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Oleg Zhurakousky
 */
public class InboundChannelAdapaterParserTests {
	
	@Before
	public void prepare(){
		new File("foo").delete();
	}

	@Test
	public void testWithLocalFiles() throws Exception{
		ApplicationContext context =
			new ClassPathXmlApplicationContext("InboundChannelAdapaterParserTests-context.xml", this.getClass());
		assertTrue(new File("src/main/resources").exists());
	
		Object adapter = context.getBean("sftpAdapterAutoCreate");
		assertTrue(adapter instanceof SourcePollingChannelAdapter);
		SftpInboundSynchronizingMessageSource source = 
			(SftpInboundSynchronizingMessageSource) TestUtils.getPropertyValue(adapter, "source");
		assertNotNull(source);
		PollableChannel requestChannel = context.getBean("requestChannel", PollableChannel.class);
		assertNotNull(requestChannel.receive(2000));
	}

	@Test(expected=BeanDefinitionStoreException.class)
	//exactly one of 'filename-pattern' or 'filter' is allowed on SFTP inbound adapter
	public void testLocalFilesAutoCreationFalse() throws Exception{
		assertTrue(!new File("target/bar").exists());
		new ClassPathXmlApplicationContext("InboundChannelAdapaterParserTests-context-fail.xml", this.getClass());
	}

	@Test
	public void testLocalFilesAreFound() throws Exception{
		assertTrue(new File("target").exists());
		new ClassPathXmlApplicationContext("InboundChannelAdapaterParserTests-context.xml", this.getClass());
		assertTrue(new File("target").exists());
	}
	
	@Test
	public void testLocalDirAutoCreated() throws Exception{
		assertFalse(new File("foo").exists());
		new ClassPathXmlApplicationContext("InboundChannelAdapaterParserTests-context.xml", this.getClass());
		assertTrue(new File("foo").exists());
	}
	
	@After
	public void cleanUp() throws Exception{
		new File("foo").delete();
	}

}
