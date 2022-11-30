package it.pagopa.ecommerce.commons.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TransactionAmountTest {

    @Test
    void shouldConstructTransactionAmount() {
        int rawTransactionAmount = 100;
        TransactionAmount amount = new TransactionAmount(rawTransactionAmount);

        assertEquals(rawTransactionAmount, amount.value());
    }
}
