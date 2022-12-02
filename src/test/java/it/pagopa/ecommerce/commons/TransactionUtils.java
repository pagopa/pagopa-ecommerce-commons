package it.pagopa.ecommerce.commons;

import it.pagopa.ecommerce.commons.domain.*;
import it.pagopa.ecommerce.commons.domain.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.events.v1.*;
import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.generated.transactions.server.model.TransactionStatusDto;

import javax.annotation.Nonnull;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public class TransactionUtils {

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
    public static final String BROKER_NAME = "brokerName";
    public static final String PSP_CHANNEL_CODE = "pspChannelCode";
    public static final String PAYMENT_METHOD_NAME = "paymentMethodName";
    public static final String PSP_BUSINESS_NAME = "pspBusinessName";
    public static final String AUTHORIZATION_REQUEST_ID = "authorizationRequestId";
    public static final String TRANSACTION_ID = UUID.randomUUID().toString();

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent() {
        return new TransactionActivatedEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionActivatedEvent.TransactionEventCode.TRANSACTION_ACTIVATED_EVENT,
                ZonedDateTime.now(),
                new TransactionActivatedData(DESCRIPTION, AMOUNT, EMAIL, FAULT_CODE, FAULT_CODE_STRING, PAYMENT_TOKEN)
        );
    }

    @Nonnull
    public static TransactionActivated transactionActivated(String creationDate) {
        return new TransactionActivated(
                new TransactionId(UUID.fromString(TRANSACTION_ID)),
                new PaymentToken(PAYMENT_TOKEN),
                new RptId(RPT_ID),
                new TransactionDescription(DESCRIPTION),
                new TransactionAmount(AMOUNT),
                new Email(EMAIL),
                FAULT_CODE,
                FAULT_CODE_STRING,
                ZonedDateTime.parse(creationDate),
                TransactionStatusDto.ACTIVATED
        );
    }

    @Nonnull
    public static TransactionWithRequestedAuthorization transactionWithRequestedAuthorization(
                                                                                              TransactionAuthorizationRequestedEvent authorizationRequestedEvent,
                                                                                              TransactionActivated transactionActivated
    ) {
        return new TransactionWithRequestedAuthorization(
                transactionActivated.withStatus(TransactionStatusDto.AUTHORIZATION_REQUESTED),
                authorizationRequestedEvent.getData()
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent() {
        return new TransactionAuthorizationRequestedEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionAuthorizationRequestedEvent.TransactionEventCode.TRANSACTION_AUTHORIZATION_REQUESTED,
                ZonedDateTime.now(),
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
                                                                                                        TransactionAuthorizationStatusUpdateData.AuthorizationResult authorizationResult
    ) {
        TransactionAuthorizationStatusUpdateData.TransactionStatus newStatus;
        switch (authorizationResult) {
            case OK -> newStatus = TransactionAuthorizationStatusUpdateData.TransactionStatus.AUTHORIZED;
            case KO -> newStatus = TransactionAuthorizationStatusUpdateData.TransactionStatus.AUTHORIZATION_FAILED;
            default -> throw new IllegalStateException("Unexpected value: " + authorizationResult);
        }

        return new TransactionAuthorizationStatusUpdatedEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionAuthorizationStatusUpdatedEvent.TransactionEventCode.TRANSACTION_AUTHORIZATION_STATUS_UPDATED,
                ZonedDateTime.now(),
                new TransactionAuthorizationStatusUpdateData(authorizationResult, newStatus)
        );
    }

    @Nonnull
    public static TransactionWithCompletedAuthorization transactionWithCompletedAuthorization(
                                                                                              TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent,
                                                                                              TransactionWithRequestedAuthorization transactionWithRequestedAuthorization
    ) {
        return new TransactionWithCompletedAuthorization(
                transactionWithRequestedAuthorization.withStatus(TransactionStatusDto.AUTHORIZED),
                authorizationStatusUpdatedEvent.getData()
        );
    }

    @Nonnull
    public static TransactionClosureSentEvent transactionClosureSentEvent(
                                                                          ClosePaymentResponseDto.OutcomeEnum closePaymentOutcome
    ) {
        TransactionAuthorizationStatusUpdateData.TransactionStatus newStatus;
        TransactionClosureSendData.ClosepaymentResponseOutcome transactionClosePaymentOutcome = TransactionClosureSendData.ClosepaymentResponseOutcome
                .fromValue(closePaymentOutcome.getValue());
        switch (transactionClosePaymentOutcome) {
            case OK -> newStatus = TransactionAuthorizationStatusUpdateData.TransactionStatus.CLOSED;
            case KO -> newStatus = TransactionAuthorizationStatusUpdateData.TransactionStatus.CLOSURE_FAILED;
            default -> throw new IllegalStateException("Unexpected value: " + closePaymentOutcome);
        }

        return new TransactionClosureSentEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionClosureSentEvent.TransactionEventCode.TRANSACTION_CLOSURE_SENT_EVENT,
                ZonedDateTime.now(),
                new TransactionClosureSendData(transactionClosePaymentOutcome, newStatus, "authorizationCode")
        );
    }

    @Nonnull
    public static TransactionClosureErrorEvent transactionClosureErrorEvent() {
        return new TransactionClosureErrorEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionClosureErrorEvent.TransactionEventCode.TRANSACTION_CLOSURE_ERROR_EVENT,
                ZonedDateTime.now(),
                null
        );
    }

    @Nonnull
    public static TransactionWithClosureError transactionWithClosureError(
                                                                          TransactionClosureErrorEvent transactionClosureErrorEvent,
                                                                          TransactionWithCompletedAuthorization transaction
    ) {
        return new TransactionWithClosureError(
                transaction.withStatus(TransactionStatusDto.CLOSURE_ERROR),
                transactionClosureErrorEvent
        );
    }

    @Nonnull
    public static TransactionClosed transactionClosed(
                                                      TransactionClosureSentEvent closureSentEvent,
                                                      BaseTransactionWithCompletedAuthorization transactionWithCompletedAuthorization
    ) {
        TransactionStatusDto newStatus = TransactionStatusDto
                .fromValue(closureSentEvent.getData().getNewTransactionStatus().value());

        return new TransactionClosed(
                ((BaseTransactionWithCompletedAuthorization) transactionWithCompletedAuthorization
                        .withStatus(newStatus)),
                closureSentEvent.getData()
        );
    }

    @Nonnull
    public static TransactionActivationRequestedEvent transactionActivationRequestedEvent() {
        return new TransactionActivationRequestedEvent(
                Version.V_1,
                UUID.randomUUID().toString(),
                TRANSACTION_ID,
                RPT_ID,
                PAYMENT_TOKEN,
                TransactionActivationRequestedEvent.TransactionEventCode.TRANSACTION_ACTIVATION_REQUESTED,
                ZonedDateTime.now(),
                new TransactionActivationRequestData(
                        DESCRIPTION,
                        AMOUNT,
                        EMAIL,
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        "paymentContextCode"
                )
        );
    }
}
