package it.pagopa.ecommerce.commons.queues;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * <p>
 * Tracing utilities to wrap {@link Mono}s with manual OpenTelemetry
 * instrumentation.
 * </p>
 */
public class TracingUtils {
    /**
     * Constant for traceparent header name
     */
    public static final String TRACEPARENT = "traceparent";

    /**
     * Constant for tracestate header name
     */
    public static final String TRACESTATE = "tracestate";

    /**
     * Constant for baggage header name
     */
    public static final String BAGGAGE = "baggage";

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
            @NonNull OpenTelemetry openTelemetry,
            @NonNull Tracer tracer
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
     * Wraps a {@link Mono} with a new {@link Span} with name {@code spanName}. The
     * new span is created as a child of the current span.
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
                                 @NonNull String spanName,
                                 @NonNull Function<TracingInfo, Mono<T>> traced
    ) {
        /*
         * Here we don't write the span to Reactor's Context because we assume that the
         * caller is already set up with correct span propagation (otherwise context
         * extraction wouldn't work). Under these conditions it is not necessary to
         * write to Reactor's Context after starting a new span.
         *
         * (See `traceMonoWithSpan`)
         */
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
                            rawTracingInfo.get(TRACEPARENT),
                            Optional.ofNullable(rawTracingInfo.get(TRACESTATE)),
                            Optional.ofNullable(rawTracingInfo.get(BAGGAGE))
                    );

                    return new TracingContext(span, tracingInfo);
                },
                tracingContext -> traced.apply(tracingContext.tracingInfo),
                tracingContext -> tracingContext.span.end()
        );
    }

    /**
     * <p>
     * Wraps a {@link Mono} with a new {@link Span} with name {@code spanName}. The
     * new span is created with a link to a remote span whose tracing data is
     * extracted from {@code tracingInfo}.
     * </p>
     *
     * @param tracingInfo tracing info to reconstruct the remote span to be linked
     *                    to
     * @param spanName    name of the new span
     * @param operation   {@link Mono} that will be wrapped
     * @return a new {@link Mono} with a span linked to the remote span identified
     *         by {@code tracingInfo}
     * @param <T> type parameter for the original {@link Mono}
     */
    public <T> @NonNull Mono<T> traceMonoWithRemoteSpan(
                                                        @NonNull TracingInfo tracingInfo,
                                                        @NonNull String spanName,
                                                        @NonNull Mono<T> operation
    ) {
        Span span = createSpanWithRemoteTracingContext(tracingInfo, spanName);

        return traceMonoWithSpan(span, operation);
    }

    private @NonNull Span createSpanWithRemoteTracingContext(
                                                             @NonNull TracingInfo tracingInfo,
                                                             @NonNull String spanName
    ) {
        logger.debug("Creating Span with remote tracing context: {}", tracingInfo);

        Context extractedContext = openTelemetry.getPropagators().getTextMapPropagator().extract(
                Context.current(),
                tracingInfo,
                new TextMapGetter<>() {
                    @Override
                    public Iterable<String> keys(@Nonnull TracingInfo tracingInfo) {
                        return Set.of(TRACEPARENT, TRACESTATE, BAGGAGE);
                    }

                    @Nullable
                    @Override
                    public String get(
                                      @Nullable TracingInfo tracingInfo,
                                      @Nonnull String key
                    ) {
                        return switch (key) {
                            case TRACEPARENT -> Optional.ofNullable(tracingInfo).map(TracingInfo::getTraceparent)
                                    .orElse(null);
                            case TRACESTATE -> Optional.ofNullable(tracingInfo).flatMap(TracingInfo::getTracestate)
                                    .orElse(null);
                            case BAGGAGE -> Optional.ofNullable(tracingInfo).flatMap(TracingInfo::getBaggage)
                                    .orElse(null);
                            default -> null;
                        };
                    }
                }
        );

        return tracer
                .spanBuilder(spanName)
                .setSpanKind(SpanKind.CONSUMER)
                .addLink(Span.fromContext(extractedContext).getSpanContext())
                .startSpan();
    }

    private <T> @NonNull Mono<T> traceMonoWithSpan(
                                                   @NonNull Span span,
                                                   @NonNull Mono<T> operation
    ) {
        /* @formatter:off
         *
         * Writing to `PARENT_TRACE_CONTEXT_KEY` is necessary when instrumenting
         * manually to propagate the tracing context through Azure SDK.
         * (See `traceMono`)
         *
         * See: https://learn.microsoft.com/en-us/java/api/overview/azure/core-tracing-opentelemetry-readme?view=azure-java-preview#asynchronous-clients
         *
         * @formatter:on
         */
        return Mono.using(
                () -> span,
                s -> operation.contextWrite(
                        reactor.util.context.Context.of(PARENT_TRACE_CONTEXT_KEY, Context.current().with(s))
                ),
                Span::end
        );
    }
}
