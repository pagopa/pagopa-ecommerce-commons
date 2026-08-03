package it.pagopa.ecommerce.commons.mdcutilities;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import org.slf4j.MDC;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MDCContextLifterTest {

    private static final Subscription NO_OP_SUBSCRIPTION = new Subscription() {
        @Override
        public void request(long n) {
            // no-op
        }

        @Override
        public void cancel() {
            // no-op
        }
    };

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldDelegateOnSubscribe() {
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(Context.empty());
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);

        lifter.onSubscribe(NO_OP_SUBSCRIPTION);

        assertSame(NO_OP_SUBSCRIPTION, coreSubscriber.subscription);
    }

    @Test
    void shouldCopyContextToMdcOnNext() {
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(
                Context.of(
                        LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(),
                        "transaction-id",
                        LogTracingUtils.TracingEntry.EVENT_ACTION.getKey(),
                        "ACTION"
                )
        );
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);

        lifter.onNext("payload");

        assertEquals("payload", coreSubscriber.nextValue);
        assertEquals("transaction-id", MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey()));
        assertEquals("ACTION", MDC.get(LogTracingUtils.TracingEntry.EVENT_ACTION.getKey()));
        assertEquals(
                LogTracingUtils.TracingEntry.CTX_EVENT_CODE.getDefaultValue(),
                MDC.get(LogTracingUtils.TracingEntry.CTX_EVENT_CODE.getKey())
        );
        assertNull(MDC.get(LogTracingUtils.TracingEntry.DEPENDENCY.getKey()));
    }

    @Test
    void shouldClearMdcWhenContextIsEmptyOnNext() {
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(Context.empty());
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);
        MDC.put("to-clear", "value");

        lifter.onNext("payload");

        assertEquals("payload", coreSubscriber.nextValue);
        assertNull(MDC.get("to-clear"));
    }

    @Test
    void shouldAlwaysClearMdcOnError() {
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(
                Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "transaction-id")
        );
        RuntimeException expected = new RuntimeException("delegate-error");
        RuntimeException failure = new RuntimeException("upstream");
        coreSubscriber.onErrorToThrow = expected;
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> lifter.onError(failure));

        assertSame(expected, thrown);
        assertSame(failure, coreSubscriber.lastError);
        assertNull(MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey()));
    }

    @Test
    void shouldAlwaysClearMdcOnComplete() {
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(
                Context.of(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey(), "transaction-id")
        );
        RuntimeException expected = new RuntimeException("delegate-complete-error");
        coreSubscriber.onCompleteToThrow = expected;
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);

        RuntimeException thrown = assertThrows(RuntimeException.class, lifter::onComplete);

        assertSame(expected, thrown);
        assertTrue(coreSubscriber.completed);
        assertNull(MDC.get(LogTracingUtils.TracingEntry.CTX_TRANSACTION_ID.getKey()));
    }

    @Test
    void shouldDelegateCurrentContext() {
        Context context = Context.of("key", "value");
        RecordingSubscriber coreSubscriber = new RecordingSubscriber(context);
        MDCContextLifter<String> lifter = new MDCContextLifter<>(coreSubscriber);

        Context result = lifter.currentContext();

        assertSame(context, result);
    }

    private static class RecordingSubscriber implements CoreSubscriber<String> {

        private final @NotNull Context context;
        private Subscription subscription;
        private String nextValue;
        private RuntimeException onErrorToThrow;
        private RuntimeException onCompleteToThrow;
        private Throwable lastError;
        private boolean completed;

        private RecordingSubscriber(@NotNull Context context) {
            this.context = context;
        }

        @Override
        public void onSubscribe(@NotNull Subscription subscription) {
            this.subscription = subscription;
        }

        @Override
        public void onNext(String value) {
            this.nextValue = value;
        }

        @Override
        public void onError(@NotNull Throwable throwable) {
            this.lastError = throwable;
            if (onErrorToThrow != null) {
                throw onErrorToThrow;
            }
        }

        @Override
        public void onComplete() {
            this.completed = true;
            if (onCompleteToThrow != null) {
                throw onCompleteToThrow;
            }
        }

        @Override
        public @NotNull Context currentContext() {
            return context;
        }
    }
}
