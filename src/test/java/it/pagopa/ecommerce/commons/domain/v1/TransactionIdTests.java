package it.pagopa.ecommerce.commons.domain.v1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionIdTests {


    @ParameterizedTest
    @ValueSource(strings = {"", "transactionIdtransactionIdtransa"})
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
}
