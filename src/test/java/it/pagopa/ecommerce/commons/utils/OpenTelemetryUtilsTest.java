package it.pagopa.ecommerce.commons.utils;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class OpenTelemetryUtilsTest {

    private final Tracer openTelemetryTracer = Mockito.mock(Tracer.class);

    private final SpanBuilder spanBuilder = Mockito.mock(SpanBuilder.class);

    private final Span span = Mockito.mock(Span.class);

    private final OpenTelemetryUtils openTelemetryUtils = new OpenTelemetryUtils(openTelemetryTracer);

    @Test
    void shouldCreateSpanWithAttributes() {
        // prerequisite
        String spanName = "spanName";
        Attributes attributes = Attributes.of(AttributeKey.stringKey("key"), "value");
        given(openTelemetryTracer.spanBuilder(spanName)).willReturn(spanBuilder);
        given(spanBuilder.startSpan()).willReturn(span);
        given(span.setAllAttributes(any())).willReturn(span);
        // test
        openTelemetryUtils.addSpanWithAttributes(spanName, attributes);
        // assertions
        verify(openTelemetryTracer, times(1)).spanBuilder(spanName);
        verify(span, times(1)).setAllAttributes(attributes);
        verify(span, times(1)).end();
    }

    @Test
    void shouldCreateErrorSpanWithAttributes() {
        // prerequisite
        String spanName = "spanName";
        Attributes attributes = Attributes.of(AttributeKey.stringKey("key"), "value");
        given(openTelemetryTracer.spanBuilder(spanName)).willReturn(spanBuilder);
        given(spanBuilder.startSpan()).willReturn(span);
        given(span.setAllAttributes(any())).willReturn(span);
        // test
        openTelemetryUtils.addErrorSpanWithAttributes(spanName, attributes);
        // assertions
        verify(openTelemetryTracer, times(1)).spanBuilder(spanName);
        verify(span, times(1)).setAllAttributes(attributes);
        verify(span, times(1)).setStatus(StatusCode.ERROR);
        verify(span, times(1)).end();
    }

    @Test
    void shouldCreateErrorSpanWithThrowable() {
        // prerequisite
        String spanName = "spanName";
        Throwable throwable = new RuntimeException("test exception");
        given(openTelemetryTracer.spanBuilder(spanName)).willReturn(spanBuilder);
        given(spanBuilder.startSpan()).willReturn(span);
        given(span.setAllAttributes(any())).willReturn(span);
        // test
        openTelemetryUtils.addErrorSpanWithException(spanName, throwable);
        // assertions
        verify(openTelemetryTracer, times(1)).spanBuilder(spanName);
        verify(span, times(1)).setStatus(StatusCode.ERROR);
        verify(span, times(1)).recordException(throwable);
        verify(span, times(1)).end();
    }

}
