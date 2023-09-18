package it.pagopa.ecommerce.commons.domain.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionDescriptionTest {

    @Test
    void shouldConstructTransactionDescription() {
        String rawDescription = "description";
        TransactionDescription transactionDescription = new TransactionDescription(rawDescription);

        assertEquals(rawDescription, transactionDescription.value());
    }
}
