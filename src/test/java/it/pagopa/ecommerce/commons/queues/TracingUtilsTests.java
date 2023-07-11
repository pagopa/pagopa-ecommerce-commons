package it.pagopa.ecommerce.commons.queues;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static org.mockito.ArgumentMatchers.any;

public class TracingUtilsTests {

    private final OpenTelemetry openTelemetry = GlobalOpenTelemetry.get();

    private final Tracer tracer = openTelemetry.getTracer("test");

    private final TracingUtils tracingUtils = new TracingUtils(openTelemetry, tracer);

    public static TracingUtils getMock() {
        TracingUtils mockedTracingUtils = Mockito.mock(TracingUtils.class);
        Mockito.when(mockedTracingUtils.traceMono(any(), any())).thenAnswer(invocation -> {
            Function<TracingInfo, Mono<?>> arg = invocation.getArgument(1);

            return arg.apply(MOCK_TRACING_INFO);
        });

        Mockito.when(mockedTracingUtils.traceMonoWithRemoteSpan(any(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(2));

        return mockedTracingUtils;
    }

    @Test
    public void traceMonoWithRemoteSpanWithMonoValueReturnsValue() {
        int expected = 0;
        Mono<Integer> operation = Mono.just(expected);

        StepVerifier.create(tracingUtils.traceMonoWithRemoteSpan(MOCK_TRACING_INFO, "test", operation))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    public void traceMonoWithRemoteSpanWithMonoErrorReturnsError() {
        RuntimeException expected = new RuntimeException("error!");
        Mono<Integer> operation = Mono.error(expected);

        StepVerifier.create(tracingUtils.traceMonoWithRemoteSpan(MOCK_TRACING_INFO, "test", operation))
                .expectErrorMatches(e -> e.equals(expected))
                .verify();
    }
}
