package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.api.JwtIssuerApi;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKResponseDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKSResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class JwtIssuerClientTests {

    @Mock
    private ApiClient apiClient;
    @Mock
    @Qualifier("jwtIssuerWebClient")
    private JwtIssuerApi jwtIssuerApi;

    private JwtIssuerClient jwtIssuerClient;

    @BeforeEach
    public void init() {
        jwtIssuerClient = new JwtIssuerClient(jwtIssuerApi);
    }

    @Test
    void shouldRetrieveFieldsDtoUsingExplicitParameters() {
        JWKResponseDto jwkResponseDto = new JWKResponseDto();
        jwkResponseDto.alg("alg");
        jwkResponseDto.use("use");
        jwkResponseDto.e("e");
        jwkResponseDto.n("n");
        jwkResponseDto.kid("kid");
        jwkResponseDto.kty(JWKResponseDto.KtyEnum.RSA);
        JWKSResponseDto jwksResponseDto = new JWKSResponseDto().addKeysItem(jwkResponseDto);

        Mockito.when(
                jwtIssuerApi.getTokenPublicKeys()
        ).thenReturn(Mono.just(jwksResponseDto));

        StepVerifier
                .create(
                        jwtIssuerClient.getKeys()
                )
                .expectNext(jwksResponseDto)
                .verifyComplete();
    }
    /*
     * @Test void shouldThrowExceptionWhenBuildFormThrows() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * CreateHostedOrderRequestDto requestDto =
     * buildCreateHostedOrderRequestDto(null);
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersBuildPost( correlationUUID,
     * MOCKED_API_KEY, requestDto ) ) .thenReturn( Mono.error( new
     * WebClientResponseException(
     * "Test error when calling pspApiV1OrdersBuildPost",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.buildForm( correlationUUID,
     * URI.create(MERCHANT_URL), URI.create(RESULT_URL),
     * URI.create(NOTIFICATION_URL), URI.create(CANCEL_URL), ORDER_REQUEST_ORDER_ID,
     * ORDER_REQUEST_CUSTOMER_ID, NpgClient.PaymentMethod.CARDS, MOCKED_API_KEY ) )
     * .expectError(NpgResponseException.class) .verify(); }
     *
     * @Test void shouldThrowExceptionWhenBuildFormSubsequentPaymentThrows() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * CreateHostedOrderRequestDto requestDto =
     * buildCreateHostedOrderRequestDto(ORDER_REQUEST_CONTRACT_ID);
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersBuildPost( correlationUUID,
     * MOCKED_API_KEY, requestDto ) ) .thenReturn( Mono.error( new
     * WebClientResponseException(
     * "Test error when calling pspApiV1OrdersBuildPost",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.buildForm( correlationUUID,
     * URI.create(MERCHANT_URL), URI.create(RESULT_URL),
     * URI.create(NOTIFICATION_URL), URI.create(CANCEL_URL), ORDER_REQUEST_ORDER_ID,
     * ORDER_REQUEST_CUSTOMER_ID, NpgClient.PaymentMethod.CARDS, MOCKED_API_KEY,
     * ORDER_REQUEST_CONTRACT_ID ) ) .expectError(NpgResponseException.class)
     * .verify(); }
     *
     * @Test void shouldPropagateErrorCodesWhenBuildFormThrows() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * CreateHostedOrderRequestDto requestDto =
     * buildCreateHostedOrderRequestDto(null);
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersBuildPost( correlationUUID,
     * MOCKED_API_KEY, requestDto ) ) .thenReturn( Mono.error( new
     * WebClientResponseException(
     * "Test error when calling pspApiV1OrdersBuildPost",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.buildForm( correlationUUID,
     * URI.create(MERCHANT_URL), URI.create(RESULT_URL),
     * URI.create(NOTIFICATION_URL), URI.create(CANCEL_URL), ORDER_REQUEST_ORDER_ID,
     * ORDER_REQUEST_CUSTOMER_ID, NpgClient.PaymentMethod.CARDS, MOCKED_API_KEY ) )
     * .expectError(NpgResponseException.class) .verify(); }
     *
     * @Test void shouldPropagateErrorCodesWhenBuildFormSubsequentPaymentThrows()
     * throws JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * CreateHostedOrderRequestDto requestDto =
     * buildCreateHostedOrderRequestDto(ORDER_REQUEST_CONTRACT_ID);
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersBuildPost( correlationUUID,
     * MOCKED_API_KEY, requestDto ) ) .thenReturn( Mono.error( new
     * WebClientResponseException(
     * "Test error when calling pspApiV1OrdersBuildPost",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.buildForm( correlationUUID,
     * URI.create(MERCHANT_URL), URI.create(RESULT_URL),
     * URI.create(NOTIFICATION_URL), URI.create(CANCEL_URL), ORDER_REQUEST_ORDER_ID,
     * ORDER_REQUEST_CUSTOMER_ID, NpgClient.PaymentMethod.CARDS, MOCKED_API_KEY,
     * ORDER_REQUEST_CONTRACT_ID ) ) .expectError(NpgResponseException.class)
     * .verify(); }
     *
     * @Test void shouldRetrieveCardDataWithSuccess() { UUID correlationUUID =
     * UUID.randomUUID(); CardDataResponseDto expectedResponse = new
     * CardDataResponseDto().bin(BIN).circuit(CIRCUIT).expiringDate("0426")
     * .lastFourDigits("1234");
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildCardDataGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ).thenReturn(Mono.just(expectedResponse));
     *
     * StepVerifier .create( npgClient.getCardData( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectNext(expectedResponse) .verifyComplete(); }
     *
     * @Test void shouldThrowExceptionWhileRetrieveCardData() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildCardDataGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ) .thenReturn( Mono.error( new
     * WebClientResponseException( "Error while invoke method for get card data",
     * HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase(), null,
     * objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ), null ) )
     * );
     *
     * StepVerifier .create( npgClient.getCardData( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectError(NpgResponseException.class) .verify(); }
     *
     * @Test void shouldPropagateErrorCodesWhileRetrieveCardData() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildCardDataGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ) .thenReturn( Mono.error( new
     * WebClientResponseException( "Error while invoke method for get card data",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.getCardData( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectErrorMatches( e -> e instanceof
     * NpgResponseException npgResponseException &&
     * npgResponseException.getErrors().equals(List.of("GW0001")) ) .verify(); }
     *
     * @Test void shouldConfirmPaymentGivenValidNpgSession() { StateResponseDto
     * stateResponseDto = buildTestRetrieveStateResponseDto();
     *
     * UUID correlationUUID = UUID.randomUUID(); ConfirmPaymentRequestDto
     * confirmPaymentRequestDto = buildTestConfirmPaymentRequestDto();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildConfirmPaymentPost(
     * correlationUUID, MOCKED_API_KEY, confirmPaymentRequestDto )
     * ).thenReturn(Mono.just(stateResponseDto));
     *
     * StepVerifier .create( npgClient.confirmPayment( correlationUUID, SESSION_ID,
     * new BigDecimal(ORDER_REQUEST_PAY), MOCKED_API_KEY ) )
     * .expectNext(stateResponseDto) .verifyComplete(); }
     *
     * @Test void shouldRefundPaymentGivenValidNpgSession() { RefundResponseDto
     * refundResponseDto = buildTestRefundResponseDto();
     *
     * UUID correlationUUID = UUID.randomUUID(); RefundRequestDto refundRequestDto =
     * buildRefundRequestDto();
     *
     * Mockito.when( paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
     * OPERATION_ID, correlationUUID, MOCKED_API_KEY, IDEMPOTENCE_KEY.toString(),
     * refundRequestDto ) ).thenReturn(Mono.just(refundResponseDto));
     *
     * StepVerifier .create( npgClient.refundPayment( correlationUUID, OPERATION_ID,
     * IDEMPOTENCE_KEY, BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
     * MOCKED_API_KEY, REFUND_DESCRIPTION ) ) .expectNext(refundResponseDto)
     * .verifyComplete(); }
     *
     * @Test void shouldRefundPaymentGivenValidNpgSessionWithNullDescription() {
     * RefundResponseDto refundResponseDto = buildTestRefundResponseDto();
     *
     * UUID correlationUUID = UUID.randomUUID(); RefundRequestDto refundRequestDto =
     * buildRefundRequestDtoNullDescription();
     *
     * Mockito.when( paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
     * OPERATION_ID, correlationUUID, MOCKED_API_KEY, IDEMPOTENCE_KEY.toString(),
     * refundRequestDto ) ).thenReturn(Mono.just(refundResponseDto));
     *
     * StepVerifier .create( npgClient.refundPayment( correlationUUID, OPERATION_ID,
     * IDEMPOTENCE_KEY, BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
     * MOCKED_API_KEY, null ) ) .expectNext(refundResponseDto) .verifyComplete(); }
     *
     * @Test void shouldPropagateErrorCodesWhileConfirmPayment() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * ConfirmPaymentRequestDto confirmPaymentRequestDto =
     * buildTestConfirmPaymentRequestDto();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildConfirmPaymentPost(
     * correlationUUID, MOCKED_API_KEY, confirmPaymentRequestDto ) ) .thenReturn(
     * Mono.error( new WebClientResponseException(
     * "Error while invoke method for confirm payment",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.confirmPayment( correlationUUID, SESSION_ID,
     * new BigDecimal(ORDER_REQUEST_PAY), MOCKED_API_KEY ) ) .expectErrorMatches( e
     * -> e instanceof NpgResponseException npgResponseException &&
     * npgResponseException.getErrors().equals(List.of("GW0001")) &&
     * npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST) )
     * .verify(); }
     *
     * @Test void shouldPropagateErrorCodesWhileRefundPayment() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * RefundRequestDto refundRequestDto = buildRefundRequestDto();
     *
     * Mockito.when( paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
     * OPERATION_ID, correlationUUID, MOCKED_API_KEY, IDEMPOTENCE_KEY.toString(),
     * refundRequestDto ) ) .thenReturn( Mono.error( new WebClientResponseException(
     * "Error while invoke method for confirm payment",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.refundPayment( correlationUUID, OPERATION_ID,
     * IDEMPOTENCE_KEY, BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
     * MOCKED_API_KEY, REFUND_DESCRIPTION ) ) .expectErrorMatches( e -> e instanceof
     * NpgResponseException npgResponseException &&
     * npgResponseException.getErrors().equals(List.of("GW0001")) &&
     * npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST) )
     * .verify(); }
     *
     * @Test void shouldPropagateErrorCodesWhileRefundPaymentWithNullDescription()
     * throws JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     * RefundRequestDto refundRequestDto = buildRefundRequestDtoNullDescription();
     *
     * Mockito.when( paymentServicesApi.pspApiV1OperationsOperationIdRefundsPost(
     * OPERATION_ID, correlationUUID, MOCKED_API_KEY, IDEMPOTENCE_KEY.toString(),
     * refundRequestDto ) ) .thenReturn( Mono.error( new WebClientResponseException(
     * "Error while invoke method for confirm payment",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.refundPayment( correlationUUID, OPERATION_ID,
     * IDEMPOTENCE_KEY, BigDecimal.valueOf(Integer.parseInt(AMOUNT)),
     * MOCKED_API_KEY, null ) ) .expectErrorMatches( e -> e instanceof
     * NpgResponseException npgResponseException &&
     * npgResponseException.getErrors().equals(List.of("GW0001")) &&
     * npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST) )
     * .verify(); }
     *
     * @Test void shouldPerformOrderBuildForApmWithPayActionAndTransactionAmount() {
     * FieldsDto fieldsDto = buildTestFieldsDtoForSubsequentPayment(); Integer
     * transactionTotalAmount = 1000; UUID correlationUUID = UUID.randomUUID();
     * CreateHostedOrderRequestDto requestDto = buildCreateHostedOrderRequestDto(
     * ORDER_REQUEST_CONTRACT_ID, transactionTotalAmount, null );
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersBuildPost( correlationUUID,
     * MOCKED_API_KEY, requestDto ) ).thenReturn(Mono.just(fieldsDto));
     *
     * StepVerifier .create( npgClient.buildFormForPayment( correlationUUID,
     * URI.create(MERCHANT_URL), URI.create(RESULT_URL),
     * URI.create(NOTIFICATION_URL), URI.create(CANCEL_URL), ORDER_REQUEST_ORDER_ID,
     * ORDER_REQUEST_CUSTOMER_ID, NpgClient.PaymentMethod.CARDS, MOCKED_API_KEY,
     * ORDER_REQUEST_CONTRACT_ID, transactionTotalAmount, null ) )
     * .expectNext(fieldsDto) .verifyComplete(); }
     *
     * @Test void shouldGetStateOfPayment() { UUID correlationUUID =
     * UUID.randomUUID();
     *
     * StateResponseDto stateResponseDto = new
     * StateResponseDto().state(WorkflowStateDto.PAYMENT_COMPLETE);
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildStateGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ).thenReturn(Mono.just(stateResponseDto));
     *
     * StepVerifier .create( npgClient.getState( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectNext(stateResponseDto) .verifyComplete();
     *
     * }
     *
     * @Test void shouldGetStateOfOrder() { UUID correlationUUID =
     * UUID.randomUUID();
     *
     * final var response = new OrderResponseDto() .orderStatus( new
     * OrderStatusDto() .authorizedAmount("123") .capturedAmount("123")
     * .lastOperationType(OperationTypeDto.REFUND)
     * .lastOperationTime("2022-09-01T01:20:00.001Z") ) .addOperationsItem( new
     * OperationDto() .orderId("order-id") .operationId("operation-id")
     * .operationAmount("123") .operationAmount("123")
     * .paymentMethod(PaymentMethodDto.CARD) .channel(ChannelTypeDto.ECOMMERCE)
     * .operationType(OperationTypeDto.REFUND)
     * .operationResult(OperationResultDto.EXECUTED) ) .links(List.of());
     *
     * Mockito.when( paymentServicesApi.pspApiV1OrdersOrderIdGet( correlationUUID,
     * ORDER_REQUEST_ORDER_ID, MOCKED_API_KEY ) ).thenReturn(Mono.just(response));
     *
     * StepVerifier .create( npgClient.getOrder( correlationUUID, MOCKED_API_KEY,
     * ORDER_REQUEST_ORDER_ID ) ) .expectNext(response) .verifyComplete();
     *
     * }
     *
     * @Test void shouldPropagateErrorCodesWhileGetState() throws
     * JsonProcessingException { UUID correlationUUID = UUID.randomUUID();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildStateGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ) .thenReturn( Mono.error( new
     * WebClientResponseException( "Error while invoke method for get state",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, objectMapper.writeValueAsBytes( npgClientErrorResponse("GW0001") ),
     * null ) ) );
     *
     * StepVerifier .create( npgClient.getState( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectErrorMatches( e -> e instanceof
     * NpgResponseException npgResponseException &&
     * npgResponseException.getErrors().equals(List.of("GW0001")) &&
     * npgResponseException.getStatusCode().get().equals(HttpStatus.BAD_REQUEST) )
     * .verify(); }
     *
     * @Test void shouldPropagateErrorForUnparsableErrorResponse() { UUID
     * correlationUUID = UUID.randomUUID();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildStateGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ) .thenReturn( Mono.error( new
     * WebClientResponseException( "Error while invoke method for get state",
     * HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(),
     * null, "error".getBytes(StandardCharsets.UTF_8), null ) ) );
     *
     * StepVerifier .create( npgClient.getState( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectErrorMatches( e -> { NpgResponseException
     * npgResponseException = (NpgResponseException) e;
     * assertTrue(npgResponseException.getErrors().isEmpty());
     * assertEquals(HttpStatus.BAD_REQUEST,
     * npgResponseException.getStatusCode().get()); return true;
     *
     * }
     *
     * ) .verify(); }
     *
     * @Test void shouldPropagateErrorForGenericExceptionThrownFromClient() { UUID
     * correlationUUID = UUID.randomUUID();
     *
     * Mockito.when( paymentServicesApi.pspApiV1BuildStateGet( correlationUUID,
     * SESSION_ID, MOCKED_API_KEY ) ) .thenReturn( Mono.error( new
     * NullPointerException("Error while invoke method for get state") ) );
     *
     * StepVerifier .create( npgClient.getState( correlationUUID, SESSION_ID,
     * MOCKED_API_KEY ) ) .expectErrorMatches( e -> { NpgResponseException
     * npgResponseException = (NpgResponseException) e;
     * assertTrue(npgResponseException.getErrors().isEmpty());
     * assertTrue(npgResponseException.getStatusCode().isEmpty()); return true; }
     *
     * ) .verify(); }
     *
     * private static ClientErrorDto npgClientErrorResponse(String gatewayError) {
     * return new ClientErrorDto() .errors( List.of( new ErrorsInnerDto()
     * .code(gatewayError) .description("Error %s".formatted(gatewayError)) ) ); }
     *
     * private SessionIdRequestDto buildSessionIdRequestDto() { return new
     * SessionIdRequestDto() .sessionId(SESSION_ID); }
     *
     * private RefundRequestDto buildRefundRequestDto() { return new
     * RefundRequestDto() .amount(AMOUNT) .currency(CURRENCY)
     * .description(REFUND_DESCRIPTION); }
     *
     * private RefundRequestDto buildRefundRequestDtoNullDescription() { return new
     * RefundRequestDto() .amount(AMOUNT) .currency(CURRENCY); }
     *
     * private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto(String
     * contractId) { return buildCreateHostedOrderRequestDto(contractId, null,
     * null); }
     *
     * private CreateHostedOrderRequestDto buildCreateHostedOrderRequestDto( String
     * contractId, Integer amount, String language ) { if (language == null) {
     * language = ORDER_REQUEST_LANGUAGE_ITA; }
     *
     * return new CreateHostedOrderRequestDto() .version(ORDER_REQUEST_VERSION)
     * .merchantUrl(MERCHANT_URL) .order( new OrderDto()
     * .orderId(ORDER_REQUEST_ORDER_ID)
     * .amount(Optional.ofNullable(amount).map(Objects::toString).orElse(
     * ORDER_REQUEST_PAY)) .currency(ORDER_REQUEST_CURRENCY_EUR)
     * .customerId(ORDER_REQUEST_CUSTOMER_ID) ) .paymentSession( new
     * PaymentSessionDto() .paymentService(ORDER_REQUEST_PAYMENT_SERVICE_CARDS)
     * .amount(Optional.ofNullable(amount).map(Objects::toString).orElse(
     * ORDER_REQUEST_PAY)) .actionType(ActionTypeDto.PAY) .language(language)
     * .cancelUrl(CANCEL_URL) .notificationUrl(NOTIFICATION_URL)
     * .resultUrl(RESULT_URL) .recurrence( contractId != null ? new
     * RecurringSettingsDto() .action(RecurringActionDto.SUBSEQUENT_PAYMENT)
     * .contractType(RecurringContractTypeDto.CIT)
     * .contractId(ORDER_REQUEST_CONTRACT_ID) : null ) ); }
     *
     * private FieldsDto buildTestFieldsDto() { return new FieldsDto()
     * .sessionId(SESSION_ID) .securityToken(SECURITY_TOKEN) .fields( List.of( new
     * FieldDto().id(TEST_1).src(SRC_1).propertyClass(PROPERTY_1).type(TYPE_1) ) );
     * }
     *
     * private FieldsDto buildTestFieldsDtoForSubsequentPayment() { return new
     * FieldsDto() .sessionId(SESSION_ID)
     * .state(WorkflowStateDto.READY_FOR_PAYMENT); }
     *
     * private FieldsDto buildTestFieldsDtoForSubsequentPaymentApm() { return new
     * FieldsDto() .sessionId(SESSION_ID)
     * .state(WorkflowStateDto.REDIRECTED_TO_EXTERNAL_DOMAIN)
     * .url("http://external-domain/redirectionUrl"); }
     *
     * private StateResponseDto buildTestRetrieveStateResponseDto() { return new
     * StateResponseDto() .url("https://iframe-gdi")
     * .state(WorkflowStateDto.REDIRECTED_TO_EXTERNAL_DOMAIN); }
     *
     * private RefundResponseDto buildTestRefundResponseDto() { return new
     * RefundResponseDto().operationId("operation-id").operationTime(
     * "2022-09-01T01:20:00.001Z"); }
     *
     * private ConfirmPaymentRequestDto buildTestConfirmPaymentRequestDto() { return
     * new ConfirmPaymentRequestDto() .sessionId(SESSION_ID)
     * .amount(ORDER_REQUEST_PAY); }
     */
}
