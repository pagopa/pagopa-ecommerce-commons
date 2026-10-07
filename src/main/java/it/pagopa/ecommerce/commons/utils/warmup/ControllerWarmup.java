package it.pagopa.ecommerce.commons.utils.warmup;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import it.pagopa.ecommerce.commons.mdcutilities.LogTracingUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

/**
 * Controller warmup logic. This class is an {@link ApplicationListener} for the
 * {@link ContextRefreshedEvent} raised when an ApplicationContext gets
 * initialized or refreshed. Once the event is fired the classpath is scanned
 * searching for all {@link RestController} an, for each of those, searching for
 * methods annotated with {@link Warmup} to be executed
 *
 * @see Warmup
 * @see ContextRefreshedEvent
 * @see ApplicationListener
 */
@Component
@Slf4j
public class ControllerWarmup implements ApplicationListener<ContextRefreshedEvent> {

    /**
     * Default constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required by Spring framework
     * for component instantiation and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public ControllerWarmup() {
    }

    /**
     * Callback method that handles {@link ContextRefreshedEvent} event
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        event
                .getApplicationContext()
                .getBeansWithAnnotation(RestController.class)
                .values()
                .forEach(this::warmupController);
    }

    private void warmupController(Object controller) {
        Class<?> controllerClass = ClassUtils.getUserClass(controller.getClass());
        long startTime = System.currentTimeMillis();
        int warmUpMethods = Arrays.stream(controllerClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Warmup.class))
                .parallel()
                .mapToInt(method -> {
                    long methodStartTime = System.currentTimeMillis();
                    try {
                        method.invoke(controller);
                        if (log.isDebugEnabled()) {
                            LogTracingUtils.loggerTracingUtils()
                                    .details(
                                            Map.of(
                                                    "method",
                                                    method.getName()
                                            )
                                    )
                                    .logDebug(log, "Warmup method invoked");
                        }
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        LogTracingUtils.loggerTracingUtils()
                                .failure()
                                .logError(log, "Exception invoking warmup method");
                    } finally {
                        long interTime = System.currentTimeMillis() - methodStartTime;
                        if (log.isDebugEnabled()) {
                            LogTracingUtils.loggerTracingUtils()
                                    .details(
                                            Map.of(
                                                    "method",
                                                    method.getName(),
                                                    "elapsed_time",
                                                    String.valueOf(interTime)
                                            )
                                    )
                                    .logDebug(log, "Warmup method executed");
                        }
                    }

                    return 1;
                })
                .sum();
        long elapsedTime = System.currentTimeMillis() - startTime;
        LogTracingUtils.loggerTracingUtils()
                .details(
                        Map.of(
                                "controller",
                                controllerClass.getName(),
                                "warmup_methods",
                                String.valueOf(warmUpMethods),
                                "elapsed_time",
                                String.valueOf(elapsedTime)
                        )
                )
                .logInfo(log, "Controller warm-up executed");
    }

}
