package it.pagopa.ecommerce.commons.domain.v1;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IdempotencyKeyTest {
    private final String VALID_FISCAL_CODE = "32009090901";
    private final String INVALID_FISCAL_CODE = "3200909090";

    private final String VALID_KEY_ID = "aabbccddee";
    private final String INVALID_KEY_ID = "aabbccddeeffgg";

    @Test
    void shouldThrowInvalidFiscalCode() {
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IdempotencyKey(INVALID_FISCAL_CODE, VALID_KEY_ID)
        );

        String expectedMessage = "PSP fiscal code doesn't match regex";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldThrowInvalidKeyId() {
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IdempotencyKey(VALID_FISCAL_CODE, INVALID_KEY_ID)
        );

        String expectedMessage = "Key identifier doesn't match regex";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldReturnKey() {
        IdempotencyKey key = new IdempotencyKey(VALID_FISCAL_CODE, VALID_KEY_ID);

        assertTrue(key.rawValue().equalsIgnoreCase(VALID_FISCAL_CODE + "_" + VALID_KEY_ID));
    }

    @Test
    void shouldReturnKeyWithPersistenceConstructor() {
        IdempotencyKey key = new IdempotencyKey(VALID_FISCAL_CODE + "_" + VALID_KEY_ID);

        assertTrue(key.rawValue().equalsIgnoreCase(VALID_FISCAL_CODE + "_" + VALID_KEY_ID));
    }

    @Test
    void shouldThrowInvalidIdempotencyKeyWithPersistenceConstructor() {
        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> new IdempotencyKey(VALID_FISCAL_CODE + "_" + INVALID_KEY_ID)
        );

        String expectedMessage = "Key identifier doesn't match regex";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void shouldThrowMalformedIdempotencyKeyWithPersistenceConstructor() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new IdempotencyKey(VALID_FISCAL_CODE + "_" + INVALID_KEY_ID + "_" + "aaaa")
        );
    }
}
