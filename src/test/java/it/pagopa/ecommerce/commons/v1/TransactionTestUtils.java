package it.pagopa.ecommerce.commons.v1;

import it.pagopa.ecommerce.commons.documents.PaymentNotice;
import it.pagopa.ecommerce.commons.documents.PaymentTransferInformation;
import it.pagopa.ecommerce.commons.documents.v1.Transaction;
import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.*;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;

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
    public static final String PAYMENT_METHOD_NAME = "paymentMethodName";
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

    private static final boolean IS_ALL_CCP_FALSE = false;

    /*
     * Company name field will be populated only for v2 events
     */
    public static final String COMPANY_NAME = null;

    public static final String CREDITOR_REFERENCE_ID = null;

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

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent() {
        return transactionActivateEvent(ZonedDateTime.now().toString());
    }

    @Nonnull
    public static TransactionActivatedEvent transactionActivateEvent(String creationDate) {
        return new TransactionActivatedEvent(
                TRANSACTION_ID,
                creationDate,
                new TransactionActivatedData(
                        EMAIL,
                        List.of(
                                new PaymentNotice(
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
                        FAULT_CODE,
                        FAULT_CODE_STRING,
                        Transaction.ClientId.CHECKOUT,
                        ID_CART,
                        PAYMENT_TOKEN_VALIDITY_TIME_SEC
                )
        );
    }

    @Nonnull
    public static TransactionActivated transactionActivated(String creationDate) {
        return new TransactionActivated(
                new TransactionId(TRANSACTION_ID),
                List.of(
                        new it.pagopa.ecommerce.commons.domain.v1.PaymentNotice(
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
                PAYMENT_TOKEN_VALIDITY_TIME_SEC
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
                        false,
                        AUTHORIZATION_REQUEST_ID,
                        PAYMENT_GATEWAY,
                        LOGO_URI,
                        TransactionAuthorizationRequestData.CardBrand.VISA,
                        PAYMENT_METHOD_DESCRIPTION
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent(
                                                                                                TransactionAuthorizationRequestData.PaymentGateway paymentGateway
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
                        LOGO_URI,
                        TransactionAuthorizationRequestData.CardBrand.VISA,
                        PAYMENT_METHOD_DESCRIPTION
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent() {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationCompletedData(
                        AUTHORIZATION_CODE,
                        RRN,
                        timestampOperation,
                        null,
                        AUTHORIZATION_RESULT_DTO
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent(
                                                                                                AuthorizationResultDto authorizationResultDto
    ) {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationCompletedData(
                        AUTHORIZATION_CODE,
                        RRN,
                        timestampOperation,
                        null,
                        authorizationResultDto
                )
        );
    }

    @Nonnull
    public static TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent(
                                                                                                AuthorizationResultDto authorizationResultDto,
                                                                                                String rnn
    ) {
        return new TransactionAuthorizationCompletedEvent(
                TRANSACTION_ID,
                new TransactionAuthorizationCompletedData(
                        AUTHORIZATION_CODE,
                        rnn,
                        timestampOperation,
                        null,
                        authorizationResultDto
                )
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
    public static TransactionClosureErrorEvent transactionClosureErrorEvent() {
        return new TransactionClosureErrorEvent(
                TRANSACTION_ID
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
                PAYMENT_DATE,
                RECEIVING_OFFICE_NAME,
                PAYMENT_DESCRIPTION
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
                        new PaymentNotice(
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
                RRN
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

    public static it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo paymentRequestInfoV1() {
        return new it.pagopa.ecommerce.commons.repositories.v1.PaymentRequestInfo(
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

    public static it.pagopa.ecommerce.commons.repositories.v2.PaymentRequestInfo paymentRequestInfoV2() {
        return new it.pagopa.ecommerce.commons.repositories.v2.PaymentRequestInfo(
                new it.pagopa.ecommerce.commons.domain.v2.RptId(RPT_ID),
                PA_FISCAL_CODE,
                PA_NAME,
                DESCRIPTION,
                AMOUNT,
                DUE_DATE,
                PAYMENT_TOKEN,
                ZonedDateTime.now().toString(),
                new it.pagopa.ecommerce.commons.domain.v2.IdempotencyKey(IDEMPOTENCY_KEY),
                List.of(
                        new it.pagopa.ecommerce.commons.domain.v2.PaymentTransferInfo(
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
                        ) -> ((it.pagopa.ecommerce.commons.domain.v1.Transaction) trx).applyEvent(event)
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Error reducing input events: [%s]".formatted(Arrays.toString(events))
                        )
                );
    }

}
