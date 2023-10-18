package it.pagopa.ecommerce.commons.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class NpgClientPaymentMethodTests {

    @Test
    void shouldReturnPaymentMethodEnum() {
        for (NpgClient.PaymentMethod paymentMethod : NpgClient.PaymentMethod.values()) {
            assertNotNull(NpgClient.PaymentMethod.fromServiceName(paymentMethod.serviceName));
        }
        assertThrows(IllegalArgumentException.class, () -> NpgClient.PaymentMethod.fromServiceName("serviceName"));

    }
}
