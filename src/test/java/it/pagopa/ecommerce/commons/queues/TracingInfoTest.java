package it.pagopa.ecommerce.commons.queues;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class TracingInfoTest {
    public static final TracingInfo MOCK_TRACING_INFO = new TracingInfo(
            "mock_traceparent",
            Optional.of("mock_tracestate"),
            Optional.of("mock_baggage")
    );

    @Test
    public void throwsOnNullValues() {
        assertThrows(Exception.class, () -> new TracingInfo(null, Optional.of(""), Optional.of("")));
        assertThrows(Exception.class, () -> new TracingInfo("", null, Optional.of("")));
        assertThrows(Exception.class, () -> new TracingInfo("", Optional.of(""), null));
    }
}
