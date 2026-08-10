package it.pagopa.ecommerce.commons.mdcutilities;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import reactor.util.context.Context;

public class LogTracingUtils {
    private String outcome;
    private String message;
    private Throwable error;
    private String stackTrace;
    private Map<AttributeKeys, String> attributes = new EnumMap<>(AttributeKeys.class);
    private final Map<String, String> details = new HashMap<>();
    private Logger logger;

    private final List<String> mdcKeys = new ArrayList<>();

    private static final String SUCCESS = "success";
    private static final String FAILURE = "failure";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Getter
    public enum AttributeKeys {
        /** Reactor context key for action associated with the event. */
        EVENT_ACTION("event.action", "{eventAction-not-found}"),
        /** Reactor context key for transaction identifier. */
        CTX_TRANSACTION_ID("ctx.transaction.id", "{transactionId-not-found}"),
        /** Reactor context key for transaction identifier. */
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

    private enum AttributeKeysPrivate {
        CTX_DETAILS("ctx.details", "{details-not-found}"),
        /** MDC key for event outcome. */
        EVENT_OUTCOME("event.outcome", "{eventOutcome-not-found}"),
        /** MDC key for dependency name involved in the operation. */
        DEPENDENCY("dependency", "{dependency-not-found}"),
        /** MDC key for error class name. */
        ERROR_TYPE("error.type", "{errorType-not-found}"),
        /** MDC key for error message text. */
        ERROR_MESSAGE("error.message", "{errorMessage-not-found}"),

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

    private LogTracingUtils() {}

    public static LogTracingUtils loggerTracingUtils() {
        return new LogTracingUtils();
    }

    public LogTracingUtils attributes(Map<AttributeKeys, String> attributes){
        this.attributes = attributes;
        return this;
    }

    public LogTracingUtils details(Map<String, String> details) {
        this.details.putAll(details);
        return this;
    }

    public LogTracingUtils dependency(String dependency){
        this.details.put(AttributeKeysPrivate.DEPENDENCY.key, dependency);
        return this;
    }

    public LogTracingUtils error(Throwable error) {
        this.error = error;
        return this;
    }

    public LogTracingUtils errorWithStackTrace(Throwable error) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        error.printStackTrace(pw);

        this.stackTrace = sw.toString();

        pw.close();

        return this.error(error);
    }

    public LogTracingUtils success() {
        this.outcome = SUCCESS;
        return this;
    }

    public LogTracingUtils failure() {
        this.outcome = FAILURE;
        return this;
    }

    private static String serializeDetailsToMdcMap(Map<String, ?> details){
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

    // Save key added to MDC
    private void addMdcKey(String key, String value){
        MDC.put(key, value);
        mdcKeys.add(key);
    }

    public void logInfo(Logger logger, String message){
        this.message = message;
        this.logger = logger;
        log(Level.INFO);
    }

    public void logDebug(Logger logger, String message){
        this.message = message;
        this.logger = logger;
        log(Level.DEBUG);
    }

    public void logWarn(Logger logger, String message){
        this.message = message;
        this.logger = logger;
        log(Level.WARN);
    }

    public void logTrace(Logger logger, String message){
        this.message = message;
        this.logger = logger;
        log(Level.TRACE);
    }

    public void logError(Logger logger, String message){
        this.logger = logger;
        this.message = message;
        log(Level.ERROR);
    }

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
            case null, default -> throw new RuntimeException("loggerLevel null or not defined.");
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
            Map<LogTracingUtils.AttributeKeys, String> tracingEntries,
            Context reactorContext
    ) {
        Context enrichedContext = reactorContext;
        if (tracingEntries != null) {
            for (Map.Entry<LogTracingUtils.AttributeKeys, String> entry : tracingEntries.entrySet()) {
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
