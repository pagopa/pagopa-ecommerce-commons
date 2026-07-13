package it.pagopa.ecommerce.commons.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class NpgClientPaymentMethodTests {

    @Test
    void shouldReturnPaymentMethodEnum() {
        for (NpgClient.PaymentMethod paymentMethod : NpgClient.PaymentMethod.values()) {
            assertNotNull(NpgClient.PaymentMethod.fromServiceName(paymentMethod.serviceName));
        }
        assertThrows(IllegalArgumentException.class, () -> NpgClient.PaymentMethod.fromServiceName("serviceName"));

    }

    @Test
    void shouldReturnPaymentMethodFromMethodTypeCode() {
        assertEquals(NpgClient.PaymentMethod.CARDS, NpgClient.PaymentMethod.fromMethodTypeCode("CP"));
        assertEquals(NpgClient.PaymentMethod.PAYPAL, NpgClient.PaymentMethod.fromMethodTypeCode("PPAL"));
        assertEquals(NpgClient.PaymentMethod.MYBANK, NpgClient.PaymentMethod.fromMethodTypeCode("MYBK"));
        assertEquals(NpgClient.PaymentMethod.GOOGLEPAY, NpgClient.PaymentMethod.fromMethodTypeCode("GOOG"));
        assertEquals(NpgClient.PaymentMethod.APPLEPAY, NpgClient.PaymentMethod.fromMethodTypeCode("APPL"));
        assertEquals(NpgClient.PaymentMethod.BANCOMATPAY, NpgClient.PaymentMethod.fromMethodTypeCode("BPAY"));
        assertEquals(NpgClient.PaymentMethod.SATISPAY, NpgClient.PaymentMethod.fromMethodTypeCode("SATY"));
    }

    @Test
    void shouldThrowExceptionForInvalidMethodTypeCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> NpgClient.PaymentMethod.fromMethodTypeCode("INVALID")
        );
        assertTrue(exception.getMessage().contains("INVALID"));
    }

    @Test
    void shouldThrowExceptionForNullMethodTypeCode() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> NpgClient.PaymentMethod.fromMethodTypeCode(null)
        );
        assertEquals("methodTypeCode must not be null", exception.getMessage());
    }
}
