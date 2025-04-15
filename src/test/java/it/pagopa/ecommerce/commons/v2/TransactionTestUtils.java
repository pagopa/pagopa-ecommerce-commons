package it.pagopa.ecommerce.commons.v2;

import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.documents.v2.Transaction;
import it.pagopa.ecommerce.commons.documents.v2.activation.EmptyTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.activation.NpgTransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.activation.TransactionGatewayActivationData;
import it.pagopa.ecommerce.commons.documents.v2.authorization.*;
import it.pagopa.ecommerce.commons.documents.v2.refund.EmptyGatewayRefundData;
import it.pagopa.ecommerce.commons.documents.v2.refund.GatewayRefundData;
import it.pagopa.ecommerce.commons.domain.*;
import it.pagopa.ecommerce.commons.domain.v2.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.*;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.repositories.PaymentRequestInfo;

import javax.annotation.Nonnull;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static final String EMAIL_STRING = "foo@example.com";
    public static final String FISCAL_CODE_STRING = "test";

    private static final String timestampOperation = "2023-01-01T01:02:03+01:00";

    public static final Confidential<Email> EMAIL = new Confidential<>(
            UUID.randomUUID().toString()
    );

    public static final String FAULT_CODE = "";
    public static final String FAULT_CODE_STRING = "";
    public static final String PAYMENT_INSTRUMENT_ID = "paymentInstrumentId";
    public static final String PSP_ID = "pspId";
    public static final String PAYMENT_TYPE_CODE = "CP";
    public static final String PAYMENT_CONTEXT_CODE = "paymentContextCode";
    public static final String BROKER_NAME = "brokerName";
    public static final String PSP_CHANNEL_CODE = "pspChannelCode";
    public static final String PAYMENT_METHOD_NAME = "CARDS";
    public static final String PAYMENT_METHOD_DESCRIPTION = "paymentMethodDescription";
    public static final String PSP_BUSINESS_NAME = "pspBusinessName";
    public static final String AUTHORIZATION_CODE = "authorizationCode";
    public static final String RRN = "rrn";
    public static final AuthorizationResultDto AUTHORIZATION_RESULT_DTO = AuthorizationResultDto.OK;
    public static final String AUTHORIZATION_REQUEST_ID = UUID.randomUUID().toString();

    public static final TransactionAuthorizationRequestData.PaymentGateway PAYMENT_GATEWAY = TransactionAuthorizationRequestData.PaymentGateway.NPG;
    public static final String TRANSACTION_ID = UUID.randomUUID().toString().replace("-", "");
    public static final String TRANSFER_PA_FISCAL_CODE = "transferPAFiscalCode";
    public static final Boolean TRANSFER_DIGITAL_STAMP = true;
    public static final Integer TRANSFER_AMOUNT = 0;
    public static final String TRANSFER_CATEGORY = "transferCategory";
    public static final String ID_CART = "ecIdCart";
    public static final String LANGUAGE = "it-IT";
    public static final URI LOGO_URI;

    public static final boolean IS_ALL_CCP_FALSE = false;

    public static final String USER_ID = UUID.randomUUID().toString();

    public static final String NPG_VALIDATION_SERVICE_ID = "validationServiceId";

    static {
        try {
            LOGO_URI = new URI("http://paymentMethodLogo.it");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String PAYMENT_DATE = ZonedDateTime.now().toString();

    public static final String RECEIVING_OFFICE_NAME = "receivingOfficeName";

    public static final String PAYMENT_DESCRIPTION = "paymentDescription";

    public static final String PA_FISCAL_CODE = "paFiscalCode";

    public static final String PA_NAME = "paName";

    public static final String DUE_DATE = "1900-01-01";
    public static final String IDEMPOTENCY_KEY = "00000000000_AABBCCDDEE";

    public static final int PAYMENT_TOKEN_VALIDITY_TIME_SEC = 900;

    public static final String NPG_ORDER_ID = "npgOrderId";

    public static final String NPG_CORRELATION_ID = "npgCorrelationId";

    public static final String NPG_SESSION_ID = "npgSessionId";

    public static final String NPG_OPERATION_ID = "npgOperationId";
    public static final String NPG_PAYMENT_END_TO_END_ID = "npgPaymentEndToEndId";

    public static final String NPG_CONFIRM_PAYMENT_SESSION_ID = "npgConfirmPaymentSessionId";

    public static final int REDIRECT_AUTHORIZATION_TIMEOUT = 60000;

    public static final String NPG_WALLET_CARD_BIN = "12345678";
    public static final String NPG_WALLET_CARD_LAST_FOUR_DIGITS = "1234";
    public static final String NPG_WALLET_PAYPAL_MASKED_EMAIL = "test**********@test********.it";

    public static final String NPG_WALLET_ID = UUID.randomUUID().toString();

    public static final String COMPANY_NAME = "companyName";
    public static final String CREDITOR_REFERENCE_ID = "222222222222";

    public static final String ID_BUNDLE = "idBundle";

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent() {
        return transactionActivateEvent(new EmptyTransactionGatewayActivationData());
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(
                                                                     TransactionGatewayActivationData transactionActivatedData
    ) {
        return transactionActivateEvent(ZonedDateTime.now().toString(), transactionActivatedData);
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(
                                                                     String creationDate,
                                                                     TransactionGatewayActivationData transactionActivatedData
    ) {
        return transactionActivateEvent(creationDate, transactionActivatedData, USER_ID);
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(
                                                                     String creationDate,
                                                                     TransactionGatewayActivationData transactionActivatedData,
                                                                     String userId
    ) {
        return transactionActivateEvent(creationDate, transactionActivatedData, userId, Transaction.ClientId.CHECKOUT);
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(
                                                                     String creationDate,
                                                                     TransactionGatewayActivationData transactionActivatedData,
                                                                     String userId,
                                                                     Transaction.ClientId clientId
    ) {
        return transactionActivateEvent(
                creationDate,
                transactionActivatedData,
                userId,
                clientId,
                CREDITOR_REFERENCE_ID
        );
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(
                                                                     String creationDate,
                                                                     TransactionGatewayActivationData transactionActivatedData,
                                                                     String userId,
                                                                     Transaction.ClientId clientId,
                                                                     String creditorReferenceId
    ) {
        return new TransactionActivatedEvent(
                TRANSACTION_ID,
                creationDate,
                new TransactionActivatedData(
                        EMAIL,
                        List.of(
                                new it.pagopa.ecommerce.commons.documents.PaymentNotice(
                                        PAYMENT_TOKEN,
                                        RPT_ID,
                                        DESCRIPTION,
                                        AMOUNT,
                                        PAYMENT_CONTEXT_CODE,
                                        List.of(
                                                new PaymentTransferInformation(
                                                        TRANSFER_PA_FISCAL_CODE,
                                                        TRANSFER_DIGITAL_STAMP,
                                                        TRANSFER_AMOUNT,
                                                        TRANSFER_CATEGORY
                                                )
                                        ),
                                        IS_ALL_CCP_FALSE,
                                        COMPANY_NAME,
                                        creditorReferenceId
                                )
                        ),
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        clientId,
                        ID_CART,
                        PAYMENT_TOKEN_VALIDITY_TIME_SEC,
                        transactionActivatedData,
                        userId
                )
        );
    }

    @Nonnull
    public static TransactionActivated transactionActivated(
                                                            String creationDate
    ) {
        return transactionActivated(creationDate, new EmptyTransactionGatewayActivationData());
    }

    @Nonnull
    public static TransactionActivated transactionActivated(
                                                            String creationDate,
                                                            TransactionGatewayActivationData transactionActivatedData
    ) {
        return new TransactionActivated(
                new TransactionId(TRANSACTION_ID),
                List.of(
                        new it.pagopa.ecommerce.commons.domain.PaymentNotice(
                                new PaymentToken(PAYMENT_TOKEN),
                                new RptId(RPT_ID),
                                new TransactionAmount(AMOUNT),
                                new TransactionDescription(DESCRIPTION),
                                new PaymentContextCode(PAYMENT_CONTEXT_CODE),
                                List.of(
                                        new PaymentTransferInfo(
                                                TRANSFER_PA_FISCAL_CODE,
                                                TRANSFER_DIGITAL_STAMP,
                                                TRANSFER_AMOUNT,
                                                TRANSFER_CATEGORY
                                        )
                                ),
                                IS_ALL_CCP_FALSE,
                                new CompanyName(COMPANY_NAME),
                                CREDITOR_REFERENCE_ID
                        )
                ),
                EMAIL,
                FAULT_CODE,
                FAULT_CODE_STRING,
                ZonedDateTime.parse(creationDate),
                Transaction.ClientId.CHECKOUT,
                ID_CART,
                PAYMENT_TOKEN_VALIDITY_TIME_SEC,
                transactionActivatedData,
                USER_ID
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
        return transactionAuthorizationRequestedEvent(
                PAYMENT_GATEWAY,
                new NpgTransactionGatewayAuthorizationRequestedData(
                        LOGO_URI,
                        "VISA",
                        "sessionId",
                        "confirmPaymentSessionId",
                        null
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent(
                                                                                                TransactionGatewayAuthorizationRequestedData transactionGatewayAuthorizationRequestedData
    ) {
        return transactionAuthorizationRequestedEvent(
                PAYMENT_GATEWAY,
                transactionGatewayAuthorizationRequestedData
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent(
                                                                                                TransactionAuthorizationRequestData.PaymentGateway paymentGateway
    ) {
        return transactionAuthorizationRequestedEvent(
                paymentGateway,
                new NpgTransactionGatewayAuthorizationRequestedData(
                        LOGO_URI,
                        "VISA",
                        "sessionId",
                        null,
                        null
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent(
                                                                                                TransactionAuthorizationRequestData.PaymentGateway paymentGateway,
                                                                                                TransactionGatewayAuthorizationRequestedData transactionGatewayAuthorizationRequestedData
    ) {
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
                        false,
                        AUTHORIZATION_REQUEST_ID,
                        paymentGateway,
                        PAYMENT_METHOD_DESCRIPTION,
                        transactionGatewayAuthorizationRequestedData,
                        ID_BUNDLE
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent(
                                                                                                TransactionGatewayAuthorizationData transactionGatewayAuthorizationData
    ) {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationCompletedData(
                        AUTHORIZATION_CODE,
                        RRN,
                        timestampOperation,
                        transactionGatewayAuthorizationData
                )
        );
    }

    @Nonnull
    public static TransactionClosureRequestedEvent transactionClosureRequestedEvent() {
        return new TransactionClosureRequestedEvent(TRANSACTION_ID);
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
    public static TransactionWithClosureRequested transactionWithClosureRequested(
                                                                                  TransactionAuthorizationCompleted transactionAuthorizationCompleted

    ) {
        return new TransactionWithClosureRequested(
                transactionAuthorizationCompleted
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
    public static TransactionClosureErrorEvent transactionClosureErrorEvent() {
        return transactionClosureErrorEvent(null);
    }

    @Nonnull
    public static TransactionClosureErrorEvent transactionClosureErrorEvent(ClosureErrorData closureErrorData) {
        return new TransactionClosureErrorEvent(
                TRANSACTION_ID,
                closureErrorData
        );
    }

    @Nonnull
    public static TransactionWithClosureError transactionWithClosureError(
                                                                          TransactionClosureErrorEvent transactionClosureErrorEvent,
                                                                          BaseTransactionWithPaymentToken transaction
    ) {
        return new TransactionWithClosureError(
                transaction,
                transactionClosureErrorEvent
        );
    }

    @Nonnull
    public static TransactionClosed transactionClosed(
                                                      BaseTransactionWithClosureRequested baseTransactionWithClosureRequested,
                                                      TransactionClosedEvent transactionClosedEvent
    ) {
        return new TransactionClosed(
                baseTransactionWithClosureRequested,
                transactionClosedEvent
        );
    }

    @Nonnull
    public static TransactionWithUserReceiptOk transactionWithUserReceiptOk(
                                                                            BaseTransactionWithRequestedUserReceipt baseTransaction,
                                                                            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        return new TransactionWithUserReceiptOk(
                baseTransaction,
                transactionUserReceiptAddedEvent
        );
    }

    @Nonnull
    public static TransactionWithUserReceiptKo transactionWithUserReceiptKo(
                                                                            BaseTransactionWithRequestedUserReceipt baseTransaction,
                                                                            TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent
    ) {
        return new TransactionWithUserReceiptKo(
                baseTransaction,
                transactionUserReceiptAddedEvent
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent(
                                                                                    TransactionUserReceiptData data
    ) {
        return new TransactionUserReceiptAddedEvent(
                TRANSACTION_ID,
                data
        );
    }

    public static TransactionUserReceiptData transactionUserReceiptData(TransactionUserReceiptData.Outcome outcome) {
        return new TransactionUserReceiptData(
                outcome,
                LANGUAGE,
                PAYMENT_DATE
        );
    }

    @Nonnull
    public static TransactionExpired transactionExpired(
                                                        BaseTransactionWithRequestedAuthorization transaction,
                                                        TransactionExpiredEvent expiredEvent
    ) {
        return new TransactionExpired(transaction, expiredEvent);
    }

    @Nonnull
    public static TransactionRefunded transactionRefunded(
                                                          BaseTransactionWithRefundRequested transaction,
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
    public static TransactionCancellationExpired transactionCancellationExpired(
                                                                                BaseTransactionWithCancellationRequested baseTransaction,
                                                                                TransactionExpiredEvent transactionExpiredEvent
    ) {
        return new TransactionCancellationExpired(baseTransaction, transactionExpiredEvent);
    }

    @Nonnull
    public static TransactionExpiredNotAuthorized transactionExpiredNotAuthorized(
                                                                                  BaseTransactionWithPaymentToken transaction,
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
    public static TransactionWithRefundError transactionWithRefundError(
                                                                        TransactionWithRefundError baseTransaction,
                                                                        TransactionGatewayAuthorizationData transactionGatewayAuthorizationData
    ) {
        return new TransactionWithRefundError(baseTransaction, transactionGatewayAuthorizationData);
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
        return transactionRefundRetriedEvent(retryCount, null);
    }

    @Nonnull
    public static TransactionRefundRetriedEvent transactionRefundRetriedEvent(
                                                                              int retryCount,
                                                                              TransactionGatewayAuthorizationData transactionGatewayAuthorizationData
    ) {
        return new TransactionRefundRetriedEvent(
                TRANSACTION_ID,
                new TransactionRefundRetriedData(transactionGatewayAuthorizationData, retryCount)
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
        return transactionClosureRetriedEvent(retryCount, null);
    }

    @Nonnull
    public static TransactionClosureRetriedEvent transactionClosureRetriedEvent(
                                                                                int retryCount,
                                                                                ClosureErrorData closureErrorData
    ) {
        return new TransactionClosureRetriedEvent(
                TRANSACTION_ID,
                new TransactionClosureRetriedData(closureErrorData, retryCount)
        );
    }

    @Nonnull
    public static TransactionAuthorizationOutcomeWaitingEvent transactionAuthorizationOutcomeWaitingEvent(
                                                                                                          int retryCount
    ) {
        return new TransactionAuthorizationOutcomeWaitingEvent(
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
    public static TransactionExpiredEvent transactionExpiredEvent(TransactionStatusDto transactionStatusDto) {
        return new TransactionExpiredEvent(
                TRANSACTION_ID,
                new TransactionExpiredData(transactionStatusDto)
        );
    }

    @Nonnull
    public static TransactionRefundedEvent transactionRefundedEvent(BaseTransaction baseTransaction) {
        return transactionRefundedEvent(baseTransaction, new EmptyGatewayRefundData());
    }

    @Nonnull
    public static TransactionRefundedEvent transactionRefundedEvent(
                                                                    BaseTransaction baseTransaction,
                                                                    GatewayRefundData gatewayRefundData
    ) {
        return new TransactionRefundedEvent(
                TRANSACTION_ID,
                new TransactionRefundedData(gatewayRefundData, baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static TransactionRefundRequestedEvent transactionRefundRequestedEvent(
                                                                                  BaseTransaction baseTransaction
    ) {
        return transactionRefundRequestedEvent(baseTransaction, null);
    }

    @Nonnull
    public static TransactionRefundRequestedEvent transactionRefundRequestedEvent(
                                                                                  BaseTransaction baseTransaction,
                                                                                  TransactionGatewayAuthorizationData gatewayAuthorizationData
    ) {
        return new TransactionRefundRequestedEvent(
                TRANSACTION_ID,
                new TransactionRefundRequestedData(gatewayAuthorizationData, baseTransaction.getStatus())
        );
    }

    @Nonnull
    public static TransactionRefundErrorEvent transactionRefundErrorEvent(BaseTransaction baseTransaction) {
        return transactionRefundErrorEvent();
    }

    @Nonnull
    public static TransactionRefundErrorEvent transactionRefundErrorEvent() {
        return new TransactionRefundErrorEvent(
                TRANSACTION_ID,
                new TransactionRefundErrorData()
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddErrorEvent transactionUserReceiptAddErrorEvent(
                                                                                          TransactionUserReceiptData data
    ) {
        return new TransactionUserReceiptAddErrorEvent(
                TRANSACTION_ID,
                data
        );
    }

    @Nonnull
    public static TransactionUserReceiptAddRetriedEvent transactionUserReceiptAddRetriedEvent(int retryCount) {
        return new TransactionUserReceiptAddRetriedEvent(
                TRANSACTION_ID,
                new TransactionRetriedData(retryCount)
        );
    }

    @Nonnull
    public static TransactionWithUserReceiptError transactionWithUserReceiptError(
                                                                                  BaseTransactionWithRequestedUserReceipt baseTransaction,
                                                                                  TransactionUserReceiptAddErrorEvent transactionUserReceiptAddErrorEvent
    ) {
        return new TransactionWithUserReceiptError(
                baseTransaction,
                transactionUserReceiptAddErrorEvent
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
                        new it.pagopa.ecommerce.commons.documents.PaymentNotice(
                                PAYMENT_TOKEN,
                                RPT_ID,
                                DESCRIPTION,
                                AMOUNT,
                                PAYMENT_CONTEXT_CODE,
                                List.of(
                                        new PaymentTransferInformation(
                                                TRANSFER_PA_FISCAL_CODE,
                                                TRANSFER_DIGITAL_STAMP,
                                                TRANSFER_AMOUNT,
                                                TRANSFER_CATEGORY
                                        )
                                ),
                                IS_ALL_CCP_FALSE,
                                COMPANY_NAME,
                                CREDITOR_REFERENCE_ID
                        )
                ),
                null,
                EMAIL,
                transactionStatus,
                Transaction.ClientId.CHECKOUT,
                creationDateTime.toString(),
                ID_CART,
                RRN,
                USER_ID,
                null,
                null
        );
    }

    @Nonnull
    public static TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt(
                                                                                          BaseTransactionClosed baseTransactionClosed,
                                                                                          TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent
    ) {
        return new TransactionWithRequestedUserReceipt(baseTransactionClosed, transactionUserReceiptRequestedEvent);
    }

    @Nonnull
    public static TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent(
                                                                                            TransactionUserReceiptData transactionUserReceiptData
    ) {
        return new TransactionUserReceiptRequestedEvent(
                TRANSACTION_ID,
                transactionUserReceiptData
        );
    }

    public static PaymentRequestInfo paymentRequestInfo() {
        return new PaymentRequestInfo(
                new RptId(RPT_ID),
                PA_FISCAL_CODE,
                PA_NAME,
                DESCRIPTION,
                AMOUNT,
                DUE_DATE,
                PAYMENT_TOKEN,
                ZonedDateTime.now().toString(),
                new IdempotencyKey(IDEMPOTENCY_KEY),
                List.of(
                        new PaymentTransferInfo(
                                TRANSFER_PA_FISCAL_CODE,
                                TRANSFER_DIGITAL_STAMP,
                                TRANSFER_AMOUNT,
                                TRANSFER_CATEGORY
                        )
                ),
                false,
                CREDITOR_REFERENCE_ID
        );
    }

    @Nonnull
    public static BaseTransaction reduceEvents(TransactionEvent<?>... events) {
        List<Object> reductionList = new ArrayList<>();
        reductionList.add(new EmptyTransaction());
        reductionList.addAll(Arrays.stream(events).toList());
        return (BaseTransaction) reductionList
                .stream()
                .reduce(
                        (
                         trx,
                         event
                        ) -> ((it.pagopa.ecommerce.commons.domain.v2.Transaction) trx).applyEvent(event)
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Error reducing input events: [%s]".formatted(Arrays.toString(events))
                        )
                );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData pgsTransactionGatewayAuthorizationData(
                                                                                             AuthorizationResultDto authorizationOutcome
    ) {
        return new PgsTransactionGatewayAuthorizationData(
                null,
                authorizationOutcome
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData pgsTransactionGatewayAuthorizationData(
                                                                                             AuthorizationResultDto authorizationOutcome,
                                                                                             String errorCode
    ) {
        return new PgsTransactionGatewayAuthorizationData(
                errorCode,
                authorizationOutcome
        );

    }

    @Nonnull
    public static TransactionGatewayActivationData npgTransactionGatewayActivationData() {
        return new NpgTransactionGatewayActivationData(
                NPG_ORDER_ID,
                NPG_CORRELATION_ID
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData npgTransactionGatewayAuthorizationData(
                                                                                             OperationResultDto outcomeDto
    ) {
        return npgTransactionGatewayAuthorizationData(outcomeDto, null);
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData npgTransactionGatewayAuthorizationData(
                                                                                             OperationResultDto outcomeDto,
                                                                                             String errorCode
    ) {
        return npgTransactionGatewayAuthorizationData(outcomeDto, errorCode, NPG_VALIDATION_SERVICE_ID);
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData npgTransactionGatewayAuthorizationData(
                                                                                             OperationResultDto outcomeDto,
                                                                                             String errorCode,
                                                                                             String validationServiceId
    ) {
        return new NpgTransactionGatewayAuthorizationData(
                outcomeDto,
                NPG_OPERATION_ID,
                NPG_PAYMENT_END_TO_END_ID,
                errorCode,
                validationServiceId
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationRequestedData pgsTransactionGatewayAuthorizationRequestedData() {
        return new PgsTransactionGatewayAuthorizationRequestedData(
                LOGO_URI,
                PgsTransactionGatewayAuthorizationRequestedData.CardBrand.VISA
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationRequestedData npgTransactionGatewayAuthorizationRequestedData() {
        return new NpgTransactionGatewayAuthorizationRequestedData(
                LOGO_URI,
                "VISA",
                NPG_SESSION_ID,
                NPG_CONFIRM_PAYMENT_SESSION_ID,
                null
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationRequestedData npgTransactionGatewayAuthorizationRequestedData(
                                                                                                               WalletInfo walletInfo
    ) {
        return new NpgTransactionGatewayAuthorizationRequestedData(
                LOGO_URI,
                "VISA",
                NPG_SESSION_ID,
                NPG_CONFIRM_PAYMENT_SESSION_ID,
                walletInfo
        );
    }

    @Nonnull
    public static WalletInfo cardsWalletInfo() {
        return new WalletInfo(
                NPG_WALLET_ID,
                new WalletInfo.CardWalletDetails(
                        NPG_WALLET_CARD_BIN,
                        NPG_WALLET_CARD_LAST_FOUR_DIGITS
                )
        );
    }

    @Nonnull
    public static WalletInfo paypalWalletInfo() {
        return new WalletInfo(
                NPG_WALLET_ID,
                new WalletInfo.PaypalWalletDetails(
                        NPG_WALLET_PAYPAL_MASKED_EMAIL
                )
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationRequestedData redirectTransactionGatewayAuthorizationRequestedData() {
        return new RedirectTransactionGatewayAuthorizationRequestedData(
                LOGO_URI,
                REDIRECT_AUTHORIZATION_TIMEOUT
        );
    }

    @Nonnull
    public static TransactionGatewayAuthorizationData redirectTransactionGatewayAuthorizationData(
                                                                                                  RedirectTransactionGatewayAuthorizationData.Outcome outcome,
                                                                                                  String errorCode
    ) {
        return new RedirectTransactionGatewayAuthorizationData(
                outcome,
                errorCode
        );
    }

}
