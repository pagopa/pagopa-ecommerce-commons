package it.pagopa.ecommerce.commons.utils;

import io.opentelemetry.api.common.Attributes;
import it.pagopa.ecommerce.commons.documents.v2.Transaction;
import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionStatusTracerUtilsTest {

    private final OpenTelemetryUtils openTelemetryUtils = Mockito.mock(OpenTelemetryUtils.class);
    private final UpdateTransactionStatusTracerUtils updateTransactionStatusTracerUtils = new UpdateTransactionStatusTracerUtils(
            openTelemetryUtils
    );

    @Captor
    private ArgumentCaptor<Attributes> attributesCaptor;

    @ParameterizedTest
    @EnumSource(UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.class)
    void shouldTraceTransactionUpdateStatusSuccessfullyForNodoSendPaymentResultDetails(
                                                                                       UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome outcome
    ) {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.SendPaymentResultNodoStatusUpdate(
                outcome,
                "pspId",
                "CP",
                Transaction.ClientId.CHECKOUT,
                false,
                new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult(
                        "OK",
                        Optional.empty()
                )
        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getClientId().get().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPaymentMethodTypeCode().get(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment().get(),
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY)
        );
        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY
                )
        );
        assertEquals(
                "OK",
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );

    }

    @ParameterizedTest
    @EnumSource(UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.class)
    void shouldTraceTransactionUpdateStatusSuccessfullyForNodoClosePaymentDetails(
                                                                                  UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome outcome
    ) {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.ClosePaymentNodoStatusUpdate(
                outcome,
                "pspId",
                "CP",
                Transaction.ClientId.CHECKOUT,
                true,
                new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult("OK", Optional.empty())
        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getClientId().get().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPaymentMethodTypeCode().get(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment().get(),
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY)
        );
        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY
                )
        );
        assertEquals(
                "OK",
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );

    }

    private static Stream<Arguments> tracePaymentGatewayDetailsTestMethodSource() {
        return Stream.of(
                Arguments.of(
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.PGS_XPAY
                ),
                Arguments.of(
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.PGS_VPOS
                ),
                Arguments.of(
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.NPG
                ),
                Arguments.of(UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.UNKNOWN)
        );
    }

    @ParameterizedTest
    @MethodSource("tracePaymentGatewayDetailsTestMethodSource")
    void shouldTraceTransactionUpdateStatusSuccessfullyForPaymentGatewayDetails(
                                                                                UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger trigger
    ) {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdate(
                trigger,
                UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdateContext(
                        "pspId",
                        new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult("OK", Optional.empty()),
                        "CP",
                        Transaction.ClientId.CHECKOUT,
                        true
                )
        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getClientId().get().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPaymentMethodTypeCode().get(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment().get(),
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY)
        );
        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY
                )
        );
        assertEquals(
                statusUpdateInfo.getGatewayOutcomeResult().get().gatewayOperationOutcome(),
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );

    }

    @Test
    void shouldTracePspId() {
        String pspId = "pspId";

        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdate(
                UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.REDIRECT,
                UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdateContext(
                        pspId,
                        new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult("OK", Optional.empty()),
                        "CP",
                        Transaction.ClientId.CHECKOUT,
                        false
                )
        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                pspId,
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

    }

    @Test
    void shouldThrowExceptionBuildingInvalidPaymentGatewayStatusUpdateRecordWithNullAttributes() {
        assertThrows(
                NullPointerException.class,
                () -> new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdate(
                        null,
                        null,
                        null
                )
        );
    }

    @Test
    void shouldThrowExceptionBuildingInvalidPaymentGatewayStatusUpdateRecordWithInvalidPaymentGatewayType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdate(
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.NODO,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                        new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdateContext(
                                "pspId",
                                new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult("OK", Optional.empty()),
                                "CP",
                                Transaction.ClientId.CHECKOUT,
                                false
                        )
                )
        );
        assertEquals("Invalid trigger for PaymentGatewayStatusUpdate: NODO", exception.getMessage());
    }

    @Test
    void shouldTraceAuthorizationRequestedUpdateStatus() {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.AuthorizationRequestedStatusUpdate(
                UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.NPG,
                UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                "pspId",
                "CP",
                Transaction.ClientId.CHECKOUT,
                true,
                new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult(
                        "OK",
                        Optional.empty()
                )

        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getClientId().get().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.getPaymentMethodTypeCode().get(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment().get(),
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY)
        );
        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY
                )
        );
        assertEquals(
                "OK",
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );
    }

    @Test
    void shouldTraceUserCanceledNodeClosePaymentUpdateStatus() {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.UserCancelClosePaymentNodoStatusUpdate(
                UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                Transaction.ClientId.CHECKOUT,
                new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult(
                        "KO",
                        Optional.of("error")
                )
        );
        // pre-conditions
        doNothing().when(openTelemetryUtils).addSpanWithAttributes(
                eq(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_SPAN_NAME),
                attributesCaptor.capture()
        );
        // test
        updateTransactionStatusTracerUtils.traceStatusUpdateOperation(statusUpdateInfo);
        // assertions
        verify(openTelemetryUtils, times(1)).addSpanWithAttributes(any(), any());
        Attributes attributes = attributesCaptor.getValue();
        assertEquals(
                statusUpdateInfo.getOutcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getType().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getTrigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.getClientId().get().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertNull(
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_WALLET_PAYMENT_ATTRIBUTE_KEY)
        );
        assertEquals(
                "error",
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_ERROR_CODE_ATTRIBUTE_KEY
                )
        );
        assertEquals(
                "KO",
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );
    }

    @Test
    void shouldThrowExceptionBuildingAuthorizationRequestedStatusUpdateWithInvalidTrigger() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new UpdateTransactionStatusTracerUtils.AuthorizationRequestedStatusUpdate(
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.NODO,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                        "pspId",
                        "CP",
                        Transaction.ClientId.CHECKOUT,
                        true,
                        new UpdateTransactionStatusTracerUtils.GatewayOutcomeResult(
                                "OK",
                                Optional.empty()
                        )

                )
        );
        assertEquals("Invalid trigger for AuthorizationRequestedStatusUpdate: NODO", exception.getMessage());
    }

    public static Stream<Arguments> paymentGatewayToTriggerMethodSource() {
        return Stream.of(
                Arguments.of(
                        TransactionAuthorizationRequestData.PaymentGateway.NPG,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.NPG
                ),
                Arguments.of(
                        TransactionAuthorizationRequestData.PaymentGateway.VPOS,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.PGS_VPOS
                ),
                Arguments.of(
                        TransactionAuthorizationRequestData.PaymentGateway.XPAY,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.PGS_XPAY
                ),
                Arguments.of(
                        TransactionAuthorizationRequestData.PaymentGateway.REDIRECT,
                        UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.REDIRECT
                )
        );
    }

    @ParameterizedTest
    @MethodSource("paymentGatewayToTriggerMethodSource")
    void shouldConvertPaymentGatewayToTriggerSuccessfully(
                                                          TransactionAuthorizationRequestData.PaymentGateway paymentGateway,
                                                          UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger expectedTrigger
    ) {
        assertEquals(expectedTrigger, UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.from(paymentGateway));
    }

}
