package it.pagopa.ecommerce.commons.documents.v1;

import it.pagopa.ecommerce.commons.domain.v1.TransactionActivated;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Base persistence view for transactions.
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
     * Enumeration of transaction client initiators
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
            @Nullable Integer feeTotal,
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
     * Conversion constructor from a {@link TransactionActivated} to a Transaction
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

}