package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.client.NpgClient;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.stream.Stream;

import static it.pagopa.ecommerce.commons.utils.NpgClientUtils.getPaymentEndToEndId;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NpgClientUtilsTest {

    private static Stream<Arguments> paymentEndToEndIdTestCase() {
        return Stream.of(
                Arguments.of(
                        NpgClient.PaymentMethod.BANCOMATPAY,
                        Map.of(NpgClientUtils.EndToEndId.BANCOMAT_PAY.value, "bpayEndToEndId"),
                        "bpayEndToEndId"
                ),
                Arguments.of(
                        NpgClient.PaymentMethod.BANCOMATPAY,
                        null,
                        "paymentEndToEndId"
                ),
                Arguments.of(
                        NpgClient.PaymentMethod.MYBANK,
                        Map.of(NpgClientUtils.EndToEndId.MYBANK.value, "myBankEndToEndId"),
                        "myBankEndToEndId"
                ),
                Arguments.of(
                        NpgClient.PaymentMethod.MYBANK,
                        null,
                        "paymentEndToEndId"
                ),
                Arguments.of(
                        NpgClient.PaymentMethod.CARDS,
                        Map.of(),
                        "paymentEndToEndId"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("paymentEndToEndIdTestCase")
    public void shouldGetPaymentEndToEndId(
                                           NpgClient.PaymentMethod paymentMethod,
                                           Map<String, Object> additionalData,
                                           String expectedPaymentId
    ) {
        OperationDto operationDto = mock(OperationDto.class);
        if (additionalData == null || additionalData.isEmpty())
            when(operationDto.getPaymentEndToEndId()).thenReturn(expectedPaymentId);

        when(operationDto.getPaymentCircuit()).thenReturn(paymentMethod.serviceName);
        when(operationDto.getAdditionalData()).thenReturn(additionalData);
        assertEquals(expectedPaymentId, getPaymentEndToEndId(operationDto));

    }

    @Test
    public void shouldReturnNullForGetPaymentEndToEndIdWithOperationNull() {
        assertNull(getPaymentEndToEndId(null));
    }

    @Test
    public void shouldReturnNullForGetPaymentEndToEndIdWithPaymentCircuitNull() {
        OperationDto operationDto = mock(OperationDto.class);
        when(operationDto.getPaymentCircuit()).thenReturn(null);
        assertNull(getPaymentEndToEndId(operationDto));
    }

    @Test
    void testConstructorIsPrivate()
            throws NoSuchMethodException {
        Constructor<NpgClientUtils> constructor = NpgClientUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }
}
