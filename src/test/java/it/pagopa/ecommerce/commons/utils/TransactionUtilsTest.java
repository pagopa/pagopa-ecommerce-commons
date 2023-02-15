package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.TransactionTestUtils;
import it.pagopa.ecommerce.commons.documents.TransactionAuthorizationRequestedEvent;
import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionWithRequestedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static it.pagopa.ecommerce.commons.utils.TransactionUtils.getTransactionFee;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TransactionUtilsTest {

    private TransactionUtils transactionUtils;

    List<TransactionStatusDto> transientStatusList = List.of(
            TransactionStatusDto.ACTIVATED,
            TransactionStatusDto.AUTHORIZATION_REQUESTED,
            TransactionStatusDto.AUTHORIZATION_COMPLETED,
            TransactionStatusDto.CLOSURE_ERROR,
            TransactionStatusDto.CLOSED,
            TransactionStatusDto.EXPIRED
    );

    List<TransactionStatusDto> refaundableStatusList = List.of(
            TransactionStatusDto.CLOSED,
            TransactionStatusDto.CLOSURE_ERROR,
            TransactionStatusDto.EXPIRED
    );

    @Test
    void shouldHaveBeenTransientStatus() {
        transactionUtils = new TransactionUtils();
        transientStatusList.forEach(
                transactionStatusDto -> {
                    assertTrue(
                            transactionUtils.isTransientStatus(transactionStatusDto),
                            "Error! The status is not transient"
                    );
                }
        );
    }

    @Test
    void shouldHaveBeenRefundableTransactionStatus() {
        transactionUtils = new TransactionUtils();
        refaundableStatusList.forEach(
                transactionStatusDto -> {
                    assertTrue(
                            transactionUtils.isRefundableTransaction(transactionStatusDto),
                            "Error! The status is not refundable transaction"
                    );
                }
        );
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
