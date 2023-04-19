package it.pagopa.ecommerce.commons.domain.v1;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionIdTests {

    @Test
    void shouldFailCreateTransactionIdFromTooShortString() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionId(""));
    }

    @Test
    void shouldFailCreateTransactionIdFromInvalidUUIDString() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionId("transactionIdtransactionIdtransa"));
    }

    @Test
    void shouldFailCreateTransactionIdFromNullUUIDString() {
        assertThrows(IllegalArgumentException.class, () -> new TransactionId((String) null));
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
