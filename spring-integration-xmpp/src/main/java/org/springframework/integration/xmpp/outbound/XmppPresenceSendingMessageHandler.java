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

package org.springframework.integration.xmpp.outbound;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.springframework.integration.Message;
import org.springframework.integration.xmpp.AbstractXmppConnectionAwareMessageHandler;
import org.springframework.util.Assert;

/**
 * MessageHandler that publishes updated Presence values for a given connection. 
 *
 * @author Josh Long
 * @author Oleg Zhurakousky
 * @since 2.0
 */
public class XmppPresenceSendingMessageHandler extends AbstractXmppConnectionAwareMessageHandler  {

	public XmppPresenceSendingMessageHandler() {
		super();
	}

	public XmppPresenceSendingMessageHandler(XMPPConnection xmppConnection) {
		super(xmppConnection);
	}


	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		Assert.isTrue(this.initialized, this.getComponentName() + " must be initialized");
		Object payload = message.getPayload();
		Assert.isInstanceOf(Presence.class, payload, "'payload' must be of type 'org.jivesoftware.smack.packet.Presence', was: " 
					+ payload.getClass().getName());
		this.xmppConnection.sendPacket((Presence)payload);
	}

}
