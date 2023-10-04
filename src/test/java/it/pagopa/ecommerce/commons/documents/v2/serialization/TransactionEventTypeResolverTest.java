package it.pagopa.ecommerce.commons.documents.v2.serialization;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.documents.v2.activation.EmptyTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.activation.NpgTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.NpgTransactionGatewayAuthorizationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.NpgTransactionGatewayAuthorizationRequestedData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.PgsTransactionGatewayAuthorizationData;
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
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
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

    @Test
    void canRoundTripQueueTransactionActivatedEventSerializationWithEmptyActivationData() {
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
                """.replace("\n", "").replace(" ", "");
        QueueEvent<TransactionActivatedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(new EmptyTransactionGatewayActivationData()),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().getData().setEmail(new Confidential<>("a91a4e54-d1bc-48fb-b252-cc2893fc88e2"));
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
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
    void canRoundTripQueueTransactionActivatedEventSerializationWithNpgActivationData() {
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
                                "orderId": "orderId",
                                "correlationId": "correlationId",
                                "sessionId": "sessionId"
                            }
                        },
                        "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                    },
                    "tracingInfo": {
                        "traceparent": "mock_traceparent",
                        "tracestate": "mock_tracestate",
                        "baggage": "mock_baggage"
                    }
                }""".replace("\n", "").replace(" ", "");
        QueueEvent<TransactionActivatedEvent> originalEvent = new QueueEvent<>(
                TransactionTestUtils.transactionActivateEvent(
                        new NpgTransactionGatewayActivationData("orderId", "correlationId", "sessionId")
                ),
                MOCK_TRACING_INFO
        );
        originalEvent.event().setTransactionId("bdb92a6577fb4aab9bba2ebb80cd8310");
        originalEvent.event().setId("0660cd04-db3e-4b7e-858b-e8f75a29ac30");
        originalEvent.event().getData().setEmail(new Confidential<>("a91a4e54-d1bc-48fb-b252-cc2893fc88e2"));
        originalEvent.event().setCreationDate("2023-09-25T14:44:31.177776+02:00[Europe/Rome]");
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
                                   "paymentEndToEndId": "paymentEndToEndId"
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
                                "paymentEndToEndId"
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
                              "paymentTypeCode": "paymentTypeCode",
                              "brokerName": "brokerName",
                              "pspChannelCode": "pspChannelCode",
                              "paymentMethodName": "paymentMethodName",
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
                               "paymentTypeCode": "paymentTypeCode",
                               "brokerName": "brokerName",
                               "pspChannelCode": "pspChannelCode",
                               "paymentMethodName": "paymentMethodName",
                               "pspBusinessName": "pspBusinessName",
                               "authorizationRequestId": "d93cb073-445c-476b-b0fd-abe343d8b6a5",
                               "paymentGateway": "VPOS",
                               "paymentMethodDescription": "paymentMethodDescription",
                               "transactionGatewayAuthorizationRequestedData": {
                                   "type": "NPG",
                                   "logo":"http://paymentMethodLogo.it",
                                   "brand":"VISA"
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
                        new NpgTransactionGatewayAuthorizationRequestedData(
                                TransactionTestUtils.LOGO_URI,
                                "VISA"
                        )
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
}
