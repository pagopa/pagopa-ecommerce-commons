package it.pagopa.ecommerce.commons.client;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.storage.queue.models.SendMessageResult;
import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.queues.QueueEvent;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class QueueAsyncClientTest {
    private final com.azure.storage.queue.QueueAsyncClient azureQueueAsyncClient = Mockito
            .mock(com.azure.storage.queue.QueueAsyncClient.class);

    private final JsonSerializer jsonSerializer = JsonSerializerProviders.createInstance(true);

    private final QueueAsyncClient queueAsyncClient = new QueueAsyncClient(azureQueueAsyncClient, jsonSerializer);

    @Test
    void clientForwardsParametersToInnerClient() {
        /* preconditions */
        Mono<Response<SendMessageResult>> queueResponse = queueSuccessfulResponse();
        QueueEvent<TransactionActivatedEvent> queueEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(),
                MOCK_TRACING_INFO
        );

        Duration visibilityTimeout = Duration.ofSeconds(10);
        Duration timeToLive = Duration.ofSeconds(10);
        BinaryData serializedEvent = BinaryData.fromObject(queueEvent);

        Mockito.when(
                azureQueueAsyncClient
                        .sendMessageWithResponse(any(BinaryData.class), eq(visibilityTimeout), eq(timeToLive))
        )
                .thenReturn(queueResponse);

        /* test */
        Hooks.onOperatorDebug();

        StepVerifier.create(queueAsyncClient.sendMessageWithResponse(queueEvent, visibilityTimeout, timeToLive))
                .expectNext(Objects.requireNonNull(queueResponse.block()))
                .verifyComplete();

        /* assertions */
        Mockito.verify(azureQueueAsyncClient, Mockito.times(1)).sendMessageWithResponse(
                argThat((BinaryData b) -> Arrays.equals(b.toBytes(), serializedEvent.toBytes())),
                eq(visibilityTimeout),
                eq(timeToLive)
        );
    }

    @Test
    void queueNameForwardsToInnerClient() {
        /* preconditions */
        String expectedQueueName = "queue-name";
        Mockito.when(
                azureQueueAsyncClient
                        .getQueueName()
        )
                .thenReturn(expectedQueueName);

        /* test */
        String actualQueueName = queueAsyncClient.getQueueName();

        /* assertions */
        assertEquals(expectedQueueName, actualQueueName);

        Mockito.verify(azureQueueAsyncClient, Mockito.times(1)).getQueueName();
    }

    private static Mono<Response<SendMessageResult>> queueSuccessfulResponse() {
        return Mono.just(new Response<>() {
            @Override
            public int getStatusCode() {
                return 200;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public HttpRequest getRequest() {
                return null;
            }

            @Override
            public SendMessageResult getValue() {
                return new SendMessageResult();
            }
        });
    }
}
