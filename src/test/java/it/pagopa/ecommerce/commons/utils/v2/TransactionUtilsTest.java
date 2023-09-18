package it.pagopa.ecommerce.commons.utils.v2;

import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.domain.v2.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static it.pagopa.ecommerce.commons.utils.v2.TransactionUtils.getTransactionFee;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionUtilsTest {

    private TransactionUtils transactionUtils;

    Set<TransactionStatusDto> transientStatusSet = Set.of(
            TransactionStatusDto.ACTIVATED,
            TransactionStatusDto.AUTHORIZATION_REQUESTED,
            TransactionStatusDto.AUTHORIZATION_COMPLETED,
            TransactionStatusDto.CLOSURE_ERROR,
            TransactionStatusDto.CLOSED,
            TransactionStatusDto.EXPIRED,
            TransactionStatusDto.NOTIFIED_KO,
            TransactionStatusDto.CANCELLATION_REQUESTED,
            TransactionStatusDto.NOTIFICATION_ERROR,
            TransactionStatusDto.NOTIFICATION_REQUESTED

    );

    Set<TransactionStatusDto> refaundableStatusSet = Set.of(
            TransactionStatusDto.CLOSED,
            TransactionStatusDto.CLOSURE_ERROR,
            TransactionStatusDto.EXPIRED,
            TransactionStatusDto.AUTHORIZATION_COMPLETED,
            TransactionStatusDto.AUTHORIZATION_REQUESTED,
            TransactionStatusDto.NOTIFIED_KO,
            TransactionStatusDto.NOTIFICATION_ERROR
    );

    @Test
    void shouldHaveBeenTransientStatus() {
        transactionUtils = new TransactionUtils();
        for (TransactionStatusDto status : TransactionStatusDto.values()) {
            if (transientStatusSet.contains(status)) {
                assertTrue(
                        transactionUtils.isTransientStatus(status),
                        "Error! The status %s was expected to be transient".formatted(status)
                );
            } else {
                assertFalse(
                        transactionUtils.isTransientStatus(status),
                        "Error! The status %s was NOT expected to be transient".formatted(status)
                );
            }
        }
    }

    @Test
    void shouldHaveBeenRefundableTransactionStatus() {
        transactionUtils = new TransactionUtils();
        for (TransactionStatusDto status : TransactionStatusDto.class.getEnumConstants()) {
            if (refaundableStatusSet.contains(status)) {
                assertTrue(
                        transactionUtils.isRefundableTransaction(status),
                        "Error! The status %s was expected to be refundable".formatted(status)
                );
            } else {
                assertFalse(
                        transactionUtils.isRefundableTransaction(status),
                        "Error! The status %s was NOT expected to be refundable".formatted(status)
                );
            }
        }
    }

    @Test
    void getFeeReturnsTransactionFeeForRequestedAuthorizationTransaction() {
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(ZonedDateTime.now().toString());
        TransactionWithRequestedAuthorization tx = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        int expected = tx.getTransactionAuthorizationRequestData().getFee();

        assertEquals(getTransactionFee(tx), Optional.of(expected));
    }

    @Test
    void getFeeReturnsEmptyForTransactionWithoutRequestedAuthorization() {
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(ZonedDateTime.now().toString());

        assertEquals(getTransactionFee(transactionActivated), Optional.empty());
    }
}
