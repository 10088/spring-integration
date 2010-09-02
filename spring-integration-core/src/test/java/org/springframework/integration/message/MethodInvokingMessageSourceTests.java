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

package org.springframework.integration.message;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.endpoint.MethodInvokingMessageSource;

/**
 * @author Mark Fisher
 */
public class MethodInvokingMessageSourceTests {

	@Test
	public void testValidMethod() {
		MethodInvokingMessageSource source = new MethodInvokingMessageSource();
		source.setObject(new TestBean());
		source.setMethodName("validMethod");
		Message<?> result = source.receive();
		assertNotNull(result);
		assertNotNull(result.getPayload());
		assertEquals("valid", result.getPayload());
	}

	@Test(expected=MessagingException.class)
	public void testNoMatchingMethodName() {
		MethodInvokingMessageSource source = new MethodInvokingMessageSource();
		source.setObject(new TestBean());
		source.setMethodName("noMatchingMethod");
		source.receive();
	}

	@Test(expected=MessagingException.class)
	public void testInvalidMethodWithArg() {
		MethodInvokingMessageSource source = new MethodInvokingMessageSource();
		source.setObject(new TestBean());
		source.setMethodName("invalidMethodWithArg");
		source.receive();
	}

	@Test(expected=MessagingException.class)
	public void testInvalidMethodWithNoReturnValue() {
		MethodInvokingMessageSource source = new MethodInvokingMessageSource();
		source.setObject(new TestBean());
		source.setMethodName("invalidMethodWithNoReturnValue");
		source.receive();
	}


	@SuppressWarnings("unused")
	private static class TestBean {

		public String validMethod() {
			return "valid";
		}

		public String invalidMethodWithArg(String arg) {
			return "invalid";
		}

		public void invalidMethodWithNoReturnValue() {
		}
	}

}
