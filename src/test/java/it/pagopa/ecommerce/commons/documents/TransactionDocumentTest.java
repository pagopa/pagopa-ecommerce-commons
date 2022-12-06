package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.TransactionUtils;
import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionDocumentTest {

    @Test
    void shouldGetAndSetTransaction() {
        ZonedDateTime creationDateTime = ZonedDateTime.now();
        TransactionStatusDto transactionStatus = TransactionStatusDto.ACTIVATED;

        /* Test */
        Transaction transaction = TransactionUtils.transactionDocument(transactionStatus, creationDateTime);

        Transaction sameTransaction = TransactionUtils.transactionDocument(transactionStatus, creationDateTime);
        sameTransaction.setCreationDate(transaction.getCreationDate());

        // Different transaction (creation date)
        Transaction differentTransaction = new Transaction(
                "",
                "",
                "",
                "",
                1,
                "",
                null,
                ZonedDateTime.now()
        );

        differentTransaction.setPaymentToken(TransactionUtils.PAYMENT_TOKEN);
        differentTransaction.setRptId(TransactionUtils.RPT_ID);
        differentTransaction.setDescription(TransactionUtils.DESCRIPTION);
        differentTransaction.setAmount(TransactionUtils.AMOUNT);
        differentTransaction.setStatus(transactionStatus);

        /* Assertions */
        assertEquals(TransactionUtils.PAYMENT_TOKEN, transaction.getPaymentToken());
        assertEquals(TransactionUtils.RPT_ID, transaction.getRptId());
        assertEquals(TransactionUtils.DESCRIPTION, transaction.getDescription());
        assertEquals(TransactionUtils.AMOUNT, transaction.getAmount());
        assertEquals(transactionStatus, transaction.getStatus());

        assertNotEquals(transaction, differentTransaction);
        assertEquals(transaction.hashCode(), sameTransaction.hashCode());
        assertNotEquals(transaction.toString(), differentTransaction.toString());
    }

    @Test
    void shouldConstructTransactionDocumentFromTransaction() {
        TransactionActivated transaction = TransactionUtils.transactionActivated(ZonedDateTime.now().toString());

        Transaction transactionDocument = Transaction.from(transaction);

        assertEquals(
                transactionDocument.getPaymentToken(),
                transaction.getTransactionActivatedData().getPaymentToken()
        );
        assertEquals(transactionDocument.getRptId(), transaction.getRptId().value());
        assertEquals(transactionDocument.getDescription(), transaction.getDescription().value());
        assertEquals(transactionDocument.getAmount(), transaction.getAmount().value());
        assertEquals(ZonedDateTime.parse(transactionDocument.getCreationDate()), transaction.getCreationDate());
        assertEquals(transactionDocument.getStatus(), transaction.getStatus());
    }

    @Test
    void shouldConstructTransactionDocumentFromTransactionWithRequestedActivation() {
        TransactionActivationRequested transaction = TransactionUtils
                .transactionActivationRequested(ZonedDateTime.now().toString());

        Transaction transactionDocument = Transaction.from(transaction);

        assertNull(transactionDocument.getPaymentToken());
        assertEquals(transactionDocument.getRptId(), transaction.getRptId().value());
        assertEquals(transactionDocument.getDescription(), transaction.getDescription().value());
        assertEquals(transactionDocument.getAmount(), transaction.getAmount().value());
        assertEquals(ZonedDateTime.parse(transactionDocument.getCreationDate()), transaction.getCreationDate());
        assertEquals(transactionDocument.getStatus(), transaction.getStatus());
    }
}
