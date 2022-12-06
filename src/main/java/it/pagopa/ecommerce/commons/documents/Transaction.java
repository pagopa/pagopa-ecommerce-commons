package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import java.time.ZonedDateTime;

import it.pagopa.generated.transactions.server.model.TransactionStatusDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * <p>
 * A transaction view persisted on a data store (view as in CQRS). Provides
 * basic access to common transaction attributes.
 * <p>
 * If you want to manipulate transactions, prefer reading from the event store
 * and using
 * {@link it.pagopa.ecommerce.commons.domain.Transaction#applyEvent(Object)
 * Transaction.applyEvent(Object)} instead.
 * </p>
 */
@Data
@Document(collection = "view")
public class Transaction {

    @Id
    private String transactionId;

    private String paymentToken;
    private String rptId;
    private String description;
    private int amount;
    private String email;
    private TransactionStatusDto status;
    private String creationDate;

    /**
     * Convenience constructor which defaults the creation date to now.
     *
     * @param transactionId the transaction unique id. Should be the string
     *                      representation of a UUID
     * @param paymentToken  the payment token associated to the transaction
     * @param rptId         the RPT id associated to the transaction. See
     *                      {@link it.pagopa.ecommerce.commons.domain.RptId}
     * @param description   the transaction description
     * @param amount        the transaction amount in euro cents
     * @param email         the email where the transaction receipt will be sent to
     * @param status        the current transaction status
     */
    public Transaction(
            String transactionId,
            String paymentToken,
            String rptId,
            String description,
            int amount,
            String email,
            TransactionStatusDto status
    ) {
        this(transactionId, paymentToken, rptId, description, amount, email, status, ZonedDateTime.now().toString());
    }

    /**
     * Primary constructor.
     *
     * @param transactionId the transaction unique id. Should be the string
     *                      representation of a UUID
     * @param paymentToken  the payment token associated to the transaction
     * @param rptId         the RPT id associated to the transaction. See
     *                      {@link it.pagopa.ecommerce.commons.domain.RptId}
     * @param description   the transaction description
     * @param amount        the transaction amount in euro cents
     * @param email         the email where the transaction receipt will be sent to
     * @param status        the current transaction status
     * @param creationDate  the transaction creation date
     */
    public Transaction(
            String transactionId,
            String paymentToken,
            String rptId,
            String description,
            int amount,
            String email,
            TransactionStatusDto status,
            ZonedDateTime creationDate
    ) {
        this(transactionId, paymentToken, rptId, description, amount, email, status, creationDate.toString());
    }

    /**
     * Persistence constructor. Prefer calling the other constructors instead.
     *
     * @param transactionId the transaction unique id. Should be the string
     *                      representation of a UUID
     * @param paymentToken  the payment token associated to the transaction
     * @param rptId         the RPT id associated to the transaction. See
     *                      {@link it.pagopa.ecommerce.commons.domain.RptId}
     * @param description   the transaction description
     * @param amount        the transaction amount in euro cents
     * @param email         the email where the transaction receipt will be sent to
     * @param status        the current transaction status
     * @param creationDate  the transaction creation date
     */
    @PersistenceConstructor
    public Transaction(
            String transactionId,
            String paymentToken,
            String rptId,
            String description,
            int amount,
            String email,
            TransactionStatusDto status,
            String creationDate
    ) {
        this.transactionId = transactionId;
        this.rptId = rptId;
        this.description = description;
        this.paymentToken = paymentToken;
        this.amount = amount;
        this.email = email;
        this.status = status;
        this.creationDate = creationDate;
    }

    /**
     * Conversion constructor from a {@link TransactionActivated}
     *
     * @param transaction the transaction
     * @return a transaction document with the same data as the domain object
     */
    public static Transaction from(TransactionActivated transaction) {
        return new Transaction(
                transaction.getTransactionId().value().toString(),
                transaction.getTransactionActivatedData().getPaymentToken(),
                transaction.getRptId().value(),
                transaction.getDescription().value(),
                transaction.getAmount().value(),
                transaction.getEmail().value(),
                transaction.getStatus(),
                transaction.getCreationDate().toString()
        );
    }

    /**
     * Conversion constructor from a {@link TransactionActivationRequested}
     *
     * @param transaction the transaction
     * @return a transaction document with the same data as the domain object
     */
    public static Transaction from(TransactionActivationRequested transaction) {
        return new Transaction(
                transaction.getTransactionId().value().toString(),
                null,
                transaction.getRptId().value(),
                transaction.getDescription().value(),
                transaction.getAmount().value(),
                transaction.getEmail().value(),
                transaction.getStatus(),
                transaction.getCreationDate().toString()
        );
    }
}
