package it.pagopa.ecommerce.commons.mdcutilities;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.*;

import lombok.Getter;
import org.slf4j.MDC;
import reactor.util.context.Context;

/**
 * Utility class with helper methods to enrich Reactor Context event processing.
 */
public class LogTracingUtils {

    private static final String CTX_DETAILS_KEY = "ctx.details";
    /** Dependency label used in MDC for MongoDB operations. */
    public static final String MONGO_DEPENDENCY_KEY = "eCommerce-mongodb";
    /** Dependency label used in MDC for Redis operations. */
    public static final String REDIS_DEPENDENCY_KEY = "eCommerce-redis";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Getter
    private static Set<TracingEntry> contextBounded = new HashSet<>();

    /**
     * Adds the provided tracing entries to the set of context-bound keys used by
     * {@link MDCContextLifter}.
     *
     * @param contextBounded tracing entries to be copied from Reactor Context to
     *                       MDC
     */
    public LogTracingUtils(Set<TracingEntry> contextBounded) {
        LogTracingUtils.contextBounded.addAll(contextBounded);
    }

    /** Tracing keys copied from Reactor Context to MDC. */
    public enum TracingEntry {
        /** Reactor context key for transaction identifier. */
        CTX_TRANSACTION_ID("ctx.transaction.id", "{transactionId-not-found}"),
        /** Reactor context key for event code. */
        CTX_EVENT_CODE("ctx.event.code", "{eventCode-not-found}"),
        /** Reactor context key for event identifier. */
        CTX_EVENT_ID("ctx.event.id", "{eventId-not-found}"),
        /** Reactor context key for RPT identifiers. */
        CTX_RPT_IDS("ctx.rpt.ids", "{rptIds-not-found}"),
        /** Reactor context key for payment tokens. */
        CTX_PAYMENT_TOKENS("ctx.payment.tokens", "{paymentTokens-not-found}"),
        /** Reactor context key for user identifier. */
        CTX_USER_ID("ctx.user.id", "{userId-not-found}"),
        /** Reactor context key for X-Forwarded-For value. */
        CTX_FORWARDED_FOR("ctx.forwarded.for", "{forwardedFor-not-found}"),
        /** MDC key for business transaction identifier. */
        TRANSACTION_ID("transaction.id", "{transactionId-not-found}"),
        /** MDC key for business transaction status. */
        TRANSACTION_STATUS("transaction.status", "{transactionStatus-not-found}"),
        /** MDC key for correlation identifier. */
        CORRELATION_ID("correlation.id", "{correlationId-not-found}"),
        /** MDC key for operation identifier. */
        OPERATION_ID("operation.id", "{operationId-not-found}"),
        /** MDC key for response code. */
        RESPONSE_CODE("response.code", "{responseCode-not-found}"),
        /** MDC key for response payload. */
        RESPONSE_BODY("response.body", "{responseBody-not-found}"),
        /** MDC key for PSP identifier. */
        PSP_ID("psp.id", "{pspId-not-found}"),
        /** MDC key for PSP channel code. */
        PSP_CHANNEL_CODE("psp.channel.code", "{pspChannelCode-not-found}"),
        /** MDC key for PSP transaction identifier. */
        PSP_TRANSACTION_ID("psp.transaction.id", "{pspTransactionId-not-found}"),
        /** MDC key for queue event identifier. */
        QUEUE_EVENT_ID("queue.event.id", "{queueEventId-not-found}"),
        /** MDC key for request path. */
        PATH("path", "{path-not-found}"),
        /** Reactor context key for action associated with the event. */
        EVENT_ACTION("event.action", "{eventAction-not-found}"),
        /** Reactor context key for event outcome. */
        EVENT_OUTCOME("event.outcome", "{eventOutcome-not-found}"),
        /** MDC key for dependency name involved in the operation. */
        DEPENDENCY("dependency", "{dependency-not-found}"),
        /** MDC key for error class name. */
        ERROR_TYPE("error.type", "{errorType-not-found}"),
        /** MDC key for error message text. */
        ERROR_MESSAGE("error.message", "{errorMessage-not-found}");

        private final String key;
        private final String defaultValue;

        TracingEntry(
                String key,
                String defaultValue
        ) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        /**
         * Returns the MDC/reactor key name.
         *
         * @return key name
         */
        public String getKey() {
            return key;
        }

        /**
         * Returns the fallback value used when the key is missing.
         *
         * @return default value
         */
        public String getDefaultValue() {
            return defaultValue;
        }

    }

    /**
     * Enrich Reactor Context with tracing entries in a fully generic way.
     *
     * <p>
     * This method accepts a map of {@link TracingEntry} enum keys with their
     * corresponding values. Each entry is added to the context with its value or
     * default value if null. Any future TracingEntry additions are automatically
     * supported without method changes.
     *
     * @param tracingEntries map of TracingEntry to value; null values use defaults
     * @param reactorContext the context to enrich
     * @return enriched context with all tracing entries
     */
    public static Context enrichContextForEvent(
                                                Map<TracingEntry, String> tracingEntries,
                                                Context reactorContext
    ) {
        Context enrichedContext = reactorContext;
        if (tracingEntries != null) {
            for (Map.Entry<TracingEntry, String> entry : tracingEntries.entrySet()) {
                enrichedContext = enrichedContext.put(
                        entry.getKey().getKey(),
                        entry.getValue() != null
                                ? entry.getValue()
                                : entry.getKey().getDefaultValue()
                );
            }
        }
        return enrichedContext;
    }

    /**
     * Executes a block with error attributes ({@code error.type} and
     * {@code error.message}) and an arbitrary map of top-level attributes
     * temporarily stored in MDC.
     *
     * <p>
     * Error attributes are extracted from the provided {@link Throwable}. Top-level
     * attributes are passed to MDC cleanup logic where string conversion is
     * handled. All keys are guaranteed to be removed after block execution.
     *
     * @param error      the exception to extract type and message from (can be
     *                   null)
     * @param attributes map of top-level MDC key-value attributes (can be null)
     * @param block      code to execute while attributes are available in MDC
     */
    public static void withErrorMdc(
                                    Throwable error,
                                    Map<String, ?> attributes,
                                    Runnable block
    ) {
        Map<String, Object> mdcMap = new HashMap<>();

        mdcMap.put(
                TracingEntry.ERROR_TYPE.getKey(),
                error != null
                        ? error.getClass().getName()
                        : TracingEntry.ERROR_TYPE.getDefaultValue()
        );
        mdcMap.put(
                TracingEntry.ERROR_MESSAGE.getKey(),
                error != null && error.getMessage() != null
                        ? error.getMessage()
                        : TracingEntry.ERROR_MESSAGE.getDefaultValue()
        );

        if (attributes != null) {
            attributes.forEach(
                    (
                     key,
                     value
                    ) -> {
                        if (key != null && value != null) {
                            mdcMap.put(key, value);
                        }
                    }
            );
        }

        insertIntoMdcAndCleanup(mdcMap, block);
    }

    /**
     * Executes a block with structured error details temporarily inserted in MDC.
     *
     * <p>
     * The method adds {@code error.type} and {@code error.message} keys, executes
     * the provided block, and always removes those keys afterward.
     *
     * @param error error instance used to populate MDC metadata
     * @param block code to execute while error metadata is available in MDC
     */
    public static void withErrorMdc(
                                    Throwable error,
                                    Runnable block
    ) {
        withErrorMdc(error, null, block);
    }

    /**
     * Executes a block with {@code ctx.details} temporarily stored in MDC as a JSON
     * string.
     *
     * <p>
     * The input map is serialized to raw JSON and stored under key
     * {@code ctx.details}. If serialization fails, an empty JSON object
     * ({@code {}}) is used as fallback. The key is always removed after block
     * execution.
     *
     * @param details map of detail values to serialize under {@code ctx.details}
     * @param block   code to execute while {@code ctx.details} is available in MDC
     */
    public static void withContextDetailsMdc(
                                             Map<String, ?> details,
                                             Runnable block
    ) {
        withContextDetailsMdc(details, null, block);
    }

    /**
     * Executes a block with {@code ctx.details} temporarily stored in MDC as a JSON
     * string.
     *
     * <p>
     * The input map is serialized to raw JSON and stored under key
     * {@code ctx.details}. If serialization fails, an empty JSON object
     * ({@code {}}) is used as fallback. The key is always removed after block
     * execution.
     *
     * @param details    map of detail values to serialize under {@code ctx.details}
     * @param attributes map of top-level MDC key-value attributes (can be null)
     * @param block      code to execute while {@code ctx.details} is available in
     *                   MDC
     */
    public static void withContextDetailsMdc(
                                             Map<String, ?> details,
                                             Map<String, ?> attributes,
                                             Runnable block
    ) {
        Map<String, Object> mdcMap = new HashMap<>();

        String rawDetails = "{}";
        if (details != null) {
            try {
                rawDetails = OBJECT_MAPPER.writeValueAsString(details);
            } catch (JsonProcessingException ignored) {
                rawDetails = "{}";
            }
        }
        mdcMap.put(CTX_DETAILS_KEY, rawDetails);

        if (attributes != null) {
            attributes.forEach(
                    (
                     k,
                     v
                    ) -> {
                        if (k != null && v != null) {
                            mdcMap.put(k, v);
                        }
                    }
            );
        }

        insertIntoMdcAndCleanup(mdcMap, block);
    }

    /**
     * Inserts the provided entries into MDC, executes the given block, and always
     * removes the inserted keys afterward.
     *
     * <p>
     * This method guarantees MDC cleanup through a {@code finally} block, so
     * temporary values do not leak across log statements or threads.
     *
     * @param entries key/value pairs to temporarily add to MDC
     * @param block   code to execute while MDC entries are available
     */
    private static void insertIntoMdcAndCleanup(
                                                Map<String, ?> entries,
                                                Runnable block
    ) {
        List<String> detailKeys = new ArrayList<>();

        try {
            if (entries != null) {
                entries.forEach(
                        (
                         key,
                         value
                        ) -> {
                            if (key != null && value != null) {
                                MDC.put(key, value.toString());
                                detailKeys.add(key);
                            }
                        }
                );
            }
            block.run();
        } finally {
            detailKeys.forEach(MDC::remove);
        }
    }
}
