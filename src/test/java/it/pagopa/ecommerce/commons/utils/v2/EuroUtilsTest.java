package it.pagopa.ecommerce.commons.utils.v2;

import it.pagopa.ecommerce.commons.utils.EuroUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class EuroUtilsTest {

    @Test
    void shouldConvertEuroCentToEuroSuccessfully() {
        BigDecimal euro = EuroUtils.euroCentsToEuro(111L);
        System.out.println(euro);
        assertEquals("1.11", euro.toString());
    }

    @Test
    void shouldConvertEuroToEuroCentSuccessfully() {
        Long euroCent = EuroUtils.euroToEuroCents(BigDecimal.valueOf(111, 2));
        assertEquals("111", euroCent.toString());
    }

    @Test
    void testConstructorIsPrivate()
            throws NoSuchMethodException {
        Constructor<EuroUtils> constructor = EuroUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

}
