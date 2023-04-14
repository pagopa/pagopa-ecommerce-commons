package it.pagopa.ecommerce.commons.utils.warmup;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.AnswersWithDelay;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.mongodb.internal.connection.tlschannel.util.Util.assertTrue;

@ExtendWith(MockitoExtension.class)
class ControllerWarmupTest {

    private final ControllerWarmup controllerWarmup = new ControllerWarmup();

    @Mock
    private ContextRefreshedEvent contextRefreshedEvent;

    @Mock
    private ApplicationContext applicationContext;

    private final Sentinel sentinel = new Sentinel() {
        @Override
        public boolean warmUp() {
            return true;
        }
    };
    private final Sentinel warmupSentinel = Mockito.spy(sentinel);

    private final Sentinel otherMethodSentinel = Mockito.spy(sentinel);

    private final MockedRestControllerWithWarmupMethods mockRestController = new MockedRestControllerWithWarmupMethods();

    private final MockedRestControllerWithoutWarmupMethods mockRestControllerWithoutWarmup = new MockedRestControllerWithoutWarmupMethods();

    @Test
    void shouldCallWarmupMethods() {
        /*
         * pre-conditions
         */
        Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        Mockito.when(applicationContext.getBeansWithAnnotation(RestController.class))
                .thenReturn(Map.of("mockRestController", mockRestController));

        /*
         * Test
         */
        controllerWarmup.onApplicationEvent(contextRefreshedEvent);
        /*
         * Assertions
         */
        Mockito.verify(contextRefreshedEvent, Mockito.times(1)).getApplicationContext();
        Mockito.verify(applicationContext, Mockito.times(1)).getBeansWithAnnotation(RestController.class);
        Mockito.verify(warmupSentinel, Mockito.times(2)).warmUp();
        Mockito.verify(otherMethodSentinel, Mockito.times(0)).warmUp();

    }

    @Test
    void shouldCallWarmupMethodInParallel() {
        /*
         * pre-conditions
         */
        Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        Mockito.when(applicationContext.getBeansWithAnnotation(RestController.class))
                .thenReturn(Map.of("mockRestController", mockRestController));
        Mockito.doAnswer(new AnswersWithDelay(1000, new Returns(true))).when(warmupSentinel).warmUp();

        /*
         * Test
         */
        long startTime = System.currentTimeMillis();
        controllerWarmup.onApplicationEvent(contextRefreshedEvent);
        long elapsedTime = System.currentTimeMillis() - startTime;
        /*
         * Assertions
         */
        Mockito.verify(contextRefreshedEvent, Mockito.times(1)).getApplicationContext();
        Mockito.verify(applicationContext, Mockito.times(1)).getBeansWithAnnotation(RestController.class);
        Mockito.verify(warmupSentinel, Mockito.times(2)).warmUp();
        Mockito.verify(otherMethodSentinel, Mockito.times(0)).warmUp();
        /*
         * mockedRestControllerWithSleeps has 3 warmup methods with a sleep of 1000 ms
         * each. To check here that all those methods are run in parallel a check is
         * performed against the onApplicationEvent method duration that should be
         * comparable with a single warmup method duration
         */
        assertTrue(elapsedTime < 2000);

    }

    @Test
    void shouldHandleWarmupMethodsException() {
        /*
         * pre-conditions
         */
        Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        Mockito.when(applicationContext.getBeansWithAnnotation(RestController.class))
                .thenReturn(Map.of("mockRestController", mockRestController));
        Mockito.doThrow(new RuntimeException("Exception invoking warmup method")).when(warmupSentinel).warmUp();
        /*
         * Test
         */
        Assertions.assertDoesNotThrow(() -> controllerWarmup.onApplicationEvent(contextRefreshedEvent));

        /*
         * Assertions
         */
        Mockito.verify(contextRefreshedEvent, Mockito.times(1)).getApplicationContext();
        Mockito.verify(applicationContext, Mockito.times(1)).getBeansWithAnnotation(RestController.class);
        Mockito.verify(warmupSentinel, Mockito.times(2)).warmUp();
        Mockito.verify(otherMethodSentinel, Mockito.times(0)).warmUp();

    }

    @Test
    void shouldIgnoreControllersWithoutWarmupMethods() {
        /*
         * pre-conditions
         */
        Mockito.when(contextRefreshedEvent.getApplicationContext()).thenReturn(applicationContext);
        Mockito.when(applicationContext.getBeansWithAnnotation(RestController.class))
                .thenReturn(Map.of("mockRestController", mockRestControllerWithoutWarmup));
        /*
         * Test
         */
        controllerWarmup.onApplicationEvent(contextRefreshedEvent);

        /*
         * Assertions
         */
        Mockito.verify(contextRefreshedEvent, Mockito.times(1)).getApplicationContext();
        Mockito.verify(applicationContext, Mockito.times(1)).getBeansWithAnnotation(RestController.class);
        Mockito.verify(warmupSentinel, Mockito.times(0)).warmUp();
        Mockito.verify(otherMethodSentinel, Mockito.times(0)).warmUp();

    }

    private interface Sentinel {
        boolean warmUp();
    }

    private class MockedRestControllerWithWarmupMethods {

        @Warmup
        public void warmupMethod() {
            warmupSentinel.warmUp();

        }

        @Warmup
        public void otherWarmupMethod() {
            warmupSentinel.warmUp();

        }

        public void noWarmupMethod() {
            otherMethodSentinel.warmUp();

        }
    }

    private class MockedRestControllerWithoutWarmupMethods {

        public void noWarmupMethod() {
            /*
             * put system out here to verify that sentinel object .toString() method is
             * invoked by warmup logic
             */
            callWarmupMethod(otherMethodSentinel);

        }

        private void callWarmupMethod(Sentinel sentinel) {
            sentinel.warmUp();
        }
    }
}
