package it.pagopa.ecommerce.commons.mdcutilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MDCContextLifterConfigurationTest {

    @AfterEach
    void cleanup() throws Exception {
        invokeLifecycleMethod(new MDCContextLifterConfiguration(), "cleanupHook");
        MDC.clear();
    }

    @Test
    void shouldRegisterAndCleanupHook() throws Exception {
        MDCContextLifterConfiguration configuration = new MDCContextLifterConfiguration();
        invokeLifecycleMethod(configuration, "cleanupHook");

        invokeLifecycleMethod(configuration, "contextOperatorHook");

        String afterHook = Mono.just("value")
                .hide()
                .contextWrite(Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "tx-001"))
                .block();
        assertEquals("value", afterHook);

        invokeLifecycleMethod(configuration, "cleanupHook");

        String afterCleanup = Mono.just("value")
                .hide()
                .contextWrite(Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "tx-001"))
                .block();
        assertEquals("value", afterCleanup);
    }

    private static void invokeLifecycleMethod(
                                              MDCContextLifterConfiguration configuration,
                                              String methodName
    ) throws Exception {
        Method method = MDCContextLifterConfiguration.class.getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(configuration);
    }
}
