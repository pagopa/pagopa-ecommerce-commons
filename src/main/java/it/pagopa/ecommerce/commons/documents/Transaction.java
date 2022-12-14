package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;

/**
 * Base persistence view for transactions.
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
     * Convenience contructor which sets the transaction creation date to now
     *
     * @param transactionId transaction unique id
     * @param paymentToken  payment token associated to the transaction
     * @param rptId         RPT id associated to the transaction
     * @param description   transaction description
     * @param amount        transaction amount in euro cents
     * @param email         user email where the payment receipt will be sent to
     * @param status        transaction status
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
     * Primary constructor
     *
     * @param transactionId transaction unique id
     * @param paymentToken  payment token associated to the transaction
     * @param rptId         RPT id associated to the transaction
     * @param description   transaction description
     * @param amount        transaction amount in euro cents
     * @param email         user email where the payment receipt will be sent to
     * @param status        transaction status
     * @param creationDate  transaction creation date
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
     * Primary persistence constructor
     *
     * @param transactionId transaction unique id
     * @param paymentToken  payment token associated to the transaction
     * @param rptId         RPT id associated to the transaction
     * @param description   transaction description
     * @param amount        transaction amount in euro cents
     * @param email         user email where the payment receipt will be sent to
     * @param status        transaction status
     * @param creationDate  transaction creation date
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
     * Conversion constructor from a {@link TransactionActivated} to a Transaction
     *
     * @param transaction the transaction
     * @return a transaction document with the same data
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
     * Conversion constructor from a {@link TransactionActivationRequested} to a
     * Transaction
     *
     * @param transaction the transaction
     * @return a transaction document with the same data
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
