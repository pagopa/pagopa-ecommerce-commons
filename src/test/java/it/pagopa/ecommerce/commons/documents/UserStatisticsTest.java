package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.documents.userstats.v1.UserStatistics;
import it.pagopa.ecommerce.commons.utils.UserStatisticsTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage.PaymentType.GUEST_PAYMENT_METHOD;
import static it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage.PaymentType.SAVED_WALLET;
import static org.junit.jupiter.api.Assertions.*;

public class UserStatisticsTest {

    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void setValidUserStatisticsWallet() {
        UserStatistics userStatisticsWallet = UserStatisticsTestUtils.userStatisticsWallet();
        Set<ConstraintViolation<UserStatistics>> violationsWallet = validator.validate(userStatisticsWallet);
        assertTrue(violationsWallet.isEmpty());
    }

    @Test
    void setValidUserStatisticsGuest() {
        UserStatistics userStatisticsGuest = UserStatisticsTestUtils.userStatisticsGuest();
        Set<ConstraintViolation<UserStatistics>> violationsGuest = validator.validate(userStatisticsGuest);
        assertTrue(violationsGuest.isEmpty());
    }

    @Test
    void testValidData() {
        assertNotNull(UserStatisticsTestUtils.userStatisticsGuest());
        assertNotNull(UserStatisticsTestUtils.userStatisticsWallet());
    }

    @Test
    void testValidType() {
        UserStatistics guest = UserStatisticsTestUtils.userStatisticsGuest();
        UserStatistics saved = UserStatisticsTestUtils.userStatisticsWallet();
        assertEquals(GUEST_PAYMENT_METHOD, guest.getLastUsage().getType());
        assertEquals(SAVED_WALLET, saved.getLastUsage().getType());
        assertInstanceOf(String.class, guest.getUserId());
        assertInstanceOf(String.class, saved.getUserId());
        assertInstanceOf(UUID.class, guest.getLastUsage().getInstrumentId());
        assertInstanceOf(UUID.class, saved.getLastUsage().getInstrumentId());
        assertInstanceOf(Instant.class, guest.getLastUsage().getDate());
        assertInstanceOf(Instant.class, saved.getLastUsage().getDate());
    }

}
