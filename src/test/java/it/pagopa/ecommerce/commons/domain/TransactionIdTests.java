package it.pagopa.ecommerce.commons.domain;

import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdTests {

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "",
                    "transactionIdtransactionIdtransa"
            }
    )
    @NullSource
    void shouldFailCreateTransactionIdForInvalidInput(String transactionId) {
        assertThrows(IllegalArgumentException.class, () -> new TransactionId(transactionId));
    }

    @Test
    void shouldCreateTransactionIdSuccessfully() {
        UUID uuid = UUID.randomUUID();
        String trimmedUUID = uuid.toString().replace("-", "");
        TransactionId transactionId = new TransactionId(trimmedUUID);
        assertEquals(uuid, transactionId.uuid());
        assertEquals(trimmedUUID, transactionId.value());
    }

    @Test
    void shouldEncodeAndDecodeBase64OfUUID() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());

        String base64 = transactionId.base64();
        Either<IllegalArgumentException, TransactionId> decodedTransactionId = TransactionId.fromBase64(base64);

        assertEquals(transactionId, decodedTransactionId.get());
    }

    @Test
    void shouldDecodeBase64OfUUIDError() {
        String wrongUuid = "xxxx";
        Either<IllegalArgumentException, TransactionId> uuidFromBase64 = TransactionId.fromBase64(wrongUuid);
        assertTrue(uuidFromBase64.isLeft());
        assertEquals("Error while decoding transactionId", uuidFromBase64.getLeft().getMessage());
    }
}
