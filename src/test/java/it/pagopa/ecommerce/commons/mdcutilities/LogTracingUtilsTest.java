package it.pagopa.ecommerce.commons.mdcutilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.util.context.Context;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogTracingUtilsTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldReturnSameContextWhenTracingEntriesAreNull() {
        // prerequisite
        Context reactorContext = Context.of("existing-key", "existing-value");

        // test
        Context enrichedContext = LogTracingUtils.enrichContextForEvent(null, reactorContext);

        // assertions
        assertSame(reactorContext, enrichedContext);
        assertEquals("existing-value", enrichedContext.get("existing-key"));
    }

    @Test
    void shouldPreserveExistingContextEntriesWhenEnriching() {
        // prerequisite
        Context existingContext = Context.of("pre-existing-key", "pre-existing-value");
        Map<LogTracingUtils.TracingEntry, String> tracingEntries = Map.of(
                LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID,
                "transaction-id"
        );

        // test
        Context enrichedContext = LogTracingUtils.enrichContextForEvent(tracingEntries, existingContext);

        // assertions
        assertEquals("pre-existing-value", enrichedContext.get("pre-existing-key"));
        assertEquals("transaction-id", enrichedContext.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey()));
    }

    @Test
    void shouldEnrichContextUsingProvidedAndDefaultValues() {
        // prerequisite
        Map<LogTracingUtils.TracingEntry, String> tracingEntries = new EnumMap<>(LogTracingUtils.TracingEntry.class);
        tracingEntries.put(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID, "transaction-id");
        tracingEntries.put(LogTracingUtils.TracingEntry.CTX_EVENT_CODE, null);

        // test
        Context enrichedContext = LogTracingUtils.enrichContextForEvent(
                tracingEntries,
                Context.empty()
        );

        // assertions
        assertEquals("transaction-id", enrichedContext.get("ctx.transaction.id"));
        assertEquals("{eventCode-not-found}", enrichedContext.get("ctx.event.code"));
    }

    @Test
    void shouldExposeExpectedTracingEntryKeys() {
        assertEquals("ctx.transaction.id", LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey());
        assertEquals("ctx.event.code", LogTracingUtils.TracingEntry.CTX_EVENT_CODE.getKey());
        assertEquals("ctx.event.id", LogTracingUtils.TracingEntry.CTX_EVENT_ID.getKey());
        assertEquals("event.action", LogTracingUtils.TracingEntry.EVENT_ACTION.getKey());
        assertEquals("event.outcome", LogTracingUtils.TracingEntry.EVENT_OUTCOME.getKey());
        assertEquals("dependency", LogTracingUtils.TracingEntry.DEPENDENCY.getKey());
        assertEquals("error.type", LogTracingUtils.TracingEntry.ERROR_TYPE.getKey());
        assertEquals("error.message", LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey());
    }

    @Test
    void shouldExposeExpectedTracingEntryDefaultValues() {
        assertEquals("{transactionId-not-found}", LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getDefaultValue());
        assertEquals("{eventCode-not-found}", LogTracingUtils.TracingEntry.CTX_EVENT_CODE.getDefaultValue());
        assertEquals("{eventId-not-found}", LogTracingUtils.TracingEntry.CTX_EVENT_ID.getDefaultValue());
        assertEquals("{eventAction-not-found}", LogTracingUtils.TracingEntry.EVENT_ACTION.getDefaultValue());
        assertEquals("{errorType-not-found}", LogTracingUtils.TracingEntry.ERROR_TYPE.getDefaultValue());
        assertEquals("{errorMessage-not-found}", LogTracingUtils.TracingEntry.ERROR_MESSAGE.getDefaultValue());
    }

    @Test
    void shouldExposeExpectedTracingEntryContextBoundFlags() {
        assertTrue(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.isContextBound());
        assertTrue(LogTracingUtils.TracingEntry.CTX_EVENT_CODE.isContextBound());
        assertTrue(LogTracingUtils.TracingEntry.CTX_EVENT_ID.isContextBound());
        assertTrue(LogTracingUtils.TracingEntry.EVENT_ACTION.isContextBound());
        assertFalse(LogTracingUtils.TracingEntry.EVENT_OUTCOME.isContextBound());
        assertFalse(LogTracingUtils.TracingEntry.DEPENDENCY.isContextBound());
        assertFalse(LogTracingUtils.TracingEntry.ERROR_TYPE.isContextBound());
        assertFalse(LogTracingUtils.TracingEntry.ERROR_MESSAGE.isContextBound());
    }

    @Test
    void shouldPopulateAndCleanupMdcForErrorDetails() {
        // prerequisite
        RuntimeException error = new RuntimeException("error");
        String dependency = LogTracingUtils.MONGO_DEPENDENCY_KEY;

        // test
        LogTracingUtils.withErrorMdc(
                error,
                Map.of(LogTracingUtils.TracingEntry.DEPENDENCY.getKey(), dependency),
                () -> {
                    assertEquals(
                            RuntimeException.class.getName(),
                            MDC.get(LogTracingUtils.TracingEntry.ERROR_TYPE.getKey())
                    );
                    assertEquals("error", MDC.get(LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey()));
                    assertEquals(dependency, MDC.get(LogTracingUtils.TracingEntry.DEPENDENCY.getKey()));
                }
        );

        // assertions
        assertNull(MDC.get(LogTracingUtils.TracingEntry.ERROR_TYPE.getKey()));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey()));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.DEPENDENCY.getKey()));
    }

    @Test
    void shouldUseFallbackValuesForNullThrowable() {
        // test
        LogTracingUtils.withErrorMdc(null, () -> {
            assertEquals(
                    "{errorType-not-found}",
                    MDC.get(LogTracingUtils.TracingEntry.ERROR_TYPE.getKey())
            );
            assertEquals(
                    "{errorMessage-not-found}",
                    MDC.get(LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey())
            );
        });
    }

    @Test
    void shouldUseDefaultMessageWhenThrowableMessageIsNull() {
        // test
        LogTracingUtils.withErrorMdc(
                new RuntimeException((String) null),
                () -> assertEquals(
                        "{errorMessage-not-found}",
                        MDC.get(LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey())
                )
        );
    }

    @Test
    void shouldCleanupMdcEvenWhenWithErrorMdcBlockThrows() {
        // prerequisite
        RuntimeException expected = new RuntimeException("failing-block");
        IllegalStateException error = new IllegalStateException("error");
        Map<String, String> attributes = Map.of(
                LogTracingUtils.TracingEntry.DEPENDENCY.getKey(),
                LogTracingUtils.REDIS_DEPENDENCY_KEY
        );
        Runnable block = () -> {
            throw expected;
        };

        // test & assertions
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> LogTracingUtils.withErrorMdc(error, attributes, block)
        );
        assertSame(expected, thrown);
        assertNull(MDC.get(LogTracingUtils.TracingEntry.ERROR_TYPE.getKey()));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.ERROR_MESSAGE.getKey()));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.DEPENDENCY.getKey()));
    }

    @Test
    void shouldIgnoreNullAttributesInWithErrorMdc() {
        // prerequisite
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("valid.key", "value");
        attributes.put(null, "ignored-value");
        attributes.put("null.value", null);

        // test
        LogTracingUtils.withErrorMdc(new RuntimeException("error"), attributes, () -> {
            assertEquals("value", MDC.get("valid.key"));
            assertNull(MDC.get("null.value"));
        });

        // assertions
        assertNull(MDC.get("valid.key"));
    }

    @Test
    void shouldWorkWhenErrorAttributesMapIsNull() {
        // test
        LogTracingUtils.withErrorMdc(
                new RuntimeException("error"),
                null,
                () -> assertEquals(
                        RuntimeException.class.getName(),
                        MDC.get(LogTracingUtils.TracingEntry.ERROR_TYPE.getKey())
                )
        );
    }

    @Test
    void shouldPopulateAndCleanupMdcForContextDetails() {
        // test
        LogTracingUtils.withContextDetailsMdc(
                Map.of("detail", "value"),
                Map.of(LogTracingUtils.TracingEntry.PATH.getKey(), "/transactions"),
                () -> {
                    assertTrue(MDC.get("ctx.details").contains("\"detail\":\"value\""));
                    assertEquals("/transactions", MDC.get(LogTracingUtils.TracingEntry.PATH.getKey()));
                }
        );

        // assertions
        assertNull(MDC.get("ctx.details"));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.PATH.getKey()));
    }

    @Test
    void shouldPopulateContextDetailsFromSingleArgumentOverload() {
        // test
        LogTracingUtils.withContextDetailsMdc(Map.of("status", "CLOSED"), () -> {
            String details = MDC.get("ctx.details");
            assertNotNull(details);
            assertTrue(details.contains("\"status\":\"CLOSED\""));
        });
    }

    @Test
    void shouldPopulateEmptyJsonWhenDetailsAreNull() {
        // test
        LogTracingUtils.withContextDetailsMdc(null, () -> assertEquals("{}", MDC.get("ctx.details")));

        // assertions
        assertNull(MDC.get("ctx.details"));
    }

    @Test
    void shouldPopulateEmptyJsonWhenDetailsAreEmpty() {
        // test
        LogTracingUtils.withContextDetailsMdc(Map.of(), () -> assertEquals("{}", MDC.get("ctx.details")));

        // assertions
        assertNull(MDC.get("ctx.details"));
    }

    @Test
    void shouldIgnoreNullAttributesInWithContextDetailsMdc() {
        // prerequisite
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("present", "yes");
        attributes.put("absent", null);

        // test
        LogTracingUtils.withContextDetailsMdc(
                Map.of("k", "v"),
                attributes,
                () -> {
                    assertEquals("yes", MDC.get("present"));
                    assertNull(MDC.get("absent"));
                }
        );

        // assertions
        assertNull(MDC.get("present"));
    }

    @Test
    void shouldWorkWhenContextDetailsAttributesMapIsNull() {
        // test
        LogTracingUtils.withContextDetailsMdc(
                Map.of("transactionId", "event"),
                null,
                () -> {
                    String details = MDC.get("ctx.details");
                    assertNotNull(details);
                    assertTrue(details.contains("\"transactionId\":\"event\""));
                }
        );
    }

    @Test
    void shouldCleanupMdcEvenWhenWithContextDetailsBlockThrows() {
        // prerequisite
        RuntimeException expected = new RuntimeException("failing-context-details");
        Map<String, String> details = Map.of("detail", "value");
        Map<String, String> attributes = Map.of(
                LogTracingUtils.TracingEntry.PATH.getKey(),
                "/transactions"
        );
        Runnable block = () -> {
            throw expected;
        };

        // test & assertions
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> LogTracingUtils.withContextDetailsMdc(details, attributes, block)
        );
        assertSame(expected, thrown);
        assertNull(MDC.get("ctx.details"));
        assertNull(MDC.get(LogTracingUtils.TracingEntry.PATH.getKey()));
    }
}
