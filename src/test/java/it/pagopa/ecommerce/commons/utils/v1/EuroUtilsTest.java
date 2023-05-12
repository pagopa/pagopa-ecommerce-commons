package it.pagopa.ecommerce.commons.utils.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionId;
import it.pagopa.ecommerce.commons.utils.EuroUtils;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EuroUtilsTest {

    @Test
    void shouldConvertEuroCentToEuroSuccessfully() {
        BigDecimal euro = EuroUtils.euroCentsToEuro(111);
        System.out.println(euro);
        assertEquals(euro.toString(), "1.11");
    }

    @Test
    void shouldConvertEuroToEuroCentSuccessfully() {

        Integer euroCent = EuroUtils.euroToEuroCents(BigDecimal.valueOf(111, 2));
        assertEquals(euroCent.toString(), "111");
    }

}
