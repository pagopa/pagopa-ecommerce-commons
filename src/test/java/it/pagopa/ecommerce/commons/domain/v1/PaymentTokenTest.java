package it.pagopa.ecommerce.commons.domain.v1;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PaymentTokenTest {
    @Test
    void shouldConstructPaymentToken() {
        String rawPaymentToken = "payment_token";
        PaymentToken paymentToken = new PaymentToken(rawPaymentToken);

        assertEquals(paymentToken.value(), rawPaymentToken);
    }
}
