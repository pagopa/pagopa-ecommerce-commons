package it.pagopa.ecommerce.commons.queues;

import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
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
    public void dontAllowArbitrarySubtypeDeserialization() {
        assertThrows(Exception.class, () -> {
            TransactionEvent<?> event = TransactionTestUtils.transactionActivateEvent();

            byte[] serialized = jsonSerializer.serializeToBytes(event);
            jsonSerializer.deserializeFromBytes(serialized, new TypeReference<TransactionRefundRequestedEvent>() {
            });
        });
    }

    @Test
    public void disallowNullPrimitiveProperty() {
        String serializedWithNull = """
                {
                    "bar": null
                }
                """;

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
                }
                """;

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
                    "bar": null
                }
                """;

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
                }
                """;

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
                    "quux": "a",
                    "bar": 1
                }
                """;

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
