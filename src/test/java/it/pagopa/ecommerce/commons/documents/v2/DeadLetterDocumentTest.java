package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.DeadLetterEvent;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterTransactionInfo;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterTransactionInfoDetailsData;
import it.pagopa.ecommerce.commons.v2.DeadLetterTestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class DeadLetterDocumentTest {

    private static Validator validator;

    @BeforeAll
    static void beforeAll() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void setDeadLetterWithoutTransactionInfoData() {
        DeadLetterEvent deadLetterEvent = DeadLetterTestUtils.deadLetterEventWithoutTransactionInfo();
        Set<ConstraintViolation<DeadLetterEvent>> violations = validator.validate(deadLetterEvent);
        assertTrue(violations.isEmpty());
        assertNull(deadLetterEvent.getTransactionInfo());
    }

    @Test
    void setValidTransactionInfoData() {
        DeadLetterEvent deadLetterEventNpg = DeadLetterTestUtils
                .deadLetterEventWithTransactionInfo(TransactionAuthorizationRequestData.PaymentGateway.NPG);
        DeadLetterEvent deadLetterEventRedirect = DeadLetterTestUtils
                .deadLetterEventWithTransactionInfo(TransactionAuthorizationRequestData.PaymentGateway.REDIRECT);
        Set<ConstraintViolation<DeadLetterTransactionInfo>> violations = new HashSet<>();
        violations.addAll(validator.validate(deadLetterEventNpg.getTransactionInfo()));
        violations.addAll(validator.validate(deadLetterEventRedirect.getTransactionInfo()));
        assertTrue(violations.isEmpty());
        assertEquals(
                deadLetterEventNpg.getTransactionInfo().getDetails().getType(),
                DeadLetterTransactionInfoDetailsData.TransactionInfoDataType.NPG
        );
        assertEquals(
                deadLetterEventRedirect.getTransactionInfo().getDetails().getType(),
                DeadLetterTransactionInfoDetailsData.TransactionInfoDataType.REDIRECT
        );
    }

    @Test
    void thrownErrorWithIncompleteTransactionInfoData() {
        DeadLetterEvent deadLetterEvent = DeadLetterTestUtils.deadLetterEventWithIncompleteTransactionInfo();
        Set<ConstraintViolation<DeadLetterTransactionInfo>> violations = validator
                .validate(deadLetterEvent.getTransactionInfo());
        assertFalse(
                violations.isEmpty(),
                "transactionId-authorizationRequestId-eCommerceStatus-" +
                        "gateway-paymentTokens should not be null"
        );
        assertEquals(violations.size(), 6);
    }
}
