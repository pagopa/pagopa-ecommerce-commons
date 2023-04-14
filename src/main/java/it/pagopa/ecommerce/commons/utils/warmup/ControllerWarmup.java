package it.pagopa.ecommerce.commons.utils.warmup;

import it.pagopa.ecommerce.commons.annotations.Warmup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

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
                        log.info("Invoking method: [{}]", method);
                        method.invoke(controller);
                    } catch (InvocationTargetException | IllegalAccessException e) {
                        log.error("Exception invoking warmup method", e);
                    } finally {
                        long interTime = System.currentTimeMillis() - methodStartTime;
                        log.info(
                                "Warmup method: [{}] -> elapsed time: [{}] ms",
                                method,
                                interTime
                        );
                    }

                    return 1;
                })
                .sum();
        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info(
                "Controller: [{}] warm-up executed methods: [{}], elapsed time: [{}] ms",
                controllerClass,
                warmUpMethods,
                elapsedTime
        );
    }

}
