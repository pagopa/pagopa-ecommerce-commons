package it.pagopa.ecommerce.commons.queues;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.function.Function;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static it.pagopa.ecommerce.commons.queues.TracingUtils.TRACEPARENT;
import static org.mockito.ArgumentMatchers.any;

public class TracingUtilsTests {

    private final OpenTelemetry openTelemetry = Mockito.spy(GlobalOpenTelemetry.get());

    private final Tracer tracer = openTelemetry.getTracer("test");

    private final TracingUtils tracingUtils = new TracingUtils(openTelemetry, tracer);

    @BeforeEach
    void setUpOpenTelemetryMocks() {
        TextMapPropagator textMapPropagator = Mockito.spy(W3CTraceContextPropagator.getInstance());

        Mockito.doAnswer(invocation -> {
            HashMap<String, String> map = invocation.getArgument(1);
            TextMapSetter<HashMap<String, String>> setter = invocation.getArgument(2);

            setter.set(map, TRACEPARENT, "mock_traceparent");
            return null;
        }).when(textMapPropagator).inject(any(), any(HashMap.class), any());

        ContextPropagators contextPropagators = Mockito.mock(ContextPropagators.class);
        Mockito.when(contextPropagators.getTextMapPropagator()).thenReturn(textMapPropagator);

        Mockito.when(openTelemetry.getPropagators()).thenReturn(contextPropagators);
    }

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
    void traceMonoWithRemoteSpanWithMonoValueReturnsValue() {
        int expected = 0;
        Mono<Integer> operation = Mono.just(expected);

        StepVerifier.create(tracingUtils.traceMonoWithRemoteSpan(MOCK_TRACING_INFO, "test", operation))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void traceMonoWithRemoteSpanWithMonoErrorReturnsError() {
        RuntimeException expected = new RuntimeException("error!");
        Mono<Integer> operation = Mono.error(expected);

        StepVerifier.create(tracingUtils.traceMonoWithRemoteSpan(MOCK_TRACING_INFO, "test", operation))
                .expectErrorMatches(e -> e.equals(expected))
                .verify();
    }

    @Test
    void traceMonoWithMonoValueReturnsValue() {
        int expected = 0;
        Mono<Integer> operation = Mono.just(expected);

        StepVerifier.create(tracingUtils.traceMono("test", (tracingInfo -> operation)))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void traceMonoWithMonoErrorReturnsError() {
        RuntimeException expected = new RuntimeException("error!");
        Mono<Integer> operation = Mono.error(expected);

        StepVerifier.create(tracingUtils.traceMono("test", (tracingInfo -> operation)))
                .expectErrorMatches(e -> e.equals(expected))
                .verify();
    }
}
