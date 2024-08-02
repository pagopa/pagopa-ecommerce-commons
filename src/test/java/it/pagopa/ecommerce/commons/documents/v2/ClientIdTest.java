package it.pagopa.ecommerce.commons.documents.v2;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientIdTest {
    static Stream<Arguments> effectiveClientIdMapping() {
        return Stream.of(
                Arguments.of(Transaction.ClientId.CHECKOUT, Transaction.ClientId.CHECKOUT),
                Arguments.of(Transaction.ClientId.CHECKOUT_CART, Transaction.ClientId.CHECKOUT_CART),
                Arguments.of(Transaction.ClientId.IO, Transaction.ClientId.IO),
                Arguments.of(Transaction.ClientId.WISP_REDIRECT, Transaction.ClientId.CHECKOUT_CART)
        );
    }

    @ParameterizedTest
    @MethodSource("effectiveClientIdMapping")
    void effectiveClientShouldMapClientsCorrectly(
                                                  Transaction.ClientId source,
                                                  Transaction.ClientId expected
    ) {
        Transaction.ClientId actual = source.getEffectiveClient();
        assertEquals(expected, actual);
    }
}
