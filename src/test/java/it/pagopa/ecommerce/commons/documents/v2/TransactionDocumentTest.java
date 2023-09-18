package it.pagopa.ecommerce.commons.documents.v2;

import it.pagopa.ecommerce.commons.documents.BaseTransactionEvent;
import it.pagopa.ecommerce.commons.documents.v2.activation.EmptyTransactionActivationData;
import it.pagopa.ecommerce.commons.domain.v2.TransactionActivated;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TransactionDocumentTest {
    @Test
    void v2TransactionEventIsBaseTransactionEvent() {
        TransactionEvent<TransactionActivatedData> t = TransactionTestUtils
                .transactionActivateEvent(new EmptyTransactionActivationData());

        assertInstanceOf(BaseTransactionEvent.class, t);
    }

    @Test
    void shouldGetAndSetTransaction() {
        ZonedDateTime creationDateTime = ZonedDateTime.now();
        TransactionStatusDto transactionStatus = TransactionStatusDto.ACTIVATED;

        /* Test */
        Transaction transaction = TransactionTestUtils.transactionDocument(transactionStatus, creationDateTime);

        Transaction sameTransaction = TransactionTestUtils.transactionDocument(transactionStatus, creationDateTime);
        sameTransaction.setCreationDate(transaction.getCreationDate());

        // Different transaction (creation date)
        Transaction differentTransaction = TransactionTestUtils
                .transactionDocument(transactionStatus, ZonedDateTime.now());

        /* Assertions */
        assertEquals(TransactionTestUtils.PAYMENT_TOKEN, transaction.getPaymentNotices().get(0).getPaymentToken());
        assertEquals(TransactionTestUtils.RPT_ID, transaction.getPaymentNotices().get(0).getRptId());
        assertEquals(TransactionTestUtils.DESCRIPTION, transaction.getPaymentNotices().get(0).getDescription());
        assertEquals(TransactionTestUtils.AMOUNT, transaction.getPaymentNotices().get(0).getAmount());
        assertEquals(TransactionTestUtils.TRANSACTION_ID, transaction.getTransactionId());
        assertEquals(transactionStatus, transaction.getStatus());

        assertNotEquals(transaction, differentTransaction);
        assertEquals(transaction.hashCode(), sameTransaction.hashCode());
        assertNotEquals(transaction.toString(), differentTransaction.toString());
    }

    @Test
    void shouldConstructTransactionDocumentFromTransaction() {
        TransactionActivated transaction = TransactionTestUtils
                .transactionActivated(ZonedDateTime.now().toString(), new EmptyTransactionActivationData());

        Transaction transactionDocument = Transaction.from(transaction);

        assertEquals(
                transactionDocument.getPaymentNotices().get(0).getPaymentToken(),
                transaction.getTransactionActivatedData().getPaymentNotices().get(0).getPaymentToken()
        );
        assertEquals(
                transactionDocument.getPaymentNotices().get(0).getRptId(),
                transaction.getPaymentNotices().get(0).rptId().value()
        );
        assertEquals(
                transactionDocument.getPaymentNotices().get(0).getDescription(),
                transaction.getPaymentNotices().get(0).transactionDescription().value()
        );
        assertEquals(
                transactionDocument.getPaymentNotices().get(0).getAmount(),
                transaction.getPaymentNotices().get(0).transactionAmount().value()
        );
        assertEquals(ZonedDateTime.parse(transactionDocument.getCreationDate()), transaction.getCreationDate());
        assertEquals(transactionDocument.getStatus(), transaction.getStatus());
    }

    @Test
    void shouldConvertTransactionClientIdEnumerationCorrectly() {
        assertEquals(null, Transaction.ClientId.fromString(""));
        assertEquals(null, Transaction.ClientId.fromString(null));
        for (Transaction.ClientId clientId : Transaction.ClientId.values()) {
            assertEquals(Transaction.ClientId.fromString(clientId.toString()).toString(), clientId.toString());
        }
    }

}
