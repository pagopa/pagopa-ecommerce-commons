package it.pagopa.ecommerce.commons.annotations;

import java.lang.annotation.*;

/**
 * Annotation used to annotate a controller method to be called during module
 * warm-up phase. Warm-up function can be used to send request to a
 * RestController in order to initialize all it's resource before the module
 * being ready to serve requests
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WarmupMethod {

}
