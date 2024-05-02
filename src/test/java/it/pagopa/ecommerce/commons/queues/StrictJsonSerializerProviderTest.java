package it.pagopa.ecommerce.commons.queues;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.documents.v2.activation.EmptyTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static it.pagopa.ecommerce.commons.v1.TransactionTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StrictJsonSerializerProviderTest {
    private final JsonSerializer jsonSerializer = new StrictJsonSerializerProvider().createInstance();

    record Simple(int bar) {
    }

    record SimpleWithObject(String quux) {
    }

    @Test
    void serializeEmptyOptionalAsNull() {
        Optional<Integer> value = Optional.empty();

        String serialized = new String(jsonSerializer.serializeToBytes(value));

        assertEquals("null", serialized);
    }

    @Test
    void serializeValuedOptionalAsValue() {
        int rawValue = 1;
        Optional<Integer> value = Optional.of(rawValue);

        String serialized = new String(jsonSerializer.serializeToBytes(value));

        assertEquals(String.valueOf(rawValue), serialized);
    }

    @Test
    void dontAllowArbitrarySubtypeDeserialization() {
        assertThrows(Exception.class, () -> {
            TransactionEvent<?> event = TransactionTestUtils.transactionActivateEvent();

            byte[] serialized = jsonSerializer.serializeToBytes(event);
            jsonSerializer.deserializeFromBytes(serialized, new TypeReference<TransactionRefundRequestedEvent>() {
            });
        });
    }

    @Test
    void disallowNullPrimitiveProperty() {
        String serializedWithNull = """
                {
                    "bar": null
                }
                """;

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer
                        .deserializeFromBytes(serializedWithNull.getBytes(), TypeReference.createInstance(Simple.class))
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    void disallowMissingPrimitiveProperty() {
        String serializedWithMissingProperty = """
                {
                }
                """;

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithMissingProperty.getBytes(),
                        TypeReference.createInstance(Simple.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    void disallowNullObjectProperty() {
        String serializedWithNull = """
                {
                    "bar": null
                }
                """;

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithNull.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    void disallowMissingObjectProperty() {
        String serializedWithMissingProperty = """
                {
                }
                """;

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithMissingProperty.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    void disallowExtraProperties() {
        String serializedWithExtraProperty = """
                {
                    "quux": "a",
                    "bar": 1
                }
                """;

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithExtraProperty.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(UnrecognizedPropertyException.class, exception.getCause().getClass());
    }

    @Test
    void deserializedV1Correctly() {
        String expected = """
                {
                  "eventCode": "TRANSACTION_ACTIVATED_EVENT",
                  "id": "be09bed4-f0ae-4ef2-8adb-324f720fc702",
                  "transactionId": "b753273c789140bf9938df4c50842ef3",
                  "creationDate": "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                  "data": {
                    "email": {
                      "data": "1653f446-18ec-4f83-afc9-36ce7de07398"
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
                        "companyName": null,
                        "allCCP": false
                      }
                    ],
                    "faultCode": "",
                    "faultCodeString": "",
                    "clientId": "CHECKOUT",
                    "idCart": "ecIdCart",
                    "paymentTokenValiditySeconds": 900
                  },
                  "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                }
                """.replace(" ", "").replace("\n", "");

        TransactionActivatedEvent event = new TransactionActivatedEvent(
                "b753273c789140bf9938df4c50842ef3",
                "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                new TransactionActivatedData(
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
                                        null
                                )
                        ),
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        Transaction.ClientId.CHECKOUT,
                        ID_CART,
                        PAYMENT_TOKEN_VALIDITY_TIME_SEC
                )
        );
        event.setId("be09bed4-f0ae-4ef2-8adb-324f720fc702");

        String serialized = new String(jsonSerializer.serializeToBytes(event), StandardCharsets.UTF_8);

        assertEquals(expected, serialized);
    }

    @Test
    void deserializedV2Correctly() {
        String expected = """
                {
                  "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionActivatedEvent",
                  "id": "be09bed4-f0ae-4ef2-8adb-324f720fc702",
                  "transactionId": "b753273c789140bf9938df4c50842ef3",
                  "creationDate": "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                  "data": {
                    "email": {
                      "data": "1653f446-18ec-4f83-afc9-36ce7de07398"
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
                    "transactionGatewayActivationData": {"type":"EMPTY"},
                    "userId": "34a07bc2-4dad-4a9a-a941-86d232829c94"
                  },
                  "eventCode": "TRANSACTION_ACTIVATED_EVENT"
                }
                """
                .replace(" ", "").replace("\n", "");

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
                                        COMPANY_NAME
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

        String serialized = new String(jsonSerializer.serializeToBytes(event), StandardCharsets.UTF_8);

        assertEquals(expected, serialized);
    }

    @Test
    void v1EventIsNotParsedAsV2() {
        String expected = """
                {
                  "id": "be09bed4-f0ae-4ef2-8adb-324f720fc702",
                  "transactionId": "b753273c789140bf9938df4c50842ef3",
                  "creationDate": "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                  "data": {
                    "statusBeforeExpiration": "AUTHORIZATION_COMPLETED"
                  },
                  "eventCode": "TRANSACTION_EXPIRED_EVENT"
                }
                """.replace(" ", "").replace("\n", "");

        Exception exception = assertThrows(Exception.class, () -> {
            jsonSerializer.deserializeFromBytes(
                    expected.getBytes(StandardCharsets.UTF_8),
                    TypeReference.createInstance(it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent.class)
            );
        });

        assertEquals(InvalidTypeIdException.class, exception.getCause().getClass());
        assertTrue(exception.getCause().getMessage().contains("missing type id property '_class'"));
    }

    @Test
    void v2EventIsNotParsedAsV1() {
        String expected = """
                {
                  "_class": "it.pagopa.ecommerce.commons.documents.v2.TransactionExpiredEvent",
                  "id": "be09bed4-f0ae-4ef2-8adb-324f720fc702",
                  "transactionId": "b753273c789140bf9938df4c50842ef3",
                  "creationDate": "2023-09-22T14:36:44.733455+02:00[Europe/Rome]",
                  "data": {
                    "statusBeforeExpiration": "AUTHORIZATION_COMPLETED"
                  },
                  "eventCode": "TRANSACTION_EXPIRED_EVENT"
                }
                """.replace(" ", "").replace("\n", "");

        Exception exception = assertThrows(Exception.class, () -> {
            jsonSerializer.deserializeFromBytes(
                    expected.getBytes(StandardCharsets.UTF_8),
                    TypeReference.createInstance(it.pagopa.ecommerce.commons.documents.v1.TransactionExpiredEvent.class)
            );
        });

        assertEquals(UnrecognizedPropertyException.class, exception.getCause().getClass());
        assertTrue(exception.getCause().getMessage().contains("Unrecognized field \"_class\""));
    }

}
