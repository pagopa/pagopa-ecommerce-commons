package it.pagopa.ecommerce.commons.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TransactionDescriptionTest {

    @Test
    void shouldConstructTransactionDescription() {
        String rawDescription = "description";
        TransactionDescription transactionDescription = new TransactionDescription(rawDescription);

        assertEquals(rawDescription, transactionDescription.value());
    }
}
