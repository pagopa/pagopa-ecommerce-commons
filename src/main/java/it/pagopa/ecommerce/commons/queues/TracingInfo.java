package it.pagopa.ecommerce.commons.queues;

import org.springframework.lang.NonNull;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * Tracing information as documented in
 * <a href="https://w3c.github.io/trace-context/">Trace Context Level 3
 * Spec</a>.
 * </p>
 */
public final class TracingInfo {
    private final String traceparent;

    private final @NonNull Optional<String> tracestate;

    private final @NonNull Optional<String> baggage;

    /**
     * @param traceparent traceparent header – Used to identify the trace
     * @param tracestate  tracestate header – Additional tracing-system-specific
     *                    trace information
     * @param baggage     baggage header – Additional application-specific trace
     *                    information
     */
    TracingInfo(
            @NonNull String traceparent,
            @NonNull Optional<String> tracestate,
            @NonNull Optional<String> baggage
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
    public @NonNull Optional<String> getTracestate() {
        return tracestate;
    }

    /**
     * Getter for {@code baggage}
     *
     * @return value of the baggage header
     */
    public @NonNull Optional<String> getBaggage() {
        return baggage;
    }

    @JsonCreator
    private static TracingInfo deserializer(
                                            @JsonProperty("traceparent") String traceparent,
                                            @JsonProperty("tracestate") Optional<String> tracestate,
                                            @JsonProperty("baggage") Optional<String> baggage
    ) {
        return new TracingInfo(traceparent, tracestate, baggage);
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
