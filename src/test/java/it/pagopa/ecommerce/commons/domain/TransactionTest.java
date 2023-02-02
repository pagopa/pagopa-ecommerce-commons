package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.TransactionTestUtils;
import it.pagopa.ecommerce.commons.documents.*;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.generated.ecommerce.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {

    @Test
    void shouldConstructTransaction() {
        PaymentToken paymentToken = new PaymentToken(TransactionTestUtils.PAYMENT_TOKEN);
        RptId rptId = new RptId(TransactionTestUtils.RPT_ID);
        TransactionDescription description = new TransactionDescription(TransactionTestUtils.DESCRIPTION);
        TransactionAmount amount = new TransactionAmount(TransactionTestUtils.AMOUNT);
        TransactionStatusDto status = TransactionStatusDto.ACTIVATED;

        TransactionActivated transaction = TransactionTestUtils.transactionActivated(ZonedDateTime.now().toString());

        assertEquals(
                new PaymentToken(
                        transaction.getTransactionActivatedData().getPaymentNotices().get(0).getPaymentToken()
                ),
                paymentToken
        );
        assertEquals(transaction.getPaymentNotices().get(0).rptId(), rptId);
        assertEquals(transaction.getPaymentNotices().get(0).transactionDescription(), description);
        assertEquals(transaction.getPaymentNotices().get(0).transactionAmount(), amount);
        assertEquals(transaction.getStatus(), status);
    }

    @Test
    void shouldIgnoreInvalidEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(authorizationRequestedEvent);

        EmptyTransaction expected = new EmptyTransaction();

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent event = TransactionTestUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event);

        TransactionActivated expected = TransactionTestUtils.transactionActivated(event.getCreationDate());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent event = TransactionTestUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event, event);

        TransactionActivated expected = TransactionTestUtils.transactionActivated(event.getCreationDate());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(transactionActivatedEvent, authorizationRequestedEvent);

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthCompletedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization expected = TransactionTestUtils.transactionWithCompletedAuthorization(
                authorizationStatusUpdatedEvent,
                transactionWithRequestedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthCompletedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization expected = TransactionTestUtils.transactionWithCompletedAuthorization(
                authorizationStatusUpdatedEvent,
                transactionWithRequestedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureSentEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptAddedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionStatusDto.NOTIFIED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );
        TransactionWithUserReceipt expected = TransactionTestUtils.transactionWithUserReceipt(
                transactionUserReceiptAddedEvent,
                transactionClosed
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureSentEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent,
                closureSentEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptAddedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionStatusDto.NOTIFIED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );
        TransactionWithUserReceipt expected = TransactionTestUtils.transactionWithUserReceipt(
                transactionUserReceiptAddedEvent,
                transactionClosed
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldUpgradeTransactionActivationRequestedToTransactionActivated() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivationRequestedEvent activationRequestedEvent = TransactionTestUtils
                .transactionActivationRequestedEvent();
        TransactionActivatedEvent activatedEvent = TransactionTestUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(activationRequestedEvent, activatedEvent);

        TransactionActivated expected = TransactionTestUtils
                .transactionActivated(activationRequestedEvent.getCreationDate());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldTransactionActivationRequestedIgnoreNonActivationEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivationRequestedEvent activationRequestedEvent = TransactionTestUtils
                .transactionActivationRequestedEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(activationRequestedEvent, authorizationRequestedEvent);

        TransactionActivationRequested expected = TransactionTestUtils
                .transactionActivationRequested(activationRequestedEvent.getCreationDate());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(e -> e.equals(expected)).verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamWithRecovery() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        TransactionClosed expected = TransactionTestUtils
                .transactionClosed(closureSentEvent, transactionWithClosureError);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void transactionActivationRequestedHasCorrectStatus() {
        TransactionActivationRequested tx = TransactionTestUtils
                .transactionActivationRequested(ZonedDateTime.now().toString());
        assertEquals(TransactionStatusDto.ACTIVATION_REQUESTED, tx.getStatus());
    }

    @Test
    void transactionWithRequestedAuthorizationHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);

        assertEquals(TransactionStatusDto.AUTHORIZATION_REQUESTED, transactionWithRequestedAuthorization.getStatus());
    }

    @Test
    void transactionWithCompletedAuthorizationOKHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        assertEquals(TransactionStatusDto.AUTHORIZED, transactionWithCompletedAuthorization.getStatus());
    }

    @Test
    void transactionWithCompletedAuthorizationKOHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.KO
                );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        assertEquals(TransactionStatusDto.AUTHORIZATION_FAILED, transactionWithCompletedAuthorization.getStatus());
    }

    @Test
    void transactionWithClosureErrorHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        assertEquals(TransactionStatusDto.CLOSURE_ERROR, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionClosedOKHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionWithCompletedAuthorization);

        assertEquals(TransactionStatusDto.CLOSED, transactionClosed.getStatus());
    }

    @Test
    void transactionClosedKOHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.KO);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionWithClosureError = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionWithCompletedAuthorization);

        assertEquals(TransactionStatusDto.CLOSURE_FAILED, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionNotifiedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionStatusDto.NOTIFIED);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionWithCompletedAuthorization);

        TransactionWithUserReceipt transactionWithUserReceipt = TransactionTestUtils
                .transactionWithUserReceipt(transactionUserReceiptAddedEvent, transactionClosed);

        assertEquals(TransactionStatusDto.NOTIFIED, transactionWithUserReceipt.getStatus());
    }

    @Test
    void transactionNotificationFailedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionTestUtils
                .transactionAuthorizationStatusUpdatedEvent(
                        AuthorizationResultDto.OK
                );
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.KO);
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionStatusDto.NOTIFIED_FAILED);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionWithClosureError = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionWithCompletedAuthorization);

        TransactionWithUserReceipt transactionWithUserReceipt = TransactionTestUtils
                .transactionWithUserReceipt(transactionUserReceiptAddedEvent, transactionWithClosureError);

        assertEquals(TransactionStatusDto.NOTIFIED_FAILED, transactionWithUserReceipt.getStatus());
    }
}
