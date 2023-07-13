package it.pagopa.ecommerce.commons.queues;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import it.pagopa.ecommerce.commons.documents.v1.TransactionEvent;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class StrictJsonSerializerProviderTest {
    private final JsonSerializer jsonSerializer = new StrictJsonSerializerProvider().createInstance();

    record Simple(int bar) {
    }

    record SimpleWithObject(String quux) {
    }

    @Test
    public void serializeEmptyOptionalAsNull() {
        Optional<Integer> value = Optional.empty();

        String serialized = new String(jsonSerializer.serializeToBytes(value));

        assertEquals("null", serialized);
    }

    @Test
    public void serializeValuedOptionalAsValue() {
        int rawValue = 1;
        Optional<Integer> value = Optional.of(rawValue);

        String serialized = new String(jsonSerializer.serializeToBytes(value));

        assertEquals(String.valueOf(rawValue), serialized);
    }

    @Test
    public void serializeObjectWithTypeId() {
        String classCanonicalName = Simple.class.getName();
        String serialized = new String(jsonSerializer.serializeToBytes(new Simple(0)));

        String expectedSerialized = """
                {
                    "@class": "%s",
                    "bar": 0
                }
                """
                .formatted(classCanonicalName)
                .replace(" ", "")
                .replace("\n", "");

        assertEquals(expectedSerialized, serialized);
    }

    @Test
    public void dontAllowArbitrarySubtypeDeserialization() {
        Exception exception = assertThrows(Exception.class, () -> {
            TransactionEvent<?> event = TransactionTestUtils.transactionActivateEvent();

            byte[] serialized = jsonSerializer.serializeToBytes(event);
            jsonSerializer.deserializeFromBytes(serialized, new TypeReference<>() {
            });
        });

        assertEquals(InvalidTypeIdException.class, exception.getCause().getClass());
    }

    @Test
    public void disallowNullPrimitiveProperty() {
        String serializedWithNull = """
                {
                    "@class": "%s",
                    "bar": null
                }
                """.formatted(Simple.class.getName());

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer
                        .deserializeFromBytes(serializedWithNull.getBytes(), TypeReference.createInstance(Simple.class))
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    public void disallowMissingPrimitiveProperty() {
        String serializedWithMissingProperty = """
                {
                    "@class": "%s"
                }
                """.formatted(Simple.class.getName());

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithMissingProperty.getBytes(),
                        TypeReference.createInstance(Simple.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    public void disallowNullObjectProperty() {
        String serializedWithNull = """
                {
                    "@class": "%s",
                    "bar": null
                }
                """.formatted(SimpleWithObject.class.getName());

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithNull.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    public void disallowMissingObjectProperty() {
        String serializedWithMissingProperty = """
                {
                    "@class": "%s"
                }
                """.formatted(SimpleWithObject.class.getName());

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithMissingProperty.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(MismatchedInputException.class, exception.getCause().getClass());
    }

    @Test
    public void disallowExtraProperties() {
        String serializedWithExtraProperty = """
                {
                    "@class": "%s",
                    "quux": "a",
                    "bar": 1
                }
                """.formatted(SimpleWithObject.class.getName());

        Exception exception = assertThrows(
                Exception.class,
                () -> jsonSerializer.deserializeFromBytes(
                        serializedWithExtraProperty.getBytes(),
                        TypeReference.createInstance(SimpleWithObject.class)
                )
        );

        assertEquals(UnrecognizedPropertyException.class, exception.getCause().getClass());
    }
}
