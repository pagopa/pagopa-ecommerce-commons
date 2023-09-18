package it.pagopa.ecommerce.commons.domain.v2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransactionAmountTest {

    @Test
    void shouldConstructTransactionAmount() {
        int rawTransactionAmount = 100;
        TransactionAmount amount = new TransactionAmount(rawTransactionAmount);

        assertEquals(rawTransactionAmount, amount.value());
    }
}
