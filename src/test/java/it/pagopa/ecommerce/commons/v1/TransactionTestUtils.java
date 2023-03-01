package it.pagopa.ecommerce.commons.v1;

import it.pagopa.ecommerce.commons.documents.v1.Transaction;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.*;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;

import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TransactionTestUtils {

    private TransactionTestUtils() {
        // helper class with only static methods, no need to instantiate it
    }

    public static final ConfidentialDataManager confidentialDataManager = constructConfidentialDataManager();

    public static final String RPT_ID = "77777777777111111111111111111";
    public static final String PAYMENT_TOKEN = "paymentToken";
    public static final String DESCRIPTION = "description";
    public static final int AMOUNT = 100;
    public static final Confidential<Email> EMAIL;

    static {
        try {
            EMAIL = confidentialDataManager
                    .encrypt(ConfidentialDataManager.Mode.AES_GCM_NOPAD, new Email("foo@example.com"));
        } catch (InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchPaddingException
                | BadPaddingException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String FAULT_CODE = "";
    public static final String FAULT_CODE_STRING = "";
    public static final String PAYMENT_INSTRUMENT_ID = "paymentInstrumentId";
    public static final String PSP_ID = "pspId";
    public static final String PAYMENT_TYPE_CODE = "paymentTypeCode";
    public static final String PAYMENT_CONTEXT_CODE = "paymentContextCode";
    public static final String BROKER_NAME = "brokerName";
    public static final String PSP_CHANNEL_CODE = "pspChannelCode";
    public static final String PAYMENT_METHOD_NAME = "paymentMethodName";
    public static final String PSP_BUSINESS_NAME = "pspBusinessName";

    public static final String AUTHORIZATION_CODE = "authorizationCode";

    public static final AuthorizationResultDto AUTHORIZATION_RESULT_DTO = AuthorizationResultDto.OK;
    public static final String AUTHORIZATION_REQUEST_ID = UUID.randomUUID().toString();
    public static final String TRANSACTION_ID = UUID.randomUUID().toString();

    private static ConfidentialDataManager constructConfidentialDataManager() {
        byte[] key = new byte[16];
        new Random().nextBytes(key);

        return new ConfidentialDataManager(new SecretKeySpec(key, "AES"));
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent() {
        return new TransactionActivatedEvent(
                TRANSACTION_ID,
                new TransactionActivatedData(
                        EMAIL,
                        List.of(
                                new it.pagopa.ecommerce.commons.documents.v1.PaymentNotice(
                                        PAYMENT_TOKEN,
                                        RPT_ID,
                                        DESCRIPTION,
                                        AMOUNT,
                                        PAYMENT_CONTEXT_CODE
                                )
                        ),
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        Transaction.ClientId.CHECKOUT
                )
        );
    }

    @Nonnull
    public static TransactionActivated transactionActivated(String creationDate) {
        return new TransactionActivated(
                new TransactionId(UUID.fromString(TRANSACTION_ID)),
                List.of(
                        new it.pagopa.ecommerce.commons.domain.v1.PaymentNotice(
                                new PaymentToken(PAYMENT_TOKEN),
                                new RptId(RPT_ID),
                                new TransactionAmount(AMOUNT),
                                new TransactionDescription(DESCRIPTION),
                                new PaymentContextCode(PAYMENT_CONTEXT_CODE)
                        )
                ),
                EMAIL,
                FAULT_CODE,
                FAULT_CODE_STRING,
                ZonedDateTime.parse(creationDate),
                Transaction.ClientId.CHECKOUT
        );
    }

    @Nonnull
    public static TransactionWithRequestedAuthorization transactionWithRequestedAuthorization(
                                                                                              TransactionAuthorizationRequestedEvent authorizationRequestedEvent,
                                                                                              TransactionActivated transactionActivated
    ) {
        return new TransactionWithRequestedAuthorization(
                transactionActivated,
                authorizationRequestedEvent.getData()
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent() {
        return new TransactionAuthorizationRequestedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationRequestData(
                        AMOUNT,
                        10,
                        PAYMENT_INSTRUMENT_ID,
                        PSP_ID,
                        PAYMENT_TYPE_CODE,
                        BROKER_NAME,
                        PSP_CHANNEL_CODE,
                        PAYMENT_METHOD_NAME,
                        PSP_BUSINESS_NAME,
                        AUTHORIZATION_REQUEST_ID
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent() {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationCompletedData(AUTHORIZATION_CODE, AUTHORIZATION_RESULT_DTO)
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent(
                                                                                                AuthorizationResultDto authorizationResultDto
    ) {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationCompletedData(AUTHORIZATION_CODE, authorizationResultDto)
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompleted transactionAuthorizationCompleted(
                                                                                      TransactionAuthorizationCompletedEvent authorizedEvent,
                                                                                      TransactionWithRequestedAuthorization transactionWithRequestedAuthorization
    ) {
        return new TransactionAuthorizationCompleted(
                transactionWithRequestedAuthorization,
                authorizedEvent
        );
    }

    @Nonnull
    public static TransactionClosedEvent transactionClosedEvent(TransactionClosureData.Outcome outcome) {
        return new TransactionClosedEvent(
                TRANSACTION_ID,
                new TransactionClosureData(outcome)
        );
    }

    @Nonnull
    public static TransactionClosureErrorEvent transactionClosureErrorEvent(TransactionClosureData.Outcome outcome) {
        return new TransactionClosureErrorEvent(
                TRANSACTION_ID
        );
    }

    @Nonnull
    public static TransactionWithClosureError transactionWithClosureError(
                                                                          TransactionClosureErrorEvent transactionClosureErrorEvent,
                                                                          BaseTransaction transaction
    ) {
        return new TransactionWithClosureError(
                transaction,
                transactionClosureErrorEvent
        );
    }

    @Nonnull
    public static TransactionClosed transactionClosed(
                                                      BaseTransactionWithCompletedAuthorization transactionWithCompletedAuthorization,
                                                      TransactionClosedEvent transactionClosedEvent
    ) {
        return new TransactionClosed(
                transactionWithCompletedAuthorization,
                transactionClosedEvent
        );
    }

    @Nonnull
    public static TransactionWithUserReceiptOk transactionWithUserReceiptOk(
                                                                            BaseTransactionClosed baseTransactionClosed,
                                                                            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        return new TransactionWithUserReceiptOk(
                baseTransactionClosed,
                transactionUserReceiptAddedEvent
        );
    }

    @Nonnull
    public static TransactionWithUserReceiptKo transactionWithUserReceiptKo(
                                                                            BaseTransactionClosed baseTransactionClosed,
                                                                            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        return new TransactionWithUserReceiptKo(
                baseTransactionClosed,
                transactionUserReceiptAddedEvent
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent(
                                                                                    TransactionUserReceiptData.Outcome outcome
    ) {
        return new TransactionUserReceiptAddedEvent(
                TRANSACTION_ID,
                new TransactionUserReceiptData(outcome)
        );
    }

    @Nonnull
    public static TransactionExpired transactionExpired(
                                                        BaseTransaction transaction,
                                                        TransactionExpiredEvent expiredEvent
    ) {
        return new TransactionExpired(transaction, expiredEvent);
    }

    @Nonnull
    public static TransactionRefunded transactionRefunded(
                                                          BaseTransaction transaction,
                                                          TransactionRefundedEvent transactionRefundedEvent
    ) {
        return new TransactionRefunded(transaction, transactionRefundedEvent);
    }

    @Nonnull
    public static TransactionUnauthorized transactionUnauthorized(
                                                                  BaseTransactionWithCompletedAuthorization transaction,
                                                                  TransactionClosureFailedEvent transactionClosureFailedEvent
    ) {
        return new TransactionUnauthorized(transaction, transactionClosureFailedEvent);
    }

    @Nonnull
    public static TransactionUserCanceled transactionUserCanceled(
                                                                  BaseTransactionWithCancellationRequested transaction,
                                                                  TransactionClosedEvent transactionClosedEvent
    ) {
        return new TransactionUserCanceled(transaction, transactionClosedEvent);
    }

    @Nonnull
    public static TransactionWithCancellationRequested transactionWithCancellationRequested(
                                                                                            BaseTransactionWithPaymentToken baseTransaction,
                                                                                            TransactionUserCanceledEvent transactionUserCanceledEvent
    ) {
        return new TransactionWithCancellationRequested(baseTransaction, transactionUserCanceledEvent);
    }

    @Nonnull
    public static TransactionExpiredNotAuthorized transactionExpiredNotAuthorized(
                                                                                  BaseTransaction transaction,
                                                                                  TransactionExpiredEvent transactionExpiredEvent
    ) {
        return new TransactionExpiredNotAuthorized(transaction, transactionExpiredEvent);
    }

    @Nonnull
    public static TransactionWithRefundError transactionWithRefundError(
                                                                        BaseTransactionWithRefundRequested baseTransaction,
                                                                        TransactionRefundErrorEvent transactionRefundErrorEvent
    ) {
        return new TransactionWithRefundError(baseTransaction, transactionRefundErrorEvent);
    }

    @Nonnull
    public static TransactionWithRefundRequested transactionWithRefundRequested(
                                                                                BaseTransactionWithRequestedAuthorization baseTransaction,
                                                                                TransactionRefundRequestedEvent transactionRefundRequestedEvent
    ) {
        return new TransactionWithRefundRequested(
                baseTransaction,
                transactionRefundRequestedEvent
        );
    }

    @Nonnull
    public static TransactionRefundRetriedEvent transactionRefundRetriedEvent(int retryCount) {
        return new TransactionRefundRetriedEvent(
                TRANSACTION_ID,
                new TransactionRetriedData(retryCount)
        );
    }

    @Nonnull
    public static TransactionUserCanceledEvent transactionUserCanceledEvent() {
        return new TransactionUserCanceledEvent(
                TRANSACTION_ID
        );
    }

    @Nonnull
    public static TransactionClosureFailedEvent transactionClosureFailedEvent(TransactionClosureData.Outcome outcome) {
        return new TransactionClosureFailedEvent(
                TRANSACTION_ID,
                new TransactionClosureData(outcome)
        );
    }

    @Nonnull
    public static TransactionClosureRetriedEvent transactionClosureRetriedEvent(int retryCount) {
        return new TransactionClosureRetriedEvent(
                TRANSACTION_ID,
                new TransactionRetriedData(retryCount)
        );
    }

    @Nonnull
    public static TransactionExpiredEvent transactionExpiredEvent(BaseTransaction baseTransaction) {
        return new TransactionExpiredEvent(
                TRANSACTION_ID,
                new TransactionExpiredData(baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static TransactionRefundedEvent transactionRefundedEvent(BaseTransaction baseTransaction) {
        return new TransactionRefundedEvent(
                TRANSACTION_ID,
                new TransactionRefundedData(baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static TransactionRefundRequestedEvent transactionRefundRequestedEvent(
                                                                                  BaseTransaction baseTransaction
    ) {
        return new TransactionRefundRequestedEvent(
                TRANSACTION_ID,
                new TransactionRefundedData(baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static TransactionRefundErrorEvent transactionRefundErrorEvent(BaseTransaction baseTransaction) {
        return new TransactionRefundErrorEvent(
                TRANSACTION_ID,
                new TransactionRefundedData(baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static Transaction transactionDocument(
                                                  TransactionStatusDto transactionStatus,
                                                  ZonedDateTime creationDateTime
    ) {
        return new Transaction(
                TRANSACTION_ID,
                List.of(
                        new it.pagopa.ecommerce.commons.documents.v1.PaymentNotice(
                                PAYMENT_TOKEN,
                                RPT_ID,
                                DESCRIPTION,
                                AMOUNT,
                                PAYMENT_CONTEXT_CODE
                        )
                ),
                null,
                EMAIL,
                transactionStatus,
                Transaction.ClientId.CHECKOUT,
                creationDateTime.toString()
        );
    }

}
