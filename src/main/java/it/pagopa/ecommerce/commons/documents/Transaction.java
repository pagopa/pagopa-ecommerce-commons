package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.PaymentToken;
import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base persistence view for transactions.
 *
 */
@Data
@Document(collection = "view")
public class Transaction {

    @Id
    private String transactionId;
    private ClientId clientId;
    private String email;
    private TransactionStatusDto status;
    @Nullable
    private Integer feeTotal;
    private String creationDate;
    private List<PaymentNotice> paymentNotices;

    /**
     * Enumeration of transaction clients initializers
     */
    public enum ClientId {
        /**
         * Transaction originated by checkout frontend with notice information input by
         * user
         */
        CHECKOUT,
        /**
         * Transaction originated by E.C. through cart functionality
         */
        CHECKOUT_CART,
        /**
         * Transaction originated by IO app
         */
        IO,
        /**
         * Transaction origin is not an above ones
         */
        UNKNOWN;

        private static final Map<String, ClientId> lookupMap = Collections.unmodifiableMap(
                Arrays.stream(ClientId.values()).collect(Collectors.toMap(ClientId::toString, Function.identity()))
        );

        /**
         *
         * @param enumValue - the enumeration value to be converted to {@link ClientId}
         *                  enumeration instance
         * @return the converted {@link ClientId} enumeration instance or
         *         {@link ClientId#UNKNOWN} if the input value is not assignable to an
         *         enumeration value
         */
        public static ClientId fromString(String enumValue) {
            return lookupMap.getOrDefault(enumValue, UNKNOWN);
        }
    }

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
     * @deprecated use
     *             {@link it.pagopa.ecommerce.commons.documents.Transaction#Transaction(String, List, Integer, String, TransactionStatusDto, ClientId, String)}
     */
    @Deprecated(forRemoval = true)
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
     * @deprecated use
     *             {@link it.pagopa.ecommerce.commons.documents.Transaction#Transaction(String, List, Integer, String, TransactionStatusDto, ClientId, String)}
     */

    @Deprecated(forRemoval = true)
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
     * @deprecated use
     *             {@link it.pagopa.ecommerce.commons.documents.Transaction#Transaction(String, List, Integer, String, TransactionStatusDto, ClientId, String)}
     */
    @Deprecated(forRemoval = true)
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
        this(
                transactionId,
                List.of(new PaymentNotice(paymentToken, rptId, description, amount, null)),
                null,
                email,
                status,
                ClientId.UNKNOWN,
                creationDate
        );
    }

    /**
     * Primary persistence constructor
     *
     * @param transactionId  transaction unique id
     * @param paymentNotices notice code list
     * @param email          user email where the payment receipt will be sent to
     * @param status         transaction status
     * @param clientId       the client identifier
     * @param feeTotal       transaction total fee
     * @param creationDate   transaction creation date
     */
    @PersistenceConstructor
    public Transaction(
            String transactionId,
            List<PaymentNotice> paymentNotices,
            Integer feeTotal,
            String email,
            TransactionStatusDto status,
            ClientId clientId,
            String creationDate
    ) {
        this.transactionId = transactionId;
        this.email = email;
        this.status = status;
        this.paymentNotices = paymentNotices;
        this.feeTotal = feeTotal;
        this.clientId = clientId;
        this.creationDate = creationDate;
    }

    /**
     * Conversion constructor from a
     * {@link it.pagopa.ecommerce.commons.domain.TransactionActivated} to a
     * Transaction
     *
     * @param transaction the transaction
     * @return a transaction document with the same data
     */
    public static Transaction from(TransactionActivated transaction) {
        return new Transaction(
                transaction.getTransactionId().value().toString(),
                transaction.getTransactionActivatedData().getPaymentNotices(),
                null,
                transaction.getTransactionActivatedData().getEmail(),
                transaction.getStatus(),
                transaction.getTransactionActivatedData().getClientId(),
                transaction.getCreationDate().toString()
        );
    }

    /**
     * Conversion constructor from a
     * {@link it.pagopa.ecommerce.commons.domain.TransactionActivationRequested} to
     * a Transaction
     *
     * @param transaction the transaction
     * @return a transaction document with the same data
     */
    public static Transaction from(TransactionActivationRequested transaction) {
        return new Transaction(
                transaction.getTransactionId().value().toString(),
                transaction.getPaymentNotices().stream().filter(Objects::nonNull)
                        .map(
                                n -> new PaymentNotice(
                                        Optional.ofNullable(n.paymentToken()).orElse(new PaymentToken(null)).value(),
                                        n.rptId().value(),
                                        n.transactionDescription().value(),
                                        n.transactionAmount().value(),
                                        n.paymentContextCode().value()
                                )
                        ).collect(Collectors.toList()),
                null,
                transaction.getEmail().value(),
                transaction.getStatus(),
                transaction.getClientId(),
                transaction.getCreationDate().toString()
        );
    }
}
