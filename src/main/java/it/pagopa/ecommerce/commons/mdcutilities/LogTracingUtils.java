package it.pagopa.ecommerce.commons.mdcutilities;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import it.pagopa.ecommerce.commons.exceptions.LogTracingUtilException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import reactor.util.context.Context;

/**
 * Utility class for structured logging utilizing the Fluent Builder pattern.
 * <p>
 * This class facilitates the population of the SLF4J Mapped Diagnostic Context
 * (MDC) with predefined attributes, custom details, and error information. It
 * ensures that MDC keys are safely added before logging and properly cleaned up
 * immediately after the log is emitted, preventing context leaks in concurrent
 * environments.
 * </p>
 */
public class LogTracingUtils {
    private String outcome;
    private String message;
    private Throwable error;
    private String stackTrace;
    private final Map<AttributeKeys, String> attributes = new EnumMap<>(AttributeKeys.class);
    private final Map<String, String> details = new HashMap<>();
    private Logger logger;

    private final List<String> mdcKeys = new ArrayList<>();

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /** Dependency value used in MDC for MongoDB operations. */
    public static final String MONGO_DEPENDENCY = "eCommerce-mongodb";
    /** Dependency value used in MDC for Redis operations. */
    public static final String REDIS_DEPENDENCY = "eCommerce-redis";
    /** Dependency value used in MDC for storage queue operations. */
    public static final String STORAGE_QUEUE_DEPENDENCY = "storage-queue";
    /** Dependency value used in MDC for NPG operations. */
    public static final String NPG_DEPENDENCY = "npg";
    /** Dependency value used in MDC for NODO operations. */
    public static final String NODO_DEPENDENCY = "nodo";

    /**
     * Enumeration of standard public keys used for MDC and Reactor context
     * population.
     */
    @Getter
    public enum AttributeKeys {
        /** Reactor context key for action associated with the event. */
        EVENT_ACTION("event.action", "{eventAction-not-found}"),
        /** Reactor context key for transaction identifier. */
        CTX_TRANSACTION_ID("ctx.transaction.id", "{transactionId-not-found}"),
        /** Reactor context key for authorization request identifier. */
        CTX_AUTHORIZATION_REQUEST_ID("ctx.authorization.request.id", "{authorizationRequestId-not-found}"),
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
        /** MDC key for correlation identifier. */
        CORRELATION_ID("correlation.id", "{correlationId-not-found}"),
        /** MDC key for PSP identifier. */
        PSP_ID("psp.id", "{pspId-not-found}");

        private final String key;
        private final String defaultValue;

        AttributeKeys(
                String key,
                String defaultValue
        ) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }

    /**
     * Enumeration of internal/private MDC keys handled exclusively by the builder.
     */
    private enum AttributeKeysPrivate {
        /** MDC key for custom JSON details map. */
        CTX_DETAILS("ctx.details", "{details-not-found}"),
        /** MDC key for event outcome. */
        EVENT_OUTCOME("event.outcome", "{eventOutcome-not-found}"),
        /**
         * Details key for dependency name involved in the operation (serialized inside
         * ctx.details).
         */
        DEPENDENCY("dependency", "{dependency-not-found}"),
        /** MDC key for error class name. */
        ERROR_TYPE("error.type", "{errorType-not-found}"),
        /** MDC key for error message text. */
        ERROR_MESSAGE("error.message", "{errorMessage-not-found}"),
        /** MDC key for the complete error stack trace. */
        ERROR_STACK_TRACE("error.stack_trace", "{errorStackTrace-not-found}");

        private final String key;
        private final String defaultValue;

        AttributeKeysPrivate(
                String key,
                String defaultValue
        ) {
            this.key = key;
            this.defaultValue = defaultValue;
        }
    }

    private LogTracingUtils() {
    }

    /**
     * Creates a new instance of the log builder.
     *
     * @return a new {@link LogTracingUtils} instance
     */
    public static LogTracingUtils loggerTracingUtils() {
        return new LogTracingUtils();
    }

    /**
     * Sets a map of standard attributes to be added to the MDC.
     *
     * @param attributes map of standard {@link AttributeKeys} and their values
     * @return this builder instance
     */
    public LogTracingUtils attributes(Map<AttributeKeys, String> attributes) {
        this.attributes.putAll(attributes);
        return this;
    }

    /**
     * Adds custom details that will be serialized as a JSON string in the MDC.
     *
     * @param details a map containing custom key-value string pairs
     * @return this builder instance
     */
    public LogTracingUtils details(Map<String, String> details) {
        this.details.putAll(details);
        return this;
    }

    /**
     * Sets the external dependency name involved in the current operation.
     *
     * @param dependency the name of the dependency
     * @return this builder instance
     */
    public LogTracingUtils dependency(String dependency) {
        this.details.put(AttributeKeysPrivate.DEPENDENCY.key, dependency);
        return this;
    }

    /**
     * Marks the outcome of the logged event as successful.
     *
     * @return this builder instance
     */
    public LogTracingUtils success() {
        this.outcome = SUCCESS;
        return this;
    }

    /**
     * Marks the outcome of the logged event as a failure.
     *
     * @return this builder instance
     */
    public LogTracingUtils failure() {
        this.outcome = FAILURE;
        return this;
    }

    /**
     * Serializes the custom details map into a JSON string.
     *
     * @param details the map to serialize
     * @return a JSON formatted string, or "{}" if serialization fails or map is
     *         null
     */
    private static String serializeDetailsToMdcMap(Map<String, ?> details) {
        String rawDetails = "{}";
        if (details != null) {
            try {
                rawDetails = OBJECT_MAPPER.writeValueAsString(details);
            } catch (JsonProcessingException ignored) {
                rawDetails = "{}";
            }
        }
        return rawDetails;
    }

    /**
     * Adds a key-value pair to the MDC and tracks it for post-log cleanup.
     *
     * @param key   the MDC key
     * @param value the MDC value
     */
    private void addMdcKey(
                           String key,
                           String value
    ) {
        MDC.put(key, value);
        mdcKeys.add(key);
    }

    /**
     * Terminal operation: emits an INFO log with the configured MDC attributes,
     * then cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param message the log message
     */
    public void logInfo(
                        Logger logger,
                        String message
    ) {
        this.message = message;
        this.logger = logger;
        log(Level.INFO);
    }

    /**
     * Terminal operation: emits a DEBUG log with the configured MDC attributes,
     * then cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param message the log message
     */
    public void logDebug(
                         Logger logger,
                         String message
    ) {
        this.message = message;
        this.logger = logger;
        log(Level.DEBUG);
    }

    /**
     * Terminal operation: emits a WARN log with the configured MDC attributes, then
     * cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param message the log message
     */
    public void logWarn(
                        Logger logger,
                        String message
    ) {
        this.message = message;
        this.logger = logger;
        log(Level.WARN);
    }

    /**
     * Terminal operation: emits a TRACE log with the configured MDC attributes,
     * then cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param message the log message
     */
    public void logTrace(
                         Logger logger,
                         String message
    ) {
        this.message = message;
        this.logger = logger;
        log(Level.TRACE);
    }

    /**
     * Terminal operation: emits an ERROR log with the configured MDC attributes,
     * then cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param error   Throwable to log
     * @param message the log message
     */
    public void logError(
                         Logger logger,
                         Throwable error,
                         String message
    ) {
        this.logger = logger;
        this.message = message;
        this.error = error;
        log(Level.ERROR);
    }

    /**
     * Terminal operation: emits an ERROR log with the configured MDC attributes and
     * the full stack trace of the provided Throwable, then cleans up the MDC.
     *
     * @param logger  the SLF4J logger to use
     * @param error   Throwable to log
     * @param message the log message
     */
    public void logErrorWithStackTrace(
                                       Logger logger,
                                       Throwable error,
                                       String message
    ) {
        if (error == null) {
            throw new LogTracingUtilException("error must not be null");
        }

        StringWriter sw = new StringWriter();
        error.printStackTrace(new PrintWriter(sw));

        this.stackTrace = sw.toString();
        logError(logger, error, message);
    }

    /**
     * Core logging routine.
     * Populates the MDC with attributes, details, and errors, issues the log statement
     * using the requested level, and immediately performs MDC cleanup to prevent context leaks.
     *
     * @param loggerLevel the SLF4J log level
     */
    private void log(Level loggerLevel) {
        // Add attributes keys and values to MDC map
        if (!attributes.isEmpty()) {
            attributes.forEach(
                    (
                            key,
                            value
                    ) -> {
                        if (key != null && value != null) {
                            MDC.put(key.key, value);
                            mdcKeys.add(key.key);
                        }
                    }
            );
        }

        // Add details key and value to MDC map
        if(!details.isEmpty()) {
            addMdcKey(AttributeKeysPrivate.CTX_DETAILS.key, serializeDetailsToMdcMap(details));
        }

        if (outcome != null) {
            addMdcKey(AttributeKeysPrivate.EVENT_OUTCOME.key, outcome);
        }

        if(error != null) {
            addMdcKey(AttributeKeysPrivate.ERROR_TYPE.key, error.getClass().getName());
            addMdcKey(AttributeKeysPrivate.ERROR_MESSAGE.key, error.getMessage() != null
                    ? error.getMessage()
                    : AttributeKeysPrivate.ERROR_MESSAGE.defaultValue);
        }

        if (stackTrace != null) {
            addMdcKey(AttributeKeysPrivate.ERROR_STACK_TRACE.key, stackTrace);
        }

        switch(loggerLevel) {
            case INFO -> logger.info(message);
            case WARN -> logger.warn(message);
            case DEBUG -> logger.debug(message);
            case TRACE -> logger.trace(message);
            case ERROR -> logger.error(message);
            case null, default -> throw new LogTracingUtilException("loggerLevel null or not defined.");
        }

        // Cleanup MDC
        this.mdcKeys.forEach(MDC::remove);
        this.mdcKeys.clear();
    }

    /**
     * Enrich Reactor Context with tracing entries in a fully generic way.
     *
     * <p>
     * This method accepts a map of {@link AttributeKeys} enum keys with their
     * corresponding values. Each entry is added to the context with its value or
     * default value if null. Any future TracingEntry additions are automatically
     * supported without method changes.
     *
     * @param tracingEntries map of TracingEntry to value; null values use defaults
     * @param reactorContext the context to enrich
     * @return enriched context with all tracing entries
     */
    public static Context enrichContextForEvent(
                                                Map<AttributeKeys, String> tracingEntries,
                                                Context reactorContext
    ) {
        Context enrichedContext = reactorContext;
        if (tracingEntries != null) {
            for (Map.Entry<AttributeKeys, String> entry : tracingEntries.entrySet()) {
                if (entry.getKey() == null) {
                    continue;
                }
                enrichedContext = enrichedContext.put(
                        entry.getKey().key,
                        entry.getValue() != null
                                ? entry.getValue()
                                : entry.getKey().defaultValue
                );
            }
        }
        return enrichedContext;
    }
}
