package it.pagopa.ecommerce.commons.queues;

public class TracingInfoTest {
    public static final TracingInfo MOCK_TRACING_INFO = new TracingInfo(
            "mock_traceparent",
            "mock_tracestate",
            "mock_baggage"
    );
}
