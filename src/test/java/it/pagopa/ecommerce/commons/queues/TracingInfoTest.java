package it.pagopa.ecommerce.commons.queues;

import java.util.Optional;

public class TracingInfoTest {
    public static final TracingInfo MOCK_TRACING_INFO = new TracingInfo(
            "mock_traceparent",
            Optional.of("mock_tracestate"),
            Optional.of("mock_baggage")
    );
}
