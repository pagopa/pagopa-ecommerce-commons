package it.pagopa.ecommerce.commons.mdcutilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MDCContextLifterConfigurationTest {

    @AfterEach
    void cleanup() throws Exception {
        invokeLifecycleMethod(new MDCContextLifterConfiguration(), "cleanupHook");
        MDC.clear();
    }

    @Test
    void shouldRegisterAndCleanupHook() throws Exception {
        MDCContextLifterConfiguration configuration = new MDCContextLifterConfiguration();
        AtomicReference<String> beforeHook = new AtomicReference<>();
        AtomicReference<String> afterHook = new AtomicReference<>();
        AtomicReference<String> afterCleanup = new AtomicReference<>();

        Mono.just("value")
                .doOnNext(v -> beforeHook.set(MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey())))
                .contextWrite(Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "tx-001"))
                .block();
        assertNull(beforeHook.get());

        invokeLifecycleMethod(configuration, "contextOperatorHook");

        Mono.just("value")
                .doOnNext(v -> afterHook.set(MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey())))
                .contextWrite(Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "tx-001"))
                .block();
        assertEquals("tx-001", afterHook.get());

        invokeLifecycleMethod(configuration, "cleanupHook");

        Mono.just("value")
                .doOnNext(v -> afterCleanup.set(MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey())))
                .contextWrite(Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "tx-001"))
                .block();
        assertNull(afterCleanup.get());
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
