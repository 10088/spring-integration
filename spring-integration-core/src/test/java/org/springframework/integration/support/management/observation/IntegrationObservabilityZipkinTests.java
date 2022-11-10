/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.support.management.observation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.EndpointId;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.interceptor.ObservationPropagationChannelInterceptor;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.integration.config.GlobalChannelInterceptor;
import org.springframework.integration.gateway.MessagingGatewaySupport;
import org.springframework.integration.handler.BridgeHandler;
import org.springframework.integration.handler.advice.HandleMessageAdvice;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.GenericMessage;

import io.micrometer.common.KeyValues;
import io.micrometer.core.tck.MeterRegistryAssert;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.test.SampleTestRunner;
import io.micrometer.tracing.test.simple.SpansAssert;

/**
 * @author Artem Bilan
 *
 * @since 6.0
 */
public class IntegrationObservabilityZipkinTests extends SampleTestRunner {

	@Override
	public TracingSetup[] getTracingSetup() {
		return new TracingSetup[]{ TracingSetup.IN_MEMORY_BRAVE, TracingSetup.ZIPKIN_BRAVE };
	}

	@Override
	public SampleTestRunnerConsumer yourCode() {
		return (bb, meterRegistry) -> {
			ObservationRegistry observationRegistry = getObservationRegistry();
			try (AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext()) {
				applicationContext.registerBean(ObservationRegistry.class, () -> observationRegistry);
				applicationContext.register(ObservationIntegrationTestConfiguration.class);
				applicationContext.refresh();

				TestMessagingGatewaySupport messagingGateway =
						applicationContext.getBean(TestMessagingGatewaySupport.class);

				Message<?> receive = messagingGateway.process(new GenericMessage<>("test data"));

				assertThat(receive).isNotNull()
						.extracting("payload").isEqualTo("test data");
				var configuration = applicationContext.getBean(ObservationIntegrationTestConfiguration.class);

				assertThat(configuration.observedHandlerLatch.await(10, TimeUnit.SECONDS)).isTrue();
			}

			SpansAssert.assertThat(bb.getFinishedSpans())
					.haveSameTraceId()
					.hasASpanWithName("testInboundGateway process", spanAssert -> spanAssert
							.hasTag(IntegrationObservation.GatewayTags.COMPONENT_NAME.asString(), "testInboundGateway")
							.hasTag(IntegrationObservation.GatewayTags.COMPONENT_TYPE.asString(), "gateway")
							.hasTagWithKey("test.message.id")
							.hasKindEqualTo(Span.Kind.SERVER))
					.hasASpanWithName("observedEndpoint receive", spanAssert -> spanAssert
							.hasTag(IntegrationObservation.HandlerTags.COMPONENT_NAME.asString(), "observedEndpoint")
							.hasTag(IntegrationObservation.HandlerTags.COMPONENT_TYPE.asString(), "handler")
							.hasKindEqualTo(Span.Kind.CONSUMER))
					.hasSize(2);

			MeterRegistryAssert.assertThat(getMeterRegistry())
					.hasTimerWithNameAndTags("spring.integration.handler",
							KeyValues.of(
									IntegrationObservation.HandlerTags.COMPONENT_NAME.asString(), "observedEndpoint",
									IntegrationObservation.HandlerTags.COMPONENT_TYPE.asString(), "handler",
									"error", "none"));
		};
	}


	@Configuration
	@EnableIntegration
	@EnableIntegrationManagement(
			observationPatterns =
					"${spring.integration.management.observation-patterns:observedEndpoint,testInboundGateway}")
	public static class ObservationIntegrationTestConfiguration {

		CountDownLatch observedHandlerLatch = new CountDownLatch(1);

		@Bean
		@GlobalChannelInterceptor
		public ChannelInterceptor observationPropagationInterceptor(ObservationRegistry observationRegistry) {
			return new ObservationPropagationChannelInterceptor(observationRegistry);
		}

		@Bean
		TestMessagingGatewaySupport testInboundGateway(PollableChannel queueChannel) {
			TestMessagingGatewaySupport messagingGatewaySupport = new TestMessagingGatewaySupport();
			messagingGatewaySupport.setObservationConvention(
					new DefaultMessageRequestReplyReceiverObservationConvention() {

						@Override
						public KeyValues getHighCardinalityKeyValues(MessageRequestReplyReceiverContext context) {
							return KeyValues.of("test.message.id", context.getCarrier().getHeaders().getId().toString());
						}

					});
			messagingGatewaySupport.setRequestChannel(queueChannel);
			return messagingGatewaySupport;
		}

		@Bean
		public PollableChannel queueChannel() {
			return new QueueChannel();
		}

		@Bean
		@EndpointId("observedEndpoint")
		@ServiceActivator(inputChannel = "queueChannel",
				poller = @Poller(fixedDelay = "100"),
				adviceChain = "observedHandlerAdvice")
		BridgeHandler bridgeHandler() {
			return new BridgeHandler();
		}

		@Bean
		HandleMessageAdvice observedHandlerAdvice() {
			return invocation -> {
				try {
					return invocation.proceed();
				}
				finally {
					this.observedHandlerLatch.countDown();
				}
			};
		}

	}

	private static class TestMessagingGatewaySupport extends MessagingGatewaySupport {

		@Nullable
		Message<?> process(Message<?> request) {
			return sendAndReceiveMessage(request);
		}

	}

}