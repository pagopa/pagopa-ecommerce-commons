package it.pagopa.ecommerce.commons.utils;

import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class TransactionUtilsTest {

    private TransactionUtils transactionUtils;

    List<TransactionStatusDto> transientStatusList = List.of(
            TransactionStatusDto.ACTIVATED,
            TransactionStatusDto.AUTHORIZED,
            TransactionStatusDto.AUTHORIZATION_REQUESTED,
            TransactionStatusDto.AUTHORIZATION_FAILED,
            TransactionStatusDto.CLOSURE_FAILED,
            TransactionStatusDto.CLOSED
    );

    List<TransactionStatusDto> refaundableStatusList = List.of(
            TransactionStatusDto.AUTHORIZED,
            TransactionStatusDto.AUTHORIZATION_REQUESTED,
            TransactionStatusDto.AUTHORIZATION_FAILED,
            TransactionStatusDto.CLOSURE_FAILED
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
}
