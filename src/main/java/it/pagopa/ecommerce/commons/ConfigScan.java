package it.pagopa.ecommerce.commons;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 * The class used for create a configuration scan into the project
 * </p>
 */
@ComponentScan
@Configuration
public class ConfigScan {
    /**
     * Default constructor
     */
    /*
     * @formatter:off
     *
     * Warning java:S1186 - Methods should not be empty
     * Suppressed because this constructor is required by Spring framework
     * for configuration scanning and should remain empty
     *
     * @formatter:on
     */
    @SuppressWarnings("java:S1186")
    public ConfigScan() {
    }
}
