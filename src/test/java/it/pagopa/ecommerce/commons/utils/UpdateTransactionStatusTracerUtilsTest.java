package it.pagopa.ecommerce.commons.utils;

import io.opentelemetry.api.common.Attributes;
import it.pagopa.ecommerce.commons.documents.v2.Transaction;
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
                Optional.of("pspId"),
                "CP",
                Transaction.ClientId.CHECKOUT,
                false,
                Optional.empty()
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
                statusUpdateInfo.outcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.type().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.trigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.clientId().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.pspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.paymentMethodTypeCode(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment(),
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
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
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
                Optional.of("pspId"),
                Optional.of("CP"),
                Transaction.ClientId.CHECKOUT,
                Optional.of(true),
                Optional.empty()
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
                statusUpdateInfo.outcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.type().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.trigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.clientId().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.pspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.paymentMethodTypeCode(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment(),
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
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );

    }

    @ParameterizedTest
    @EnumSource(UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.class)
    void shouldTraceTransactionUpdateStatusSuccessfullyForNodoClosePaymentDetailsWithEmptyOptionalInformations(
                                                                                                               UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome outcome
    ) {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.ClosePaymentNodoStatusUpdate(
                outcome,
                Optional.empty(),
                Optional.empty(),
                Transaction.ClientId.CHECKOUT,
                Optional.empty(),
                Optional.empty()
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
                statusUpdateInfo.outcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.type().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.trigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.clientId().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.paymentMethodTypeCode(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertNull(
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
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
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
                        Optional.of("pspId"),
                        Optional.empty(),
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
                statusUpdateInfo.outcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.type().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.trigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.clientId().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_CLIENT_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.pspId().get(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PSP_ID_ATTRIBUTE_KEY)
        );

        assertEquals(
                statusUpdateInfo.paymentMethodTypeCode(),
                attributes.get(
                        UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_PAYMENT_METHOD_TYPE_CODE_ATTRIBUTE_KEY
                )
        );

        assertEquals(
                statusUpdateInfo.isWalletPayment(),
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
                UpdateTransactionStatusTracerUtils.FIELD_NOT_AVAILABLE,
                attributes
                        .get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_GATEWAY_OUTCOME_ATTRIBUTE_KEY)
        );

    }

    private static Stream<Arguments> tracePspIdMethodSource() {
        return Stream.of(
                Arguments.of("pspId", "pspId"),
                Arguments.of(null, "N/A")
        );
    }

    @ParameterizedTest
    @MethodSource("tracePspIdMethodSource")
    void shouldTracePspId(
                          String pspId,
                          String expectedSpanPspAttribute
    ) {
        UpdateTransactionStatusTracerUtils.StatusUpdateInfo statusUpdateInfo = new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdate(
                UpdateTransactionStatusTracerUtils.UpdateTransactionTrigger.REDIRECT,
                UpdateTransactionStatusTracerUtils.UpdateTransactionStatusOutcome.OK,
                new UpdateTransactionStatusTracerUtils.PaymentGatewayStatusUpdateContext(
                        Optional.ofNullable(pspId),
                        Optional.empty(),
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
                statusUpdateInfo.outcome().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_OUTCOME_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.type().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TYPE_ATTRIBUTE_KEY)
        );
        assertEquals(
                statusUpdateInfo.trigger().toString(),
                attributes.get(UpdateTransactionStatusTracerUtils.UPDATE_TRANSACTION_STATUS_TRIGGER_ATTRIBUTE_KEY)
        );

        assertEquals(
                expectedSpanPspAttribute,
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
                                Optional.empty(),
                                Optional.empty(),
                                "CP",
                                Transaction.ClientId.CHECKOUT,
                                false
                        )
                )
        );
        assertEquals("Invalid trigger for PaymentGatewayStatusUpdate: NODO", exception.getMessage());
    }

}
