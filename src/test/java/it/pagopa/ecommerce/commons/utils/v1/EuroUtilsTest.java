package it.pagopa.ecommerce.commons.utils.v1;

import it.pagopa.ecommerce.commons.utils.EuroUtils;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testConstructorIsPrivate()
            throws NoSuchMethodException {
        Constructor<EuroUtils> constructor = EuroUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
    }

}
