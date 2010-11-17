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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.ip.tcp.TcpInboundGateway;
import org.springframework.integration.ip.tcp.TcpOutboundGateway;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioClientConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory;
import org.springframework.integration.ip.udp.DatagramPacketMessageMapper;
import org.springframework.integration.ip.udp.MulticastReceivingChannelAdapter;
import org.springframework.integration.ip.udp.MulticastSendingMessageHandler;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.integration.ip.udp.UnicastSendingMessageHandler;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Gary Russell
 * @since 2.0
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ParserUnitTests {

	@Autowired
	ApplicationContext ctx;
	
	@Autowired
	@Qualifier(value="testInUdp")
	UnicastReceivingChannelAdapter udpIn;

	@Autowired
	@Qualifier(value="testInUdpMulticast")
	MulticastReceivingChannelAdapter udpInMulticast;

	@Autowired
	@Qualifier(value="testInTcp")
	TcpReceivingChannelAdapter tcpIn;
	
	@Autowired
	@Qualifier(value="org.springframework.integration.ip.udp.UnicastSendingMessageHandler#0")
	UnicastSendingMessageHandler udpOut;

	@Autowired
	@Qualifier(value="org.springframework.integration.ip.udp.MulticastSendingMessageHandler#0")
	MulticastSendingMessageHandler udpOutMulticast;

	@Autowired
	@Qualifier(value="org.springframework.integration.ip.tcp.TcpSendingMessageHandler#0")
	TcpSendingMessageHandler tcpOut;

	@Autowired
	@Qualifier(value="inGateway1")
	TcpInboundGateway tcpInboundGateway1;

	@Autowired
	@Qualifier(value="inGateway2")
	TcpInboundGateway tcpInboundGateway2;

	@Autowired
	@Qualifier(value="org.springframework.integration.ip.tcp.TcpOutboundGateway#0")
	TcpOutboundGateway tcpOutboundGateway;
	
	@Autowired
	@Qualifier(value="externalTE")
	TaskExecutor taskExecutor;
	
	@Autowired
	@Qualifier(value="client1")
	AbstractConnectionFactory client1;

	@Autowired
	@Qualifier(value="client2")
	AbstractConnectionFactory client2;

	@Autowired
	@Qualifier(value="cfC1")
	AbstractConnectionFactory cfC1;

	@Autowired
	@Qualifier(value="cfC2")
	AbstractConnectionFactory cfC2;

	@Autowired
	Serializer<?> serializer;

	@Autowired
	Deserializer<?> deserializer;

	@Autowired
	@Qualifier(value="server1")
	AbstractConnectionFactory server1;

	@Autowired
	@Qualifier(value="server2")
	AbstractConnectionFactory server2;

	@Autowired
	@Qualifier(value="cfS1")
	AbstractConnectionFactory cfS1;

	@Autowired
	@Qualifier(value="cfS2")
	AbstractConnectionFactory cfS2;

	@Autowired
	@Qualifier(value="cfS3")
	AbstractConnectionFactory cfS3;

	@Autowired
	@Qualifier(value="org.springframework.integration.ip.tcp.TcpSendingMessageHandler#1")
	TcpSendingMessageHandler tcpNewOut1;

	@Autowired
	@Qualifier(value="org.springframework.integration.ip.tcp.TcpSendingMessageHandler#2")
	TcpSendingMessageHandler tcpNewOut2;

	@Autowired
	@Qualifier(value="tcpNewIn1")
	TcpReceivingChannelAdapter tcpNewIn1;

	@Autowired
	@Qualifier(value="tcpNewIn2")
	TcpReceivingChannelAdapter tcpNewIn2;

	@Autowired
	@Qualifier("errorChannel")
	private MessageChannel errorChannel;

	@Test
	public void testInUdp() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(udpIn);
		assertTrue(udpIn.getPort() >= 5000);
		assertEquals(27, dfa.getPropertyValue("poolSize"));
		assertEquals(29, dfa.getPropertyValue("receiveBufferSize"));
		assertEquals(30, dfa.getPropertyValue("soReceiveBufferSize"));
		assertEquals(31, dfa.getPropertyValue("soSendBufferSize"));
		assertEquals(32, dfa.getPropertyValue("soTimeout"));
		assertEquals("testInUdp",udpIn.getComponentName());
		assertEquals("ip:udp-inbound-channel-adapter", udpIn.getComponentType());
		assertEquals("127.0.0.1", dfa.getPropertyValue("localAddress"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals(errorChannel, dfa.getPropertyValue("errorChannel"));
	}
	
	@Test
	public void testInUdpMulticast() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(udpInMulticast);
		assertTrue(udpInMulticast.getPort() >= 5100);
		assertEquals("225.6.7.8", dfa.getPropertyValue("group"));
		assertEquals(27, dfa.getPropertyValue("poolSize"));
		assertEquals(29, dfa.getPropertyValue("receiveBufferSize"));
		assertEquals(30, dfa.getPropertyValue("soReceiveBufferSize"));
		assertEquals(31, dfa.getPropertyValue("soSendBufferSize"));
		assertEquals(32, dfa.getPropertyValue("soTimeout"));
		assertEquals("127.0.0.1", dfa.getPropertyValue("localAddress"));
		assertNotSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));		
		assertNull(dfa.getPropertyValue("errorChannel"));
	}
	
	@Test
	public void testInTcp() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpIn);
		assertSame(cfS1, dfa.getPropertyValue("serverConnectionFactory"));
		assertEquals("testInTcp",tcpIn.getComponentName());
		assertEquals("ip:tcp-inbound-channel-adapter", tcpIn.getComponentType());
		assertEquals(errorChannel, dfa.getPropertyValue("errorChannel"));
	}
	
	@Test
	public void testOutUdp() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(udpOut);
		assertTrue(udpOut.getPort() >= 5400);
		assertEquals("localhost", dfa.getPropertyValue("host"));
		int ackPort = (Integer) dfa.getPropertyValue("ackPort");
		assertTrue("Expected ackPort >= 5300 was:" + ackPort, ackPort >= 5300);
		DatagramPacketMessageMapper mapper = (DatagramPacketMessageMapper) dfa
				.getPropertyValue("mapper");
		String ackAddress = (String) new DirectFieldAccessor(mapper)
				.getPropertyValue("ackAddress");
		assertEquals("somehost:" + ackPort, ackAddress);
		assertEquals(51, dfa.getPropertyValue("ackTimeout"));
		assertEquals(true, dfa.getPropertyValue("waitForAck"));
		assertEquals(52, dfa.getPropertyValue("soReceiveBufferSize"));
		assertEquals(53, dfa.getPropertyValue("soSendBufferSize"));
		assertEquals(54, dfa.getPropertyValue("soTimeout"));
		assertEquals("127.0.0.1", dfa.getPropertyValue("localAddress"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals("testOutUdp",udpOut.getComponentName());
		assertEquals("ip:udp-outbound-channel-adapter", udpOut.getComponentType());
	}
	
	@Test
	public void testOutUdpMulticast() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(udpOutMulticast);
		assertTrue(udpOutMulticast.getPort() >= 5600);
		assertEquals("225.6.7.8", dfa.getPropertyValue("host"));
		int ackPort = (Integer) dfa.getPropertyValue("ackPort");
		assertTrue("Expected ackPort >= 5500 was:" + ackPort, ackPort >= 5500);
		DatagramPacketMessageMapper mapper = (DatagramPacketMessageMapper) dfa
				.getPropertyValue("mapper");
		String ackAddress = (String) new DirectFieldAccessor(mapper)
				.getPropertyValue("ackAddress");
		assertEquals("somehost:" + ackPort, ackAddress);
		assertEquals(51, dfa.getPropertyValue("ackTimeout"));
		assertEquals(true, dfa.getPropertyValue("waitForAck"));
		assertEquals(52, dfa.getPropertyValue("soReceiveBufferSize"));
		assertEquals(53, dfa.getPropertyValue("soSendBufferSize"));
		assertEquals(54, dfa.getPropertyValue("soTimeout"));
		assertEquals(55, dfa.getPropertyValue("timeToLive"));
	}

	@Test
	public void testOutTcp() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpOut);
		assertSame(cfC1, dfa.getPropertyValue("clientConnectionFactory"));
		assertEquals("testOutTcpNio",tcpOut.getComponentName());
		assertEquals("ip:tcp-outbound-channel-adapter", tcpOut.getComponentType());
	}

	@Test
	public void testInGateway1() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpInboundGateway1);
		assertSame(cfS2, dfa.getPropertyValue("connectionFactory"));
		assertEquals(456L, dfa.getPropertyValue("replyTimeout"));
		assertEquals("inGateway1",tcpInboundGateway1.getComponentName());
		assertEquals("ip:tcp-inbound-gateway", tcpInboundGateway1.getComponentType());
		assertEquals(errorChannel, dfa.getPropertyValue("errorChannel"));
	}

	@Test
	public void testInGateway2() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpInboundGateway2);
		assertSame(cfS3, dfa.getPropertyValue("connectionFactory"));
		assertEquals(456L, dfa.getPropertyValue("replyTimeout"));
		assertEquals("inGateway2",tcpInboundGateway2.getComponentName());
		assertEquals("ip:tcp-inbound-gateway", tcpInboundGateway2.getComponentType());
		assertNull(dfa.getPropertyValue("errorChannel"));
	}

	@Test
	public void testOutGateway() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpOutboundGateway);
		assertSame(cfC2, dfa.getPropertyValue("connectionFactory"));
		assertEquals(234L, dfa.getPropertyValue("requestTimeout"));
		assertEquals(567L, dfa.getPropertyValue("replyTimeout"));
		assertEquals("outGateway",tcpOutboundGateway.getComponentName());
		assertEquals("ip:tcp-outbound-gateway", tcpOutboundGateway.getComponentType());
	}

	@Test
	public void testConnClient1() {
		assertTrue(client1 instanceof TcpNioClientConnectionFactory);
		assertEquals("localhost", client1.getHost());
		assertTrue(client1.getPort() >= 6000);
		assertEquals(54, client1.getSoLinger());
		assertEquals(1234, client1.getSoReceiveBufferSize());
		assertEquals(1235, client1.getSoSendBufferSize());
		assertEquals(1236, client1.getSoTimeout());
		assertEquals(12, client1.getSoTrafficClass());
		DirectFieldAccessor dfa = new DirectFieldAccessor(client1);
		assertSame(serializer, dfa.getPropertyValue("serializer"));
		assertSame(deserializer, dfa.getPropertyValue("deserializer"));
		assertEquals(true, dfa.getPropertyValue("soTcpNoDelay"));
		assertEquals(true, dfa.getPropertyValue("singleUse"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals(321, dfa.getPropertyValue("poolSize"));
		assertEquals(true, dfa.getPropertyValue("usingDirectBuffers"));
		assertNotNull(dfa.getPropertyValue("interceptorFactoryChain"));
	}

	@Test
	public void testConnServer1() {
		assertTrue(server1 instanceof TcpNioServerConnectionFactory);
		assertEquals(client1.getPort(), server1.getPort());
		assertEquals(55, server1.getSoLinger());
		assertEquals(1234, server1.getSoReceiveBufferSize());
		assertEquals(1235, server1.getSoSendBufferSize());
		assertEquals(1236, server1.getSoTimeout());
		assertEquals(12, server1.getSoTrafficClass());
		DirectFieldAccessor dfa = new DirectFieldAccessor(server1);
		assertSame(serializer, dfa.getPropertyValue("serializer"));
		assertSame(deserializer, dfa.getPropertyValue("deserializer"));
		assertEquals(true, dfa.getPropertyValue("soTcpNoDelay"));
		assertEquals(true, dfa.getPropertyValue("singleUse"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals(123, dfa.getPropertyValue("poolSize"));
		assertEquals(true, dfa.getPropertyValue("usingDirectBuffers"));
		assertNotNull(dfa.getPropertyValue("interceptorFactoryChain"));		
	}

	@Test
	public void testConnClient2() {
		assertTrue(client2 instanceof TcpNetClientConnectionFactory);
		assertEquals("localhost", client1.getHost());
		assertTrue(client1.getPort() >= 6000);
		assertEquals(54, client1.getSoLinger());
		assertEquals(1234, client1.getSoReceiveBufferSize());
		assertEquals(1235, client1.getSoSendBufferSize());
		assertEquals(1236, client1.getSoTimeout());
		assertEquals(12, client1.getSoTrafficClass());
		DirectFieldAccessor dfa = new DirectFieldAccessor(client1);
		assertSame(serializer, dfa.getPropertyValue("serializer"));
		assertSame(deserializer, dfa.getPropertyValue("deserializer"));
		assertEquals(true, dfa.getPropertyValue("soTcpNoDelay"));
		assertEquals(true, dfa.getPropertyValue("singleUse"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals(321, dfa.getPropertyValue("poolSize"));
		assertNotNull(dfa.getPropertyValue("interceptorFactoryChain"));
	}

	@Test
	public void testConnServer2() {
		assertTrue(server2 instanceof TcpNetServerConnectionFactory);
		assertEquals(client1.getPort(), server1.getPort());
		assertEquals(55, server1.getSoLinger());
		assertEquals(1234, server1.getSoReceiveBufferSize());
		assertEquals(1235, server1.getSoSendBufferSize());
		assertEquals(1236, server1.getSoTimeout());
		assertEquals(12, server1.getSoTrafficClass());
		DirectFieldAccessor dfa = new DirectFieldAccessor(server1);
		assertSame(serializer, dfa.getPropertyValue("serializer"));
		assertSame(deserializer, dfa.getPropertyValue("deserializer"));
		assertEquals(true, dfa.getPropertyValue("soTcpNoDelay"));
		assertEquals(true, dfa.getPropertyValue("singleUse"));
		assertSame(taskExecutor, dfa.getPropertyValue("taskExecutor"));
		assertEquals(123, dfa.getPropertyValue("poolSize"));
		assertNotNull(dfa.getPropertyValue("interceptorFactoryChain"));		
	}

	@Test
	public void testNewOut1() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpNewOut1);
		assertSame(client1, dfa.getPropertyValue("clientConnectionFactory"));
	}
	
	@Test
	public void testNewOut2() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpNewOut2);
		assertSame(server1, dfa.getPropertyValue("serverConnectionFactory"));
	}
	
	@Test
	public void testNewIn1() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpNewIn1);
		assertSame(client1, dfa.getPropertyValue("clientConnectionFactory"));
		assertNull(dfa.getPropertyValue("errorChannel"));
	}
	
	@Test
	public void testNewIn2() {
		DirectFieldAccessor dfa = new DirectFieldAccessor(tcpNewIn2);
		assertSame(server1, dfa.getPropertyValue("serverConnectionFactory"));
	}
	
}
