package it.pagopa.ecommerce.commons.domain.v2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CardLastFourDigitsTest {
    @Test
    void canConstructValidCardLastFourDigits() {
        String value = "0000";
        assertDoesNotThrow(() -> new CardLastFourDigits(value));
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of("12345"),
                Arguments.of("123"),
                Arguments.of("123a")
        );
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("invalidArguments")
    void throwsOnInvalidValue(String value) {
        assertThrows(Exception.class, () -> new CardLastFourDigits(value));
    }
}
