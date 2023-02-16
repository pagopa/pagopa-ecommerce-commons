package it.pagopa.ecommerce.commons.v1;

import it.pagopa.ecommerce.commons.documents.v1.Transaction;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class TransactionTestUtils {

    private TransactionTestUtils() {
        // helper class with only static methods, no need to instantiate it
    }

    public static final String RPT_ID = "77777777777111111111111111111";
    public static final String PAYMENT_TOKEN = "paymentToken";
    public static final String DESCRIPTION = "description";
    public static final int AMOUNT = 100;
    public static final String EMAIL = "foo@example.com";
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
                        Transaction.ClientId.UNKNOWN
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
                new Email(EMAIL),
                FAULT_CODE,
                FAULT_CODE_STRING,
                ZonedDateTime.parse(creationDate),
                Transaction.ClientId.UNKNOWN
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
    public static TransactionClosedEvent transactionClosedEvent() {
        return new TransactionClosedEvent(
                TRANSACTION_ID
        );
    }

    @Nonnull
    public static TransactionClosureErrorEvent transactionClosureErrorEvent() {
        return new TransactionClosureErrorEvent(
                TRANSACTION_ID
        );
    }

    @Nonnull
    public static TransactionWithClosureError transactionWithClosureError(
                                                                          TransactionClosureErrorEvent transactionClosureErrorEvent,
                                                                          BaseTransactionWithCompletedAuthorization transaction
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
    public static TransactionWithUserReceipt transactionWithUserReceipt(
                                                                        BaseTransactionWithCompletedAuthorization baseTransactionClosed,
                                                                        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        return new TransactionWithUserReceipt(
                baseTransactionClosed,
                transactionUserReceiptAddedEvent
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent() {
        return new TransactionUserReceiptAddedEvent(
                TRANSACTION_ID
        );
    }

    @Nonnull
    public static TransactionExpired transactionExpired(
                                                        TransactionExpiredEvent expiredEvent,
                                                        BaseTransaction transaction
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
                                                                  TransactionClosureFailedEvent transactionClosureFailedEven
    ) {
        return new TransactionUnauthorized(transaction, transactionClosureFailedEven);
    }

    @Nonnull
    public static TransactionUserCanceled transactionUserCanceled(
                                                                  BaseTransaction transaction,
                                                                  TransactionUserCanceledEvent transactionUserCanceledEvent
    ) {
        return new TransactionUserCanceled(transaction, transactionUserCanceledEvent);
    }

    @Nonnull
    public static TransactionExpiredNotAuthorized transactionExpiredNotAuthorized(
                                                                                  BaseTransaction transaction,
                                                                                  TransactionExpiredEvent transactionExpiredEvent
    ) {
        return new TransactionExpiredNotAuthorized(transaction, transactionExpiredEvent);
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
    public static TransactionClosureFailedEvent transactionClosureFailedEvent() {
        return new TransactionClosureFailedEvent(
                TRANSACTION_ID
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
    public static TransactionExpiredEvent transactionExpiredEvent(TransactionStatusDto previousStatus) {
        return new TransactionExpiredEvent(
                TRANSACTION_ID,
                new TransactionExpiredData(previousStatus)
        );
    }

    @Nonnull
    public static TransactionRefundedEvent transactionRefundedEvent(TransactionStatusDto statusBeforeRefunded) {
        return new TransactionRefundedEvent(
                TRANSACTION_ID,
                new TransactionRefundedData(statusBeforeRefunded)
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
                Transaction.ClientId.UNKNOWN,
                creationDateTime.toString()
        );
    }
}
