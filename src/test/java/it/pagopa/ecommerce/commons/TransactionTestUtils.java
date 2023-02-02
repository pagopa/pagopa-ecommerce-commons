package it.pagopa.ecommerce.commons;

import it.pagopa.ecommerce.commons.documents.Transaction;
import it.pagopa.ecommerce.commons.documents.*;
import it.pagopa.ecommerce.commons.domain.*;
import it.pagopa.ecommerce.commons.domain.PaymentNotice;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionClosed;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class TransactionTestUtils {

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
    public static final String AUTHORIZATION_REQUEST_ID = "authorizationRequestId";
    public static final String TRANSACTION_ID = UUID.randomUUID().toString();

    @Nonnull
    public static TransactionActivationRequested transactionActivationRequested(String creationDate) {
        return new TransactionActivationRequested(
                new TransactionId(UUID.fromString(TRANSACTION_ID)),
                Arrays.asList(
                        new PaymentNotice(
                                new PaymentToken(null),
                                new RptId(RPT_ID),
                                new TransactionAmount(AMOUNT),
                                new TransactionDescription(DESCRIPTION),
                                new PaymentContextCode(PAYMENT_CONTEXT_CODE)
                        )
                ),
                new Email(EMAIL),
                ZonedDateTime.parse(creationDate),
                Transaction.ClientId.UNKNOWN
        );
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent() {
        return new TransactionActivatedEvent(
                TRANSACTION_ID,
                new TransactionActivatedData(
                        EMAIL,
                        Arrays.asList(
                                new it.pagopa.ecommerce.commons.documents.PaymentNotice(
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
                Arrays.asList(
                        new PaymentNotice(
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
    public static TransactionAuthorizationStatusUpdatedEvent transactionAuthorizationStatusUpdatedEvent(
                                                                                                        AuthorizationResultDto authorizationResult
    ) {
        TransactionStatusDto newStatus;
        switch (authorizationResult) {
            case OK -> newStatus = TransactionStatusDto.AUTHORIZED;
            case KO -> newStatus = TransactionStatusDto.AUTHORIZATION_FAILED;
            default -> throw new IllegalStateException("Unexpected value: " + authorizationResult);
        }

        return new TransactionAuthorizationStatusUpdatedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationStatusUpdateData(authorizationResult, newStatus, "authorizationCode")
        );
    }

    @Nonnull
    public static TransactionWithCompletedAuthorization transactionWithCompletedAuthorization(
                                                                                              TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent,
                                                                                              TransactionWithRequestedAuthorization transactionWithRequestedAuthorization
    ) {
        return new TransactionWithCompletedAuthorization(
                transactionWithRequestedAuthorization,
                authorizationStatusUpdatedEvent.getData()
        );
    }

    @Nonnull
    public static TransactionClosureSentEvent transactionClosureSentEvent(
                                                                          ClosePaymentResponseDto.OutcomeEnum closePaymentOutcome
    ) {
        TransactionStatusDto newStatus;
        switch (closePaymentOutcome) {
            case OK -> newStatus = TransactionStatusDto.CLOSED;
            case KO -> newStatus = TransactionStatusDto.CLOSURE_FAILED;
            default -> throw new IllegalStateException("Unexpected value: " + closePaymentOutcome);
        }

        return new TransactionClosureSentEvent(
                TRANSACTION_ID,
                new TransactionClosureSendData(closePaymentOutcome, newStatus)
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
                                                                          TransactionWithCompletedAuthorization transaction
    ) {
        return new TransactionWithClosureError(
                transaction,
                transactionClosureErrorEvent
        );
    }

    @Nonnull
    public static TransactionClosed transactionClosed(
                                                      TransactionClosureSentEvent closureSentEvent,
                                                      BaseTransactionWithCompletedAuthorization transactionWithCompletedAuthorization
    ) {
        return new TransactionClosed(
                transactionWithCompletedAuthorization,
                closureSentEvent.getData()
        );
    }

    @Nonnull
    public static TransactionWithUserReceipt transactionWithUserReceipt(
                                                                        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent,
                                                                        BaseTransactionClosed baseTransactionClosed
    ) {
        return new TransactionWithUserReceipt(
                baseTransactionClosed,
                transactionUserReceiptAddedEvent.getData()
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent(
                                                                                    TransactionStatusDto transactionStatusDto
    ) {
        return new TransactionUserReceiptAddedEvent(
                TRANSACTION_ID,
                new TransactionAddReceiptData(
                        Stream.of(transactionStatusDto)
                                .filter(
                                        transactionStatus -> transactionStatus.equals(TransactionStatusDto.NOTIFIED)
                                                || transactionStatus.equals(TransactionStatusDto.NOTIFIED_FAILED)
                                ).findFirst().orElseThrow()
                )
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
    public static TransactionClosureRetriedEvent transactionClosureRetriedEvent(int retryCount) {
        return new TransactionClosureRetriedEvent(
                TRANSACTION_ID,
                new TransactionRetriedData(retryCount)
        );
    }

    @Nonnull
    public static TransactionActivationRequestedEvent transactionActivationRequestedEvent() {
        return new TransactionActivationRequestedEvent(
                TRANSACTION_ID,
                ZonedDateTime.now().toString(),
                new TransactionActivationRequestedData(
                        Arrays.asList(
                                new it.pagopa.ecommerce.commons.documents.PaymentNotice(
                                        null,
                                        RPT_ID,
                                        DESCRIPTION,
                                        AMOUNT,
                                        PAYMENT_CONTEXT_CODE
                                )
                        ),
                        EMAIL,
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        Transaction.ClientId.UNKNOWN
                )
        );
    }

    @Nonnull
    public static Transaction transactionDocument(
                                                  TransactionStatusDto transactionStatus,
                                                  ZonedDateTime creationDateTime
    ) {
        return new Transaction(
                TRANSACTION_ID,
                PAYMENT_TOKEN,
                RPT_ID,
                DESCRIPTION,
                AMOUNT,
                EMAIL,
                transactionStatus,
                creationDateTime
        );
    }
}
