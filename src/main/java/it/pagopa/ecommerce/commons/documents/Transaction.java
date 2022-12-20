package it.pagopa.ecommerce.commons.documents;

import it.pagopa.ecommerce.commons.domain.PaymentToken;
import it.pagopa.ecommerce.commons.domain.TransactionActivated;
import it.pagopa.ecommerce.commons.domain.TransactionActivationRequested;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.ZonedDateTime;
import java.util.*;
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
    private OriginType origin;
    private String email;
    private TransactionStatusDto status;
    private int amountTotal;
    private int feeTotal;
    private String creationDate;
    private List<NoticeCode> noticeCodes;

    /**
     * Enumeration of transaction origin
     */
    public enum OriginType {
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

        private static final Map<String, OriginType> lookupMap = Collections.unmodifiableMap(
                Arrays.stream(OriginType.values()).collect(Collectors.toMap(OriginType::toString, Function.identity()))
        );

        /**
         *
         * @param enumValue - the enumeration value to be converted to
         *                  {@link OriginType} enumeration instance
         * @return the converted {@link OriginType} enumeration instance or
         *         {@link OriginType#UNKNOWN} if the input value is not assignable to an
         *         enumeration value
         */
        public static OriginType fromString(String enumValue) {
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
     *             {@link Transaction#Transaction(String, List, int, int, String, TransactionStatusDto, OriginType, String)}
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
     *             {@link Transaction#Transaction(String, List, int, int, String, TransactionStatusDto, OriginType, String)}
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
     *             {@link Transaction#Transaction(String, List, int, int, String, TransactionStatusDto, OriginType, String)}
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
                List.of(new NoticeCode(paymentToken, rptId, description, amount, null)),
                amount,
                0,
                email,
                status,
                OriginType.UNKNOWN,
                creationDate
        );
    }

    /**
     * Primary persistence constructor
     *
     * @param transactionId transaction unique id
     * @param noticeCodes   notice code list
     * @param email         user email where the payment receipt will be sent to
     * @param status        transaction status
     * @param origin        transaction origin
     * @param amountTotal   transaction total amount
     * @param feeTotal      transaction total fee
     * @param creationDate  transaction creation date
     */
    @PersistenceConstructor
    public Transaction(
            String transactionId,
            List<NoticeCode> noticeCodes,
            int amountTotal,
            int feeTotal,
            String email,
            TransactionStatusDto status,
            OriginType origin,
            String creationDate
    ) {
        this.transactionId = transactionId;
        this.email = email;
        this.status = status;
        this.noticeCodes = noticeCodes;
        this.amountTotal = amountTotal;
        this.feeTotal = feeTotal;
        this.origin = origin;
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
                transaction.getTransactionActivatedData().getNoticeCodes(),
                transaction.getTransactionActivatedData().getNoticeCodes().stream().mapToInt(n -> n.getAmount()).sum(),
                0,
                transaction.getTransactionActivatedData().getEmail(),
                transaction.getStatus(),
                OriginType.UNKNOWN,
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
                transaction.getNoticeCodes().stream().filter(Objects::nonNull)
                        .map(
                                n -> new NoticeCode(
                                        Optional.ofNullable(n.paymentToken()).orElse(new PaymentToken(null)).value(),
                                        n.rptId().value(),
                                        n.transactionDescription().value(),
                                        n.transactionAmount().value(),
                                        n.paymentContextCode().value()
                                )
                        ).collect(Collectors.toList()),
                transaction.getNoticeCodes().stream().mapToInt(n -> n.transactionAmount().value()).sum(),
                0,
                transaction.getEmail().value(),
                transaction.getStatus(),
                OriginType.UNKNOWN,
                transaction.getCreationDate().toString()
        );
    }
}
