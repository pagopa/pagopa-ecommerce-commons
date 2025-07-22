package it.pagopa.ecommerce.commons.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * <p>
 * Utility class for handle euro value.
 * </p>
 */
public class EuroUtils {

    /**
     * Create EuroUtils object.
     */
    private EuroUtils() {
        throw new IllegalStateException("Utility EuroUtils class");
    }

    /**
     *
     * Method to convert euroCent value to euro.
     *
     * @param euroCents euroCent value to convert.
     * @return bigDecimal euroCent value.
     */
    public static BigDecimal euroCentsToEuro(Integer euroCents) {
        return BigDecimal.valueOf(euroCents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     *
     * Method to convert euro value to euroCent.
     *
     * @param euro euro value to convert.
     * @return Integer euro value.
     */
    public static Long euroToEuroCents(BigDecimal euro) {
        return euro.multiply(BigDecimal.valueOf(100)).longValue();
    }
}
