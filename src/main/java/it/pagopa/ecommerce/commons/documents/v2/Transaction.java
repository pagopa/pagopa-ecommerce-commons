package it.pagopa.ecommerce.commons.documents.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import it.pagopa.ecommerce.commons.documents.BaseTransactionView;
import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.Email;
import it.pagopa.ecommerce.commons.domain.v2.TransactionActivated;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode(callSuper = true)
@Data
@Document(collection = "transactions-view")
public class Transaction extends BaseTransactionView {

    private ClientId clientId;
    private Confidential<Email> email;
    private TransactionStatusDto status;
    @Nullable
    private Integer feeTotal;
    private String creationDate;
    private List<PaymentNotice> paymentNotices;
    @Nullable
    private String idCart;
    @Nullable
    private String rrn;

    @Nullable
    private String paymentGateway;

    @Nullable
    private TransactionUserReceiptData.Outcome sendPaymentResultOutcome;

    @Nullable
    private String authorizationCode;

    @Nullable
    private String authorizationErrorCode;

    @Nullable
    private String gatewayAuthorizationStatus;

    @Nullable
    private String userId;

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
        IO;

        private static final Map<String, ClientId> lookupMap = Collections.unmodifiableMap(
                Arrays.stream(ClientId.values()).collect(Collectors.toMap(ClientId::toString, Function.identity()))
        );

        /**
         * @param enumValue - the enumeration value to be converted to {@link ClientId}
         *                  enumeration instance
         * @return the converted {@link ClientId} enumeration instance
         */
        public static ClientId fromString(String enumValue) {
            return lookupMap.get(enumValue);
        }
    }

    /**
     * Primary persistence constructor. Warning java:S107 - Methods should not have
     * too many parameters
     *
     * @param transactionId  transaction unique id
     * @param paymentNotices notice code list
     * @param email          user email where the payment receipt will be sent to
     * @param status         transaction status
     * @param clientId       the client identifier
     * @param feeTotal       transaction total fee
     * @param creationDate   transaction creation date
     * @param idCart         the ec cart id
     * @param rrn            the rrn information
     * @param userId         the user unique id
     */
    /*
     * @formatter:off
     *
     * Warning java:S107 - Methods should not have too many parameters
     * Suppressed because the Transaction is a simple data container with no logic.
     *
     * @formatter:on
     */
    @JsonCreator
    @SuppressWarnings(
        {
                "java:S107"
        }
    )
    @PersistenceConstructor
    public Transaction(
            String transactionId,
            List<PaymentNotice> paymentNotices,
            @Nullable Integer feeTotal,
            Confidential<Email> email,
            TransactionStatusDto status,
            ClientId clientId,
            String creationDate,
            @Nullable String idCart,
            @Nullable String rrn,
            @Nullable String userId
    ) {
        super(transactionId);
        this.email = email;
        this.status = status;
        this.paymentNotices = paymentNotices;
        this.feeTotal = feeTotal;
        this.clientId = clientId;
        this.creationDate = creationDate;
        this.idCart = idCart;
        this.rrn = rrn;
        this.userId = userId;
    }

    /**
     * Conversion constructor from a {@link TransactionActivated} to a Transaction
     *
     * @param transaction the transaction
     * @return a transaction document with the same data
     */
    public static Transaction from(TransactionActivated transaction) {
        return new Transaction(
                transaction.getTransactionId().value().replace("-", ""),
                transaction.getTransactionActivatedData().getPaymentNotices(),
                null,
                transaction.getTransactionActivatedData().getEmail(),
                transaction.getStatus(),
                transaction.getTransactionActivatedData().getClientId(),
                transaction.getCreationDate().toString(),
                transaction.getTransactionActivatedData().getIdCart(),
                null,
                transaction.getTransactionActivatedData().getUserId()
        );
    }

}
