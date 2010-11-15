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

package org.springframework.integration.xmpp.inbound;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Packet;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.integration.xmpp.core.AbstractXmppConnectionAwareEndpoint;
import org.springframework.util.Assert;

/**
 * This component logs in as a user and forwards any messages <em>to</em> that
 * user on to downstream components. 
 *
 * @author Josh Long
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @since 2.0
 */
public class ChatMessageListeningEndpoint extends AbstractXmppConnectionAwareEndpoint {

	private final MessagingTemplate messagingTemplate = new MessagingTemplate();

	private volatile boolean extractPayload = true;

	private final PacketListener packetListener = new ChatMessagePublishingPacketListener();


	public ChatMessageListeningEndpoint() {
		super();
	}

	public ChatMessageListeningEndpoint(XMPPConnection xmppConnection) {
		super(xmppConnection);
	}


	/**
	 * @param requestChannel the channel on which the inbound message should be sent
	 */
	public void setRequestChannel(MessageChannel requestChannel) {
		this.messagingTemplate.setDefaultChannel(requestChannel);
	}

	/**
	 * Specify whether the text message body should be extracted when mapping to a
	 * Spring Integration Message payload. Otherwise, the full XMPP Message will be
	 * passed within the payload. This value is <em>true</em> by default.
	 */
	public void setExtractPayload(boolean extractPayload) {
		this.extractPayload = extractPayload;
	}

	@Override
	protected void onInit() throws Exception {
		super.onInit();
		this.messagingTemplate.afterPropertiesSet();
	}

	@Override
	protected void doStart() {
		Assert.isTrue(this.initialized, this.getComponentName() + " [" + this.getComponentType() + "] must be initialized");
		this.xmppConnection.addPacketListener(this.packetListener, null);
	}

	@Override
	protected void doStop() {
		if (this.xmppConnection != null) {
			this.xmppConnection.removePacketListener(this.packetListener);
		}
	}


	private class ChatMessagePublishingPacketListener implements PacketListener {

		public void processPacket(final Packet packet) {
			if (packet instanceof org.jivesoftware.smack.packet.Message) {
				org.jivesoftware.smack.packet.Message xmppMessage = (org.jivesoftware.smack.packet.Message) packet;
				Chat chat = xmppConnection.getChatManager().getThreadChat(xmppMessage.getThread());
				Object payload = (extractPayload ? xmppMessage.getBody() : xmppMessage);
				MessageBuilder<?> messageBuilder = MessageBuilder.withPayload(payload)
						.setHeader(XmppHeaders.TYPE, xmppMessage.getType())
						.setHeader(XmppHeaders.CHAT, chat);
				messagingTemplate.send(messageBuilder.build());
			}
		}
	}

}
