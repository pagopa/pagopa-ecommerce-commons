package it.pagopa.ecommerce.commons.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BINTest {
    static Stream<Arguments> validArguments() {
        return Stream.of(
                Arguments.of("1234"),
                Arguments.of("12345"),
                Arguments.of("123456"),
                Arguments.of("1234567"),
                Arguments.of("12345678")
        );
    }

    @ParameterizedTest
    @MethodSource("validArguments")
    void canConstructValidBIN(String value) {
        assertDoesNotThrow(() -> new BIN(value));
    }

    static Stream<Arguments> invalidArguments() {
        return Stream.of(
                Arguments.of("123"),
                Arguments.of("123456789"),
                Arguments.of("1234a")
        );
    }

    @ParameterizedTest
    @NullSource
    @MethodSource("invalidArguments")
    void throwsOnInvalidValue(String value) {
        assertThrows(Exception.class, () -> new BIN(value));
    }
}
