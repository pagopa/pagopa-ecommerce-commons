package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.domain.v1.PaymentToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaymentTokenTest {
    @Test
    void shouldConstructPaymentToken() {
        String rawPaymentToken = "payment_token";
        PaymentToken paymentToken = new PaymentToken(rawPaymentToken);

        assertEquals(paymentToken.value(), rawPaymentToken);
    }
}
