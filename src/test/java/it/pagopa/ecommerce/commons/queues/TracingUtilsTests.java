package it.pagopa.ecommerce.commons.queues;

import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static it.pagopa.ecommerce.commons.queues.TracingInfoTest.MOCK_TRACING_INFO;
import static org.mockito.ArgumentMatchers.any;

public class TracingUtilsTests {
    public static TracingUtils getMock() {
        TracingUtils mockedTracingUtils = Mockito.mock(TracingUtils.class);
        Mockito.when(mockedTracingUtils.traceMono(any(), any())).thenAnswer(invocation -> {
            Function<TracingInfo, Mono<?>> arg = invocation.getArgument(1);

            return arg.apply(MOCK_TRACING_INFO);
        });

        return mockedTracingUtils;
    }
}
