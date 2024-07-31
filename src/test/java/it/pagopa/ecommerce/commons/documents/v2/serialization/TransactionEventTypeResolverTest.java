package it.pagopa.ecommerce.commons.documents.v2.serialization;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.documents.v2.activation.EmptyTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.NpgTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.NpgTransactionGatewayAuthorizationRequestedData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.PgsTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.RedirectTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.refund.EmptyGatewayRefundData;
import it.pagopa.ecommerce.commons.documents.v2.refund.NpgGatewayRefundData;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v2.TransactionEventCode;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.queues.QueueEvent;
import it.pagopa.ecommerce.commons.queues.StrictJsonSerializerProvider;
import it.pagopa.ecommerce.commons.queues.mixin.deserialization.v2.TransactionEventMixInClassFieldDiscriminator;
import it.pagopa.ecommerce.commons.queues.mixin.serialization.v2.QueueEventMixInClassFieldDiscriminator;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.testcontainers.shaded.org.apache.commons.io.FileUtils;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static it.pagopa.ecommerce.commons.v1.TransactionTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class TransactionEventTypeResolverTest {
    private final JsonSerializer jsonSerializer = new StrictJsonSerializerProvider()
            .addMixIn(QueueEvent.class, QueueEventMixInClassFieldDiscriminator.class)
            .addMixIn(TransactionEvent.class, TransactionEventMixInClassFieldDiscriminator.class)
            .createInstance();

    @Test
    void canRoundtripQueueEventSerialization() {
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                new TransactionRefundRequestedEvent(
                        TransactionTestUtils.TRANSACTION_ID,
                        new TransactionRefundRequestedData(null, TransactionStatusDto.REFUND_REQUESTED)
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
                {"event":{"_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent","id":"0f41f3f5-2171-4d19-90ba-6ae1cd3edfc1","transactionId":"3acfaa8ab7ce488b9a5ceb72781194f0","creationDate":"2023-07-24T14:54:47.757098+02:00[Europe/Rome]","data":{"email":{"data":"ebe32cab-b324-4cbc-b14e-e7f45783fb6b"},"paymentNotices":[{"paymentToken":"paymentToken","rptId":"77777777777111111111111111111","description":"description","amount":100,"paymentContextCode":"paymentContextCode","transferList":[{"paFiscalCode":"transferPAFiscalCode","digitalStamp":true,"transferAmount":0,"transferCategory":"transferCategory"}],"allCCP":false}],"faultCode":"","faultCodeString":"","clientId":"CHECKOUT","idCart":"ecIdCart","paymentTokenValiditySeconds":900},"eventCode":"TRANSACTION_ACTIVATED_EVENT"},"tracingInfo":{"traceparent":"mock_traceparent","tracestate":"mock_tracestate","baggage":"mock_baggage"}}
                """;

        BinaryData b = BinaryData.fromBytes(input.getBytes());

        QueueEvent<TransactionActivatedEvent> t = b.toObject(new TypeReference<>() {
        }, jsonSerializer);

        assertEquals(TransactionEventCode.TRANSACTION_ACTIVATED_EVENT.toString(), t.event().getEventCode());
    }

    @Test
    void canRoundtripQueueEventSerializationWithBinaryData() {
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                new TransactionRefundRequestedEvent(
                        TransactionTestUtils.TRANSACTION_ID,
                        new TransactionRefundRequestedData(null, TransactionStatusDto.REFUND_REQUESTED)
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

    @Test
    void canRoundtripQueueEventSerializationWithBinaryDataAddingClassDiscriminatorField() {
        QueueEvent<TransactionClosedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionClosedEvent(TransactionClosureData.Outcome.KO),
                MOCK_TRACING_INFO
        );
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);
        assertTrue(
                serializedString
                        .contains("\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionClosedEvent\"")
        );
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionClosedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = "a91a4e54-d1bc-48fb-3221-cc2893fc88e2")
    @NullSource
    void canRoundTripQueueTransactionActivatedEventSerializationWithEmptyActivationData(String userId) {
        String expectedUserId = userId == null ? "null" : "\"" + userId + "\"";
        String expectedSerializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "email": {
                                "data": "a91a4e54-d1bc-48fb-b252-cc2893fc88e2"
                            },
                            "paymentNotices": [
                                {
                                    "paymentToken": "paymentToken",
                                    "rptId": "77777777777111111111111111111",
                                    "description": "description",
                                    "amount": 100,
                                    "paymentContextCode": "paymentContextCode",
                                    "transferList": [
                                        {
                                            "paFiscalCode": "transferPAFiscalCode",
                                            "digitalStamp": true,
                                            "transferAmount": 0,
                                            "transferCategory": "transferCategory"
                                        }
                                    ],
                                    "companyName": "companyName",
                                    "creditorReferenceId": null,
                                    "allCCP": false
                                }
                            ],
                            "faultCode": "",
                            "faultCodeString": "",
                            "clientId": "CHECKOUT",
                            "idCart": "ecIdCart",
                            "paymentTokenValiditySeconds": 900,
                            "transactionGatewayActivationData": {
                                "type": "EMPTY"
                            },
                            "userId": %s
                        },
                        "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }
                """.formatted(expectedUserId)
                .replace("\n", "").replace(" ", "");
        QueueEvent<TransactionActivatedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(new EmptyTransactionGatewayActivationData()),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().getData().setEmail(new Confidential<>("a91a4e54-d1bc-48fb-b252-cc2893fc88e2"));
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setUserId(userId);
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains("\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent\"")
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionActivatedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = "a91a4e54-d1bc-48fb-3221-cc2893fc88e2")
    @NullSource
    void canRoundTripQueueTransactionActivatedEventSerializationWithNpgActivationData(String userId) {
        String expectedUserId = userId == null ? "null" : "\"" + userId + "\"";
        String expectedSerializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "email": {
                                "data": "a91a4e54-d1bc-48fb-b252-cc2893fc88e2"
                            },
                            "paymentNotices": [
                                {
                                    "paymentToken": "paymentToken",
                                    "rptId": "77777777777111111111111111111",
                                    "description": "description",
                                    "amount": 100,
                                    "paymentContextCode": "paymentContextCode",
                                    "transferList": [
                                        {
                                            "paFiscalCode": "transferPAFiscalCode",
                                            "digitalStamp": true,
                                            "transferAmount": 0,
                                            "transferCategory": "transferCategory"
                                        }
                                    ],
                                    "companyName": "companyName",
                                    "creditorReferenceId": null,
                                    "allCCP": false
                                }
                            ],
                            "faultCode": "",
                            "faultCodeString": "",
                            "clientId": "CHECKOUT",
                            "idCart": "ecIdCart",
                            "paymentTokenValiditySeconds": 900,
                            "transactionGatewayActivationData": {
                                "type": "NPG",
                                "orderId": "npgOrderId",
                                "correlationId": "npgCorrelationId"
                            },
                            "userId": %s
                        },
                        "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }""".formatted(expectedUserId).replace("\n", "").replace(" ", "");
        QueueEvent<TransactionActivatedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(
                        TransactionTestUtils.npgTransactionGatewayActivationData()
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().getData().setEmail(new Confidential<>("a91a4e54-d1bc-48fb-b252-cc2893fc88e2"));
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setUserId(userId);
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains("\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent\"")
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionActivatedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationCompletedEventSerializationWithPGSData() {
        String expectedSerializedEvent = """
                {
                     "event": {
                         "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent",
                         "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                         "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                         "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                         "data": {
                             "authorizationCode": "authorizationCode",
                             "rrn": "rrn",
                             "timestampOperation": "2023-01-01T01:02:03+01:00",
                             "transactionGatewayAuthorizationData": {
                                 "type": "PGS",
                                 "errorCode": "errorCode",
                                 "authorizationResultDto": "OK"
                             }
                         },
                         "eventCode": "TRANSACTION_AUTHORIZATION_COMPLETED_EVENT"
                     },
                     "tracingInfo": {
                         "traceparent": "mock_traceparent",
                         "tracestate": "mock_tracestate",
                         "baggage": "mock_baggage"
                     }
                 }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationCompletedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationCompletedEvent(
                        new PgsTransactionGatewayAuthorizationData("errorCode", AuthorizationResultDto.OK)
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationCompletedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationCompletedEventSerializationWithNPGData() {
        String expectedSerializedEvent = """
                {
                       "event": {
                           "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent",
                           "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                           "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                           "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                           "data": {
                               "authorizationCode": "authorizationCode",
                               "rrn": "rrn",
                               "timestampOperation": "2023-01-01T01:02:03+01:00",
                               "transactionGatewayAuthorizationData": {
                                   "type": "NPG",
                                   "operationResult": "EXECUTED",
                                   "operationId": "operationId",
                                   "paymentEndToEndId": "paymentEndToEndId",
                                   "errorCode": "errorCode",
                                   "validationServiceId": "validationServiceId"
                               }
                           },
                           "eventCode": "TRANSACTION_AUTHORIZATION_COMPLETED_EVENT"
                       },
                       "tracingInfo": {
                           "traceparent": "mock_traceparent",
                           "tracestate": "mock_tracestate",
                           "baggage": "mock_baggage"
                       }
                   }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationCompletedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationCompletedEvent(
                        new NpgTransactionGatewayAuthorizationData(
                                OperationResultDto.EXECUTED,
                                "operationId",
                                "paymentEndToEndId",
                                "errorCode",
                                "validationServiceId"
                        )
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationCompletedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationCompletedEventSerializationWithRedirectData() {
        String expectedSerializedEvent = """
                {
                       "event": {
                           "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent",
                           "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                           "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                           "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                           "data": {
                               "authorizationCode": "authorizationCode",
                               "rrn": "rrn",
                               "timestampOperation": "2023-01-01T01:02:03+01:00",
                               "transactionGatewayAuthorizationData": {
                                   "type": "REDIRECT",
                                   "outcome": "KO",
                                   "errorCode": "errorCode"
                               }
                           },
                           "eventCode": "TRANSACTION_AUTHORIZATION_COMPLETED_EVENT"
                       },
                       "tracingInfo": {
                           "traceparent": "mock_traceparent",
                           "tracestate": "mock_tracestate",
                           "baggage": "mock_baggage"
                       }
                   }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationCompletedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationCompletedEvent(
                        new RedirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.KO,
                                "errorCode"
                        )
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationCompletedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationRequestedEventSerializationWithPGSData() {
        String expectedSerializedEvent = """
                {
                      "event": {
                          "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent",
                          "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                          "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                          "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                          "data": {
                              "amount": 100,
                              "fee": 10,
                              "paymentInstrumentId": "paymentInstrumentId",
                              "pspId": "pspId",
                              "paymentTypeCode": "CP",
                              "brokerName": "brokerName",
                              "pspChannelCode": "pspChannelCode",
                              "paymentMethodName": "CARDS",
                              "pspBusinessName": "pspBusinessName",
                              "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                              "paymentGateway": "VPOS",
                              "paymentMethodDescription": "paymentMethodDescription",
                              "transactionGatewayAuthorizationRequestedData": {
                                  "type": "PGS",
                                  "logo": "http://paymentMethodLogo.it",
                                  "brand": "VISA"
                              },
                              "pspOnUs": false
                          },
                          "eventCode": "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
                      },
                      "tracingInfo": {
                          "traceparent": "mock_traceparent",
                          "tracestate": "mock_tracestate",
                          "baggage": "mock_baggage"
                      }
                  }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationRequestedEvent(),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setAuthorizationRequestId("d93cb073-445c-476b-b0fd-abe343d8b6a5");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationRequestedEventSerializationWithNPGData() {
        String expectedSerializedEvent = """
                {
                       "event": {
                           "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent",
                           "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                           "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                           "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                           "data": {
                               "amount": 100,
                               "fee": 10,
                               "paymentInstrumentId": "paymentInstrumentId",
                               "pspId": "pspId",
                               "paymentTypeCode": "CP",
                               "brokerName": "brokerName",
                               "pspChannelCode": "pspChannelCode",
                               "paymentMethodName": "CARDS",
                               "pspBusinessName": "pspBusinessName",
                               "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                               "paymentGateway": "NPG",
                               "paymentMethodDescription": "paymentMethodDescription",
                               "transactionGatewayAuthorizationRequestedData": {
                                   "type": "NPG",
                                   "logo":"http://paymentMethodLogo.it",
                                   "brand":"VISA",
                                   "sessionId":"npgSessionId",
                                   "confirmPaymentSessionId":"npgConfirmPaymentSessionId",
                                   "walletInfo": null
                               },
                               "pspOnUs": false
                           },
                           "eventCode": "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
                       },
                       "tracingInfo": {
                           "traceparent": "mock_traceparent",
                           "tracestate": "mock_tracestate",
                           "baggage": "mock_baggage"
                       }
                   }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationRequestedEvent(
                        TransactionAuthorizationRequestData.PaymentGateway.NPG,
                        TransactionTestUtils.npgTransactionGatewayAuthorizationRequestedData()
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setAuthorizationRequestId("d93cb073-445c-476b-b0fd-abe343d8b6a5");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationRequestedEventSerializationWithRedirectData() {
        String expectedSerializedEvent = """
                {
                       "event": {
                           "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent",
                           "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                           "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                           "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                           "data": {
                               "amount": 100,
                               "fee": 10,
                               "paymentInstrumentId": "paymentInstrumentId",
                               "pspId": "pspId",
                               "paymentTypeCode": "CP",
                               "brokerName": "brokerName",
                               "pspChannelCode": "pspChannelCode",
                               "paymentMethodName": "CARDS",
                               "pspBusinessName": "pspBusinessName",
                               "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                               "paymentGateway": "REDIRECT",
                               "paymentMethodDescription": "paymentMethodDescription",
                               "transactionGatewayAuthorizationRequestedData": {
                                   "type": "REDIRECT",
                                   "logo":"http://paymentMethodLogo.it",
                                   "transactionOutcomeTimeoutMillis":60000
                               },
                               "pspOnUs": false
                           },
                           "eventCode": "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
                       },
                       "tracingInfo": {
                           "traceparent": "mock_traceparent",
                           "tracestate": "mock_tracestate",
                           "baggage": "mock_baggage"
                       }
                   }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationRequestedEvent(
                        TransactionAuthorizationRequestData.PaymentGateway.REDIRECT,
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationRequestedData()
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setAuthorizationRequestId("d93cb073-445c-476b-b0fd-abe343d8b6a5");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canDeserializeTransactionActivatedEventWithUserIdFieldNotSet() {
        String serializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "email": {
                                "data": "a91a4e54-d1bc-48fb-b252-cc2893fc88e2"
                            },
                            "paymentNotices": [
                                {
                                    "paymentToken": "paymentToken",
                                    "rptId": "77777777777111111111111111111",
                                    "description": "description",
                                    "amount": 100,
                                    "paymentContextCode": "paymentContextCode",
                                    "transferList": [
                                        {
                                            "paFiscalCode": "transferPAFiscalCode",
                                            "digitalStamp": true,
                                            "transferAmount": 0,
                                            "transferCategory": "transferCategory"
                                        }
                                    ],
                                    "companyName": "companyName",
                                    "allCCP": false
                                }
                            ],
                            "faultCode": "",
                            "faultCodeString": "",
                            "clientId": "CHECKOUT",
                            "idCart": "ecIdCart",
                            "paymentTokenValiditySeconds": 900,
                            "transactionGatewayActivationData": {
                                "type": "EMPTY"
                            }
                        },
                        "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }
                """;
        QueueEvent<TransactionActivatedEvent> expectedEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(new EmptyTransactionGatewayActivationData()),
                MOCK_TRACING_INFO
        );
        expectedEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        expectedEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        expectedEvent.event().getData().setEmail(new Confidential<>("a91a4e54-d1bc-48fb-b252-cc2893fc88e2"));
        expectedEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        expectedEvent.event().getData().setUserId(null);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serializedEvent.getBytes(StandardCharsets.UTF_8),
                                new TypeReference<QueueEvent<TransactionActivatedEvent>>() {
                                }
                        )
        )
                .assertNext(deserializedEvent -> assertEquals(deserializedEvent, expectedEvent))
                .verifyComplete();
    }

    @Test
    void canDeserializeAuthorizationCompletedEventWithNPGDataAndNullableFieldsNotSet() {
        String serializedEvent = """
                {
                       "event": {
                           "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedEvent",
                           "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                           "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                           "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                           "data": {
                               "authorizationCode": "authorizationCode",
                               "rrn": "rrn",
                               "timestampOperation": "2023-01-01T01:02:03+01:00",
                               "transactionGatewayAuthorizationData": {
                                   "type": "NPG",
                                   "operationResult": "EXECUTED",
                                   "operationId": "operationId",
                                   "paymentEndToEndId": "paymentEndToEndId",
                                   "errorCode": "errorCode"
                               }
                           },
                           "eventCode": "TRANSACTION_AUTHORIZATION_COMPLETED_EVENT"
                       },
                       "tracingInfo": {
                           "traceparent": "mock_traceparent",
                           "tracestate": "mock_tracestate",
                           "baggage": "mock_baggage"
                       }
                   }
                """;
        QueueEvent<TransactionAuthorizationCompletedEvent> expectedEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationCompletedEvent(
                        new NpgTransactionGatewayAuthorizationData(
                                OperationResultDto.EXECUTED,
                                "operationId",
                                "paymentEndToEndId",
                                "errorCode",
                                null
                        )
                ),
                MOCK_TRACING_INFO
        );
        expectedEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        expectedEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        expectedEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serializedEvent.getBytes(StandardCharsets.UTF_8),
                                new TypeReference<QueueEvent<TransactionAuthorizationCompletedEvent>>() {
                                }
                        )
        )
                .assertNext(event -> assertEquals(expectedEvent, event))
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationRequestedEventSerializationWithNPGDataWithCardWalletInfo() {
        String expectedSerializedEvent = """
                {
                        "event": {
                            "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent",
                            "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                            "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                            "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                            "data": {
                                "amount": 100,
                                "fee": 10,
                                "paymentInstrumentId": "paymentInstrumentId",
                                "pspId": "pspId",
                                "paymentTypeCode": "CP",
                                "brokerName": "brokerName",
                                "pspChannelCode": "pspChannelCode",
                                "paymentMethodName": "CARDS",
                                "pspBusinessName": "pspBusinessName",
                                "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                                "paymentGateway": "NPG",
                                "paymentMethodDescription": "paymentMethodDescription",
                                "transactionGatewayAuthorizationRequestedData": {
                                    "type": "NPG",
                                    "logo": "http://paymentMethodLogo.it",
                                    "brand": "VISA",
                                    "sessionId": "npgSessionId",
                                    "confirmPaymentSessionId": "npgConfirmPaymentSessionId",
                                    "walletInfo": {
                                        "walletId": "17601410-5f1d-4189-b8d1-92637952ee5f",
                                        "walletDetails": {
                                            "type": "CARDS",
                                            "bin": "12345678",
                                            "lastFourDigits": "1234"
                                        }
                                    }
                                },
                                "pspOnUs": false
                            },
                            "eventCode": "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
                        },
                        "tracingInfo": {
                            "traceparent": "mock_traceparent",
                            "tracestate": "mock_tracestate",
                            "baggage": "mock_baggage"
                        }
                    }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationRequestedEvent(
                        TransactionAuthorizationRequestData.PaymentGateway.NPG,
                        TransactionTestUtils
                                .npgTransactionGatewayAuthorizationRequestedData(TransactionTestUtils.cardsWalletInfo())
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setAuthorizationRequestId("d93cb073-445c-476b-b0fd-abe343d8b6a5");
        NpgTransactionGatewayAuthorizationRequestedData authRequestedData = (NpgTransactionGatewayAuthorizationRequestedData) originalEvent
                .event().getData().getTransactionGatewayAuthorizationRequestedData();
        authRequestedData.getWalletInfo().setWalletId("17601410-5f1d-4189-b8d1-92637952ee5f");

        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAuthorizationRequestedEventSerializationWithNPGDataWithPaypalWalletInfo() {
        String expectedSerializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "amount": 100,
                            "fee": 10,
                            "paymentInstrumentId": "paymentInstrumentId",
                            "pspId": "pspId",
                            "paymentTypeCode": "CP",
                            "brokerName": "brokerName",
                            "pspChannelCode": "pspChannelCode",
                            "paymentMethodName": "CARDS",
                            "pspBusinessName": "pspBusinessName",
                            "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                            "paymentGateway": "NPG",
                            "paymentMethodDescription": "paymentMethodDescription",
                            "transactionGatewayAuthorizationRequestedData": {
                                "type": "NPG",
                                "logo": "http://paymentMethodLogo.it",
                                "brand": "VISA",
                                "sessionId": "npgSessionId",
                                "confirmPaymentSessionId": "npgConfirmPaymentSessionId",
                                "walletInfo": {
                                    "walletId": "17601410-5f1d-4189-b8d1-92637952ee5f",
                                    "walletDetails": {
                                        "type": "PAYPAL",
                                        "maskedEmail": "test**********@test********.it"
                                    }
                                }
                            },
                            "pspOnUs": false
                        },
                        "eventCode": "TRANSACTION_AUTHORIZATION_REQUESTED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationRequestedEvent(
                        TransactionAuthorizationRequestData.PaymentGateway.NPG,
                        TransactionTestUtils.npgTransactionGatewayAuthorizationRequestedData(
                                TransactionTestUtils.paypalWalletInfo()
                        )
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        originalEvent.event().getData().setAuthorizationRequestId("d93cb073-445c-476b-b0fd-abe343d8b6a5");
        NpgTransactionGatewayAuthorizationRequestedData authRequestedData = (NpgTransactionGatewayAuthorizationRequestedData) originalEvent
                .event().getData().getTransactionGatewayAuthorizationRequestedData();
        authRequestedData.getWalletInfo().setWalletId("17601410-5f1d-4189-b8d1-92637952ee5f");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundRequestedEventWithoutGatewayData() {
        String expectedSerializedEvent = """
                {
                         "event": {
                             "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent",
                             "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                             "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                             "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                             "data": {
                                 "statusBeforeRefunded": "ACTIVATED",
                                 "gatewayAuthData": null
                             },
                             "eventCode": "TRANSACTION_REFUND_REQUESTED_EVENT"
                         },
                         "tracingInfo": {
                             "traceparent": "mock_traceparent",
                             "tracestate": "mock_tracestate",
                             "baggage": "mock_baggage"
                         }
                     }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundRequestedEvent(
                        TransactionTestUtils.transactionActivated(ZonedDateTime.now().toString()),
                        null
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundRequestedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundRequestedEventWithGatewayData() {
        String expectedSerializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "statusBeforeRefunded": "ACTIVATED",
                            "gatewayAuthData": {
                                "type": "NPG",
                                "operationResult": "EXECUTED",
                                "operationId": "npgOperationId",
                                "paymentEndToEndId": "npgPaymentEndToEndId",
                                "errorCode": null,
                                "validationServiceId": "validationServiceId"
                            }
                        },
                        "eventCode": "TRANSACTION_REFUND_REQUESTED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundRequestedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundRequestedEvent(
                        TransactionTestUtils.transactionActivated(ZonedDateTime.now().toString()),
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRequestedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundRequestedEvent>>() {
                                }
                        )
        )
                .assertNext(deserializedEvent -> assertEquals(originalEvent, deserializedEvent))
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundRetryEventWithoutGatewayData() {
        String expectedSerializedEvent = """
                {
                    "event": {
                        "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRetriedEvent",
                        "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                        "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                        "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                        "data": {
                            "retryCount": 0,
                            "transactionGatewayAuthorizationData": null
                        },
                        "eventCode": "TRANSACTION_REFUND_RETRIED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundRetriedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundRetriedEvent(
                        0,
                        null
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRetriedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundRetriedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundRetryEventWithGatewayData() {
        String expectedSerializedEvent = """
                {
                          "event": {
                              "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRetriedEvent",
                              "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                              "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                              "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                              "data": {
                                  "retryCount": 0,
                                  "transactionGatewayAuthorizationData": {
                                      "type": "NPG",
                                      "operationResult": "EXECUTED",
                                      "operationId": "npgOperationId",
                                      "paymentEndToEndId": "npgPaymentEndToEndId",
                                      "errorCode": null,
                                      "validationServiceId": "validationServiceId"
                                  }
                              },
                              "eventCode": "TRANSACTION_REFUND_RETRIED_EVENT"
                          },
                          "tracingInfo": {
                              "traceparent": "mock_traceparent",
                              "tracestate": "mock_tracestate",
                              "baggage": "mock_baggage"
                          }
                      }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundRetriedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundRetriedEvent(
                        0,
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundRetriedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundRetriedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundedEventWithEmptyGatewayData() {
        String expectedSerializedEvent = """
                {
                           "event": {
                               "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent",
                               "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                               "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                               "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                               "data": {
                                   "statusBeforeRefunded": "ACTIVATED",
                                   "gatewayOperationData": {
                                       "type": "EMPTY"
                                   }
                               },
                               "eventCode": "TRANSACTION_REFUNDED_EVENT"
                           },
                           "tracingInfo": {
                               "traceparent": "mock_traceparent",
                               "tracestate": "mock_tracestate",
                               "baggage": "mock_baggage"
                           }
                       }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundedEvent(
                        TransactionTestUtils.transactionActivated(ZonedDateTime.now().toString()),
                        new EmptyGatewayRefundData()
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueRefundedEventWithNpgData() {
        String expectedSerializedEvent = """
                {
                            "event": {
                                "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent",
                                "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                                "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                                "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                                "data": {
                                    "statusBeforeRefunded": "ACTIVATED",
                                    "gatewayOperationData": {
                                        "type": "NPG",
                                        "operationId": "npgRefundResponseOperationId"
                                    }
                                },
                                "eventCode": "TRANSACTION_REFUNDED_EVENT"
                            },
                            "tracingInfo": {
                                "traceparent": "mock_traceparent",
                                "tracestate": "mock_tracestate",
                                "baggage": "mock_baggage"
                            }
                        }
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionRefundedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionRefundedEvent(
                        TransactionTestUtils.transactionActivated(ZonedDateTime.now().toString()),
                        new NpgGatewayRefundData("npgRefundResponseOperationId")
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionRefundedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionRefundedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueClosureRetriedEvent() {
        String expectedSerializedEvent = """
                {
                             "event": {
                                 "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionClosureRetriedEvent",
                                 "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                                 "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                                 "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                                 "data": {
                                     "retryCount": 0
                                 },
                                 "eventCode": "TRANSACTION_CLOSURE_RETRIED_EVENT"
                             },
                             "tracingInfo": {
                                 "traceparent": "mock_traceparent",
                                 "tracestate": "mock_tracestate",
                                 "baggage": "mock_baggage"
                             }
                         }
                """
                .replace("\n", "").replace(" ", "");
        QueueEvent<TransactionClosureRetriedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionClosureRetriedEvent(
                        0
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionClosureRetriedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionClosureRetriedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueAddUserReceiptRetriedEvent() {
        String expectedSerializedEvent = """
                {
                             "event": {
                                 "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptAddRetriedEvent",
                                 "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                                 "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                                 "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                                 "data": {
                                     "retryCount": 0
                                 },
                                 "eventCode": "TRANSACTION_ADD_USER_RECEIPT_RETRY_EVENT"
                             },
                             "tracingInfo": {
                                 "traceparent": "mock_traceparent",
                                 "tracestate": "mock_tracestate",
                                 "baggage": "mock_baggage"
                             }
                         }
                """
                .replace("\n", "").replace(" ", "");
        QueueEvent<TransactionUserReceiptAddRetriedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionUserReceiptAddRetriedEvent(
                        0
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionUserReceiptAddRetriedEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionUserReceiptAddRetriedEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void canRoundTripQueueTransactionAuthorizationOutcomeWaitingEvent() {
        String expectedSerializedEvent = """
                {
                            "event": {
                                "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationOutcomeWaitingEvent",
                                "id": "0660cd04-db3e-4b7e-858b-e8f75a29ac30",
                                "transactionId": "bdb92a6577fb4aab9bba2ebb80cd8310",
                                "creationDate": "2023-09-25T14:44:31.177776+02:00[Europe/Rome]",
                                "data": {
                                    "retryCount": 0
                                },
                                "eventCode": "TRANSACTION_AUTHORIZATION_OUTCOME_WAITING_EVENT"
                            },
                            "tracingInfo": {
                                "traceparent": "mock_traceparent",
                                "tracestate": "mock_tracestate",
                                "baggage": "mock_baggage"
                            }
                        }
                """
                .replace("\n", "").replace(" ", "");
        QueueEvent<TransactionAuthorizationOutcomeWaitingEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionAuthorizationOutcomeWaitingEvent(
                        0
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
        byte[] serialized = jsonSerializer.serializeToBytes(originalEvent);
        String serializedString = new String(serialized);
        System.out.println("Serialized object: " + serializedString);

        assertTrue(
                serializedString
                        .contains(
                                "\"_class\":\"it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationOutcomeWaitingEvent\""
                        )
        );
        assertEquals(expectedSerializedEvent, serializedString);
        Hooks.onOperatorDebug();
        StepVerifier.create(
                jsonSerializer
                        .deserializeFromBytesAsync(
                                serialized,
                                new TypeReference<QueueEvent<TransactionAuthorizationOutcomeWaitingEvent>>() {
                                }
                        )
        )
                .expectNext(originalEvent)
                .verifyComplete();
    }

    @Test
    void shouldDeserializeEventWithoutCreditorReferenceId() throws IOException {
        String expected = FileUtils.readFileToString(
                new ClassPathResource("events/activation-no-creditor-reference-id.json").getFile(),
                StandardCharsets.UTF_8
        );
        it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent event = new it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent(
                "b753273c789140bf9938df4c50842ef3",
                "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                new it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedData(
                        new Confidential<>("1653f446-18ec-4f83-afc9-36ce7de07398"),
                        List.of(
                                new PaymentNotice(
                                        PAYMENT_TOKEN,
                                        RPT_ID,
                                        DESCRIPTION,
                                        AMOUNT,
                                        PAYMENT_CONTEXT_CODE,
                                        List.of(
                                                new PaymentTransferInformation(
                                                        TRANSFER_PA_FISCAL_CODE,
                                                        TRANSFER_DIGITAL_STAMP,
                                                        TRANSFER_AMOUNT,
                                                        TRANSFER_CATEGORY
                                                )
                                        ),
                                        false,
                                        it.pagopa.ecommerce.commons.v2.TransactionTestUtils.COMPANY_NAME,
                                        null
                                )
                        ),
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        it.pagopa.ecommerce.commons.documents.v2.Transaction.ClientId.CHECKOUT,
                        ID_CART,
                        PAYMENT_TOKEN_VALIDITY_TIME_SEC,
                        new EmptyTransactionGatewayActivationData(),
                        "34a07bc2-4dad-4a9a-a941-86d232829c94"
                )
        );
        event.setId("be09bed4-f0ae-4ef2-8adb-324f720fc702");

        System.out.println(new String(jsonSerializer.serializeToBytes(event), StandardCharsets.UTF_8));
        final var deserializedEvent = jsonSerializer.deserializeFromBytes(
                expected.getBytes(StandardCharsets.UTF_8),
                TypeReference.createInstance(it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent.class)
        );

        assertEquals(event, deserializedEvent);
    }

    @Test
    void shouldDeserializeEventWitCreditorReferenceId() throws IOException {
        String expected = FileUtils.readFileToString(
                new ClassPathResource("events/activation-with-creditor-reference-id.json").getFile(),
                StandardCharsets.UTF_8
        );
        it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent event = new it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent(
                "b753273c789140bf9938df4c50842ef3",
                "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                new it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedData(
                        new Confidential<>("1653f446-18ec-4f83-afc9-36ce7de07398"),
                        List.of(
                                new PaymentNotice(
                                        PAYMENT_TOKEN,
                                        RPT_ID,
                                        DESCRIPTION,
                                        AMOUNT,
                                        PAYMENT_CONTEXT_CODE,
                                        List.of(
                                                new PaymentTransferInformation(
                                                        TRANSFER_PA_FISCAL_CODE,
                                                        TRANSFER_DIGITAL_STAMP,
                                                        TRANSFER_AMOUNT,
                                                        TRANSFER_CATEGORY
                                                )
                                        ),
                                        false,
                                        it.pagopa.ecommerce.commons.v2.TransactionTestUtils.COMPANY_NAME,
                                        "02011915520500107"
                                )
                        ),
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        it.pagopa.ecommerce.commons.documents.v2.Transaction.ClientId.CHECKOUT,
                        ID_CART,
                        PAYMENT_TOKEN_VALIDITY_TIME_SEC,
                        new EmptyTransactionGatewayActivationData(),
                        "34a07bc2-4dad-4a9a-a941-86d232829c94"
                )
        );
        event.setId("be09bed4-f0ae-4ef2-8adb-324f720fc702");

        System.out.println(new String(jsonSerializer.serializeToBytes(event), StandardCharsets.UTF_8));
        final var deserializedEvent = jsonSerializer.deserializeFromBytes(
                expected.getBytes(StandardCharsets.UTF_8),
                TypeReference.createInstance(it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent.class)
        );

        assertEquals(event, deserializedEvent);
    }
}
