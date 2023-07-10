package it.pagopa.ecommerce.commons.queues;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

/**
 * <p>
 * Tracing utilities to wrap {@link Mono}s with manual OpenTelemetry
 * instrumentation.
 * </p>
 */
public class TracingUtils {
    private static final Logger logger = LoggerFactory.getLogger(TracingUtils.class);

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    /**
     * Primary constructor
     *
     * @param openTelemetry OpenTelemetry API entrypoint
     * @param tracer        Tracer from which to create spans
     */
    public TracingUtils(
            OpenTelemetry openTelemetry,
            Tracer tracer
    ) {
        this.tracer = tracer;
        this.openTelemetry = openTelemetry;
    }

    /**
     * Tracing context to propagate span information
     *
     * @param span        span to propagate
     * @param tracingInfo tracing information used to propagate current span context
     */
    private record TracingContext(
            @NonNull Span span,
            @NonNull TracingInfo tracingInfo
    ) {
    }

    /**
     * <p>
     * Wraps a {@link Mono} with a {@link Span} with name {@code spanName}. The new
     * span is created as a child of the current span.
     * </p>
     * <p>
     * This method allows creation of
     * {@link it.pagopa.ecommerce.commons.queues.QueueEvent QueueEvents} for
     * propagating tracing information.
     * </p>
     *
     * @param spanName name of the new {@link Span}
     * @param traced   function returning the {@link Mono} to be wrapped
     * @return a new {@link Mono} that executes the wrapped {@link Mono} inside a
     *         new child span
     * @param <T> type parameter of {@link Mono}
     */
    public <T> Mono<T> traceMono(
                                 String spanName,
                                 Function<TracingInfo, Mono<T>> traced
    ) {
        return Mono.using(
                () -> {
                    Span span = tracer.spanBuilder(spanName)
                            .setSpanKind(SpanKind.PRODUCER)
                            .setParent(Context.current().with(Span.current()))
                            .startSpan();

                    HashMap<String, String> rawTracingInfo = new HashMap<>();
                    openTelemetry.getPropagators().getTextMapPropagator().inject(
                            Context.current(),
                            rawTracingInfo,
                            (
                             map,
                             header,
                             value
                            ) -> map.put(header, value)
                    );

                    logger.debug("Raw tracing info: {}", rawTracingInfo);

                    TracingInfo tracingInfo = new TracingInfo(
                            rawTracingInfo.get("traceparent"),
                            Optional.ofNullable(rawTracingInfo.get("tracestate")),
                            Optional.ofNullable(rawTracingInfo.get("baggage"))
                    );

                    return new TracingContext(span, tracingInfo);
                },
                tracingContext -> traced.apply(tracingContext.tracingInfo),
                tracingContext -> tracingContext.span.end()
        );
    }
}
