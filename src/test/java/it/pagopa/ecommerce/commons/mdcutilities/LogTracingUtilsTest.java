package it.pagopa.ecommerce.commons.mdcutilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import reactor.util.context.Context;

class LogTracingUtilsTest {

    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        mockLogger = mock(Logger.class);
        MDC.clear(); // Ensure clean state before each test
    }

    @AfterEach
    void tearDown() {
        MDC.clear(); // Ensure clean state after each test
    }

    @Test
    void testLogInfo_withSuccessAndAttributes() {
        // Arrange
        Map<LogTracingUtils.AttributeKeys, String> attributes = new EnumMap<>(LogTracingUtils.AttributeKeys.class);
        attributes.put(LogTracingUtils.AttributeKeys.EVENT_ACTION, "test-action");
        attributes.put(LogTracingUtils.AttributeKeys.CORRELATION_ID, "12345");

        // Act
        // We use doAnswer to inspect MDC exactly when logger.info() is called
        doAnswer(invocation -> {
            assertEquals("test-action", MDC.get("event.action"));
            assertEquals("12345", MDC.get("correlation.id"));
            assertEquals("success", MDC.get("event.outcome"));
            return null;
        }).when(mockLogger).info(anyString());

        LogTracingUtils.loggerTracingUtils()
                .attributes(attributes)
                .success()
                .logInfo(mockLogger, "Test info message");

        // Assert
        verify(mockLogger, times(1)).info("Test info message");
        // Verify Cleanup
        assertNull(MDC.get("event.action"), "MDC should be cleaned up after logging");
        assertNull(MDC.get("correlation.id"));
        assertNull(MDC.get("event.outcome"));
    }

    @Test
    void testLogError_withExceptionAndStackTrace() {
        // Arrange
        RuntimeException testException = new RuntimeException("Something went wrong");

        doAnswer(invocation -> {
            assertEquals("failure", MDC.get("event.outcome"));
            assertEquals(RuntimeException.class.getName(), MDC.get("error.type"));
            assertEquals("Something went wrong", MDC.get("error.message"));
            assertNotNull(MDC.get("error.stack_trace"));
            assertTrue(MDC.get("error.stack_trace").contains("Something went wrong"));
            return null;
        }).when(mockLogger).error(anyString());

        // Act
        LogTracingUtils.loggerTracingUtils()
                .failure()
                .logErrorWithStackTrace(mockLogger, testException, "Test error message");

        // Assert
        verify(mockLogger, times(1)).error("Test error message");
        assertNull(MDC.get("error.type"));
        assertNull(MDC.get("error.stack_trace"));
    }

    @Test
    void testLogDebug_withDetailsAndDependency() throws Exception {
        // Arrange
        Map<String, String> details = Map.of("userId", "u-123", "retryCount", "3");

doAnswer(invocation -> {
    String mdcDetails = MDC.get("ctx.details");
    assertNotNull(mdcDetails);

    Map<String, String> parsedDetails = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
            mdcDetails,
            new com.fasterxml.jackson.core.type.TypeReference<>() {
            }
    );

    assertEquals(
            Map.of("userId", "u-123", "retryCount", "3", "dependency", "my-dependency"),
            parsedDetails
    );
    return null;
}).when(mockLogger).debug(anyString());

        // Act
        LogTracingUtils.loggerTracingUtils()
                .details(details)
                .dependency("my-dependency")
                .logDebug(mockLogger, "Test debug message");

        // Assert
        verify(mockLogger, times(1)).debug("Test debug message");
        assertNull(MDC.get("ctx.details"));
    }

    @Test
    void testLogWarn_basic() {
        // Arrange
        doAnswer(invocation -> {
            assertTrue(
                    MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty(),
                    "MDC should be empty since no attributes were added"
            );
            return null;
        }).when(mockLogger).warn(anyString());

        // Act
        LogTracingUtils.loggerTracingUtils().logWarn(mockLogger, "Warning message");

        // Assert
        verify(mockLogger, times(1)).warn("Warning message");
    }

    @Test
    void testLogTrace_basic() {
        // Act
        LogTracingUtils.loggerTracingUtils().logTrace(mockLogger, "Trace message");

        // Assert
        verify(mockLogger, times(1)).trace("Trace message");
    }

    @Test
    void testErrorWithoutMessage() {
        // Arrange
        Exception exceptionNoMessage = new Exception(); // No message provided

        doAnswer(invocation -> {
            assertEquals(Exception.class.getName(), MDC.get("error.type"));
            // Fallback to default value from AttributeKeysPrivate
            assertEquals("{errorMessage-not-found}", MDC.get("error.message"));
            return null;
        }).when(mockLogger).error(anyString());

        // Act
        LogTracingUtils.loggerTracingUtils()
                .logError(mockLogger, exceptionNoMessage, "Error happened");

        // Assert
        verify(mockLogger, times(1)).error("Error happened");
    }

    @Test
    void testNullAttributeKeysAndValuesAreIgnored() {
        // Arrange
        Map<LogTracingUtils.AttributeKeys, String> attributes = new EnumMap<>(LogTracingUtils.AttributeKeys.class);
        attributes.put(LogTracingUtils.AttributeKeys.CTX_USER_ID, null); // Null value

        doAnswer(invocation -> {
            assertNull(MDC.get("ctx.user.id"));
            return null;
        }).when(mockLogger).info(anyString());

        // Act
        LogTracingUtils.loggerTracingUtils()
                .attributes(attributes)
                .logInfo(mockLogger, "Testing nulls");

        // Assert
        verify(mockLogger, times(1)).info("Testing nulls");
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
        Map<LogTracingUtils.AttributeKeys, String> tracingEntries = Map.of(
                LogTracingUtils.AttributeKeys.EVENT_ACTION,
                "event_action"
        );

        // test
        Context enrichedContext = LogTracingUtils.enrichContextForEvent(tracingEntries, existingContext);

        // assertions
        assertEquals("pre-existing-value", enrichedContext.get("pre-existing-key"));
        assertEquals("event_action", enrichedContext.get(LogTracingUtils.AttributeKeys.EVENT_ACTION.getKey()));
    }

    @Test
    void shouldEnrichContextUsingProvidedAndDefaultValues() {
        // prerequisite
        Map<LogTracingUtils.AttributeKeys, String> tracingEntries = new EnumMap<>(LogTracingUtils.AttributeKeys.class);
        tracingEntries.put(LogTracingUtils.AttributeKeys.EVENT_ACTION, "event_action");
        tracingEntries.put(LogTracingUtils.AttributeKeys.CORRELATION_ID, null);

        // test
        Context enrichedContext = LogTracingUtils.enrichContextForEvent(
                tracingEntries,
                Context.empty()
        );

        // assertions
        assertEquals("event_action", enrichedContext.get("event.action"));
        assertEquals("{correlationId-not-found}", enrichedContext.get("correlation.id"));
    }

}
