package it.pagopa.ecommerce.commons.queues;

import org.springframework.lang.NonNull;

import java.util.Objects;

;

/**
 * <p>
 * Tracing information as documented in
 * <a href="https://w3c.github.io/trace-context/">Trace Context Level 3
 * Spec</a>.
 * </p>
 */
public final class TracingInfo {
    private final String traceparent;
    private final @NonNull String tracestate;
    private final @NonNull String baggage;

    /**
     * @param traceparent traceparent header – Used to identify the trace
     * @param tracestate  tracestate header – Additional tracing-system-specific
     *                    trace information
     * @param baggage     baggage header – Additional application-specific trace
     *                    information
     */
    TracingInfo(
            @NonNull String traceparent,
            @NonNull String tracestate,
            @NonNull String baggage
    ) {
        this.traceparent = Objects.requireNonNull(traceparent);
        this.tracestate = Objects.requireNonNull(tracestate);
        this.baggage = Objects.requireNonNull(baggage);
    }

    /**
     * Getter for {@code traceparent}
     *
     * @return value of the traceparent header
     */
    public @NonNull String getTraceparent() {
        return traceparent;
    }

    /**
     * Getter for {@code tracestate}
     *
     * @return value of the tracestate header
     */
    public @NonNull String getTracestate() {
        return tracestate;
    }

    /**
     * Getter for {@code baggage}
     *
     * @return value of the baggage header
     */
    public @NonNull String getBaggage() {
        return baggage;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        var that = (TracingInfo) obj;
        return Objects.equals(this.traceparent, that.traceparent) &&
                Objects.equals(this.tracestate, that.tracestate) &&
                Objects.equals(this.baggage, that.baggage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceparent, tracestate, baggage);
    }

    @Override
    public String toString() {
        return "TracingInfo[" +
                "traceparent=" + traceparent + ", " +
                "tracestate=" + tracestate + ", " +
                "baggage=" + baggage + ']';
    }

}
