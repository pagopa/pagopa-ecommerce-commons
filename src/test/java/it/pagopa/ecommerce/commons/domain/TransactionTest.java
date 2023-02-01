package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.TransactionTestUtils;
import it.pagopa.ecommerce.commons.documents.*;
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
    void shouldConstructTransactionFromAuthorizedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorized expected = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);

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
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                transactionAuthorizedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorized expected = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizationFailedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationFailedEvent transactionAuthorizationFailedEvent = TransactionTestUtils
                .transactionAuthorizationFailedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationFailedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithFailedAuthorization expected = TransactionTestUtils
                .transactionWithFailedAuthorization(
                        transactionAuthorizationFailedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizationFailedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationFailedEvent transactionAuthorizationFailedEvent = TransactionTestUtils
                .transactionAuthorizationFailedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationFailedEvent,
                authorizationRequestedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithFailedAuthorization expected = TransactionTestUtils
                .transactionWithFailedAuthorization(
                        transactionAuthorizationFailedEvent,
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
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);

        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionAuthorized
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
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                closureSentEvent,
                closureSentEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);
        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionAuthorized
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureSentEventStreamWithFailedAuthorization() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationFailedEvent authorizationFailedEvent = TransactionTestUtils
                .transactionAuthorizationFailedEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationFailedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithFailedAuthorization transactionWithFailedAuthorization = TransactionTestUtils
                .transactionWithFailedAuthorization(authorizationFailedEvent, transactionWithRequestedAuthorization);

        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithFailedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureSentEventStreamWithFailedAuthorizationIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationFailedEvent authorizationFailedEvent = TransactionTestUtils
                .transactionAuthorizationFailedEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationFailedEvent,
                closureSentEvent,
                authorizationFailedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithFailedAuthorization transactionWithFailedAuthorization = TransactionTestUtils
                .transactionWithFailedAuthorization(authorizationFailedEvent, transactionWithRequestedAuthorization);

        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                closureSentEvent,
                transactionWithFailedAuthorization
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
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                transactionClosureErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorized);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(e -> e.equals(expected)).verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorized);

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
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizedEvent,
                transactionClosureErrorEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(transactionAuthorizedEvent, transactionWithRequestedAuthorization);
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorized);

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
    void transactionAuthorizedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(
                        transactionAuthorizedEvent,
                        transactionWithRequestedAuthorization
                );

        assertEquals(TransactionStatusDto.AUTHORIZED, transactionAuthorized.getStatus());
    }

    @Test
    void transactionWithFailedAuthorizationHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationFailedEvent transactionAuthorizedEvent = TransactionTestUtils
                .transactionAuthorizationFailedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithFailedAuthorization transactionWithFailedAuthorization = TransactionTestUtils
                .transactionWithFailedAuthorization(
                        transactionAuthorizedEvent,
                        transactionWithRequestedAuthorization
                );

        assertEquals(TransactionStatusDto.AUTHORIZATION_FAILED, transactionWithFailedAuthorization.getStatus());
    }

    @Test
    void transactionWithClosureErrorHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(
                        transactionAuthorizedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorized);

        assertEquals(TransactionStatusDto.CLOSURE_ERROR, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionClosedOKHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionAuthorized(
                        transactionAuthorizedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionWithClosureError = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionWithCompletedAuthorization);

        assertEquals(TransactionStatusDto.CLOSED, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionClosedKOHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizedEvent transactionAuthorizedEvent = TransactionTestUtils.transactionAuthorizedEvent();
        TransactionClosureSentEvent transactionClosureSentEvent = TransactionTestUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.KO);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorized transactionAuthorized = TransactionTestUtils
                .transactionAuthorized(
                        transactionAuthorizedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionWithClosureError = TransactionTestUtils
                .transactionClosed(transactionClosureSentEvent, transactionAuthorized);

        assertEquals(TransactionStatusDto.CLOSURE_FAILED, transactionWithClosureError.getStatus());
    }
}
