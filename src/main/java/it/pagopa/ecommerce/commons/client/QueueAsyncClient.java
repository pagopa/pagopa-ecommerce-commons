package it.pagopa.ecommerce.commons.client;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.storage.queue.models.SendMessageResult;
import it.pagopa.ecommerce.commons.documents.v1.TransactionEvent;
import it.pagopa.ecommerce.commons.queues.QueueEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * <p>
 * Wrapper for Azure SDK Storage Queue clients in order to allow sending of
 * {@link QueueEvent} instances only.
 * </p>
 * <p>
 * {@link QueueEvent} carries tracing information so that asynchronous workloads
 * can be correlated between producers and consumers.
 * </p>
 *
 * @see QueueEvent
 */
@Slf4j
public class QueueAsyncClient {
    private final com.azure.storage.queue.QueueAsyncClient innerClient;

    /**
     * Primary constructor
     *
     * @param innerClient wrapped client
     */
    public QueueAsyncClient(com.azure.storage.queue.QueueAsyncClient innerClient) {
        this.innerClient = innerClient;
    }

    /**
     * Wraps
     * {@link com.azure.storage.queue.QueueAsyncClient#sendMessageWithResponse(BinaryData, Duration, Duration)
     * QueueAsyncClient.sendMessageWithResponse}
     *
     * @param event             event
     * @param visibilityTimeout visibility timeout
     * @param timeToLive        TTL
     * @return wrapped client response
     * @param <T> type of event
     */
    public <T extends TransactionEvent<?>> Mono<Response<SendMessageResult>> sendMessageWithResponse(
                                                                                                     QueueEvent<T> event,
                                                                                                     Duration visibilityTimeout,
                                                                                                     Duration timeToLive
    ) {
        log.debug("Sending event {} with tracing info: {}", event.event(), event.tracingInfo());
        return BinaryData.fromObjectAsync(event)
                .flatMap(e -> innerClient.sendMessageWithResponse(e, visibilityTimeout, timeToLive));
    }

    /**
     * Gets the queue name associated to this client (see
     * {@link com.azure.storage.queue.QueueAsyncClient#getQueueName()
     * QueueAsyncClient.getQueueName})
     *
     * @return the queue name
     */
    public String getQueueName() {
        return innerClient.getQueueName();
    }
}
