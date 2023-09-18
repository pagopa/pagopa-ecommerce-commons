package it.pagopa.ecommerce.commons.documents.v2.serialization;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.queues.QueueEvent;
import it.pagopa.ecommerce.commons.queues.StrictJsonSerializerProvider;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TransactionEventTypeResolverTest {
    private final JsonSerializer jsonSerializer = new StrictJsonSerializerProvider().createInstance();

    @Test
    void canRoundtripQueueEventSerialization() {
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                new TransactionRefundRequestedEvent(
                        TransactionTestUtils.TRANSACTION_ID,
                        new TransactionRefundedData(TransactionStatusDto.REFUND_REQUESTED)
                ),
                MOCK_TRACING_INFO
        );
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);

        Mono<QueueEvent<TransactionRefundRequestedEvent>> refundRequestDeserialized = jsonSerializer
                .deserializeFromBytesAsync(
                        serialized,
                        new TypeReference<>() {
                        }
                );
        Mono<QueueEvent<TransactionActivatedEvent>> activatedEventDeserialized = jsonSerializer
                .deserializeFromBytesAsync(
                        serialized,
                        new TypeReference<>() {
                        }
                );

        Mono<Either<QueueEvent<TransactionRefundRequestedEvent>, QueueEvent<TransactionActivatedEvent>>> roundTripWithFailure = activatedEventDeserialized
                .map(Either::<QueueEvent<TransactionRefundRequestedEvent>, QueueEvent<TransactionActivatedEvent>>right)
                .onErrorResume(
                        (e) -> refundRequestDeserialized
                                .map(Either::left)
                );

        StepVerifier.create(roundTripWithFailure)
                .expectNextMatches(e -> e.fold(ev -> {
                    TransactionRefundRequestedEvent event = ev.event();
                    TransactionRefundRequestedEvent originalTransactionEvent = originalEvent.event();
                    return event.getData().equals(originalTransactionEvent.getData());
                }, r -> {
                    throw new RuntimeException("Event deserialized as incorrect type!");
                }))
                .verifyComplete();
    }

    @Test
    void correctlyRoundtripsEventCode() {
        String input = """
                {"event":{"id":"0f41f3f5-2171-4d19-90ba-6ae1cd3edfc1","transactionId":"3acfaa8ab7ce488b9a5ceb72781194f0","creationDate":"2023-07-24T14:54:47.757098+02:00[Europe/Rome]","data":{"email":{"data":"ebe32cab-b324-4cbc-b14e-e7f45783fb6b"},"paymentNotices":[{"paymentToken":"paymentToken","rptId":"77777777777111111111111111111","description":"description","amount":100,"paymentContextCode":"paymentContextCode","transferList":[{"paFiscalCode":"transferPAFiscalCode","digitalStamp":true,"transferAmount":0,"transferCategory":"transferCategory"}],"allCCP":false}],"faultCode":"","faultCodeString":"","clientId":"CHECKOUT","idCart":"ecIdCart","paymentTokenValiditySeconds":900},"eventCode":"TRANSACTION_ACTIVATED_EVENT"},"tracingInfo":{"traceparent":"mock_traceparent","tracestate":"mock_tracestate","baggage":"mock_baggage"}}
                """;

        BinaryData b = BinaryData.fromBytes(input.getBytes());

        QueueEvent<TransactionActivatedEvent> t = b.toObject(new TypeReference<>() {
        }, jsonSerializer);

        assertEquals(TransactionEventCode.TRANSACTION_ACTIVATED_EVENT, t.event().getEventCode());
    }

    @Test
    void canRoundtripQueueEventSerializationWithBinaryData() {
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                new TransactionRefundRequestedEvent(
                        TransactionTestUtils.TRANSACTION_ID,
                        new TransactionRefundedData(TransactionStatusDto.REFUND_REQUESTED)
                ),
                MOCK_TRACING_INFO
        );

        BinaryData binaryData = BinaryData.fromObject(originalEvent, jsonSerializer);

        Mono<QueueEvent<TransactionRefundRequestedEvent>> refundRequestDeserialized = binaryData
                .toObjectAsync(
                        new TypeReference<>() {
                        },
                        jsonSerializer
                );
        Mono<QueueEvent<TransactionActivatedEvent>> activatedEventDeserialized = binaryData
                .toObjectAsync(
                        new TypeReference<>() {
                        },
                        jsonSerializer
                );

        Mono<Either<QueueEvent<TransactionRefundRequestedEvent>, QueueEvent<TransactionActivatedEvent>>> roundTripWithFailure = activatedEventDeserialized
                .map(Either::<QueueEvent<TransactionRefundRequestedEvent>, QueueEvent<TransactionActivatedEvent>>right)
                .onErrorResume(
                        (e) -> refundRequestDeserialized
                                .map(Either::left)
                );

        StepVerifier.create(roundTripWithFailure)
                .expectNextMatches(e -> e.fold(ev -> {
                    TransactionRefundRequestedEvent event = ev.event();
                    TransactionRefundRequestedEvent originalTransactionEvent = originalEvent.event();
                    return event.getData().equals(originalTransactionEvent.getData());
                }, r -> {
                    throw new RuntimeException("Event deserialized as incorrect type!");
                }))
                .verifyComplete();
    }

    @Test
    void serializerDisambiguatesTypesWithSameShape() {
        QueueEvent<TransactionClosedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionClosedEvent(TransactionClosureData.Outcome.KO),
                MOCK_TRACING_INFO
        );
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);

        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        Mono<QueueEvent<TransactionClosedEvent>> closedDeserialized = jsonSerializer
                .deserializeFromBytesAsync(
                        serialized,
                        new TypeReference<>() {
                        }
                );
        Mono<QueueEvent<TransactionClosureFailedEvent>> closureFailedDeserialized = jsonSerializer
                .deserializeFromBytesAsync(
                        serialized,
                        new TypeReference<>() {
                        }
                );

        Mono<Either<QueueEvent<TransactionClosedEvent>, QueueEvent<TransactionClosureFailedEvent>>> roundTripWithFailure = closureFailedDeserialized
                .map(Either::<QueueEvent<TransactionClosedEvent>, QueueEvent<TransactionClosureFailedEvent>>right)
                .onErrorResume(
                        (e) -> closedDeserialized
                                .map(Either::left)
                );

        StepVerifier.create(roundTripWithFailure)
                .expectNextMatches(v -> v.getLeft().event().getData().equals(originalEvent.event().getData()))
                .verifyComplete();
    }
}
