package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.TransactionUtils;
import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
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

        differentTransaction.getNoticeCodes().get(0).setPaymentToken(TransactionUtils.PAYMENT_TOKEN);
        differentTransaction.getNoticeCodes().get(0).setRptId(TransactionUtils.RPT_ID);
        differentTransaction.getNoticeCodes().get(0).setDescription(TransactionUtils.DESCRIPTION);
        differentTransaction.getNoticeCodes().get(0).setAmount(TransactionUtils.AMOUNT);
        differentTransaction.setStatus(transactionStatus);

        /* Assertions */
        assertEquals(TransactionUtils.PAYMENT_TOKEN, transaction.getNoticeCodes().get(0).getPaymentToken());
        assertEquals(TransactionUtils.RPT_ID, transaction.getNoticeCodes().get(0).getRptId());
        assertEquals(TransactionUtils.DESCRIPTION, transaction.getNoticeCodes().get(0).getDescription());
        assertEquals(TransactionUtils.AMOUNT, transaction.getNoticeCodes().get(0).getAmount());
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
                transactionDocument.getNoticeCodes().get(0).getPaymentToken(),
                transaction.getTransactionActivatedData().getNoticeCodes().get(0).getPaymentToken()
        );
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getRptId(),
                transaction.getNoticeCodes().get(0).rptId().value()
        );
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getDescription(),
                transaction.getNoticeCodes().get(0).transactionDescription().value()
        );
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getAmount(),
                transaction.getNoticeCodes().get(0).transactionAmount().value()
        );
        assertEquals(ZonedDateTime.parse(transactionDocument.getCreationDate()), transaction.getCreationDate());
        assertEquals(transactionDocument.getStatus(), transaction.getStatus());
    }

    @Test
    void shouldConstructTransactionDocumentFromTransactionWithRequestedActivation() {
        TransactionActivationRequested transaction = TransactionUtils
                .transactionActivationRequested(ZonedDateTime.now().toString());

        Transaction transactionDocument = Transaction.from(transaction);

        assertNull(transactionDocument.getNoticeCodes().get(0).getPaymentToken());
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getRptId(),
                transaction.getNoticeCodes().get(0).rptId().value()
        );
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getDescription(),
                transaction.getNoticeCodes().get(0).transactionDescription().value()
        );
        assertEquals(
                transactionDocument.getNoticeCodes().get(0).getAmount(),
                transaction.getNoticeCodes().get(0).transactionAmount().value()
        );
        assertEquals(ZonedDateTime.parse(transactionDocument.getCreationDate()), transaction.getCreationDate());
        assertEquals(transactionDocument.getStatus(), transaction.getStatus());
    }

    @Test
    void shouldConvertTransactionOriginEnumerationCorrectly() {
        assertEquals(Transaction.OriginType.fromString("").toString(), Transaction.OriginType.UNKNOWN.toString());
        assertEquals(Transaction.OriginType.fromString(null).toString(), Transaction.OriginType.UNKNOWN.toString());
        for (Transaction.OriginType originType : Transaction.OriginType.values()) {
            assertEquals(Transaction.OriginType.fromString(originType.toString()).toString(), originType.toString());
        }
    }
}
