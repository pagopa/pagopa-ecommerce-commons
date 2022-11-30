package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.TransactionUtils;
import it.pagopa.ecommerce.commons.generated.events.v1.*;
import it.pagopa.ecommerce.commons.generated.nodo.v2.dto.ClosePaymentResponseDto;
import it.pagopa.ecommerce.commons.generated.transactions.model.TransactionStatusDto;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionTest {

    @Test
    void shouldConstructTransaction() {
        PaymentToken paymentToken = new PaymentToken(TransactionUtils.PAYMENT_TOKEN);
        RptId rptId = new RptId(TransactionUtils.RPT_ID);
        TransactionDescription description = new TransactionDescription(TransactionUtils.DESCRIPTION);
        TransactionAmount amount = new TransactionAmount(TransactionUtils.AMOUNT);
        TransactionStatusDto status = TransactionStatusDto.ACTIVATED;

        TransactionActivated transaction = TransactionUtils.transactionActivated(ZonedDateTime.now().toString());

        assertEquals(new PaymentToken(transaction.getTransactionActivatedData().getPaymentToken()), paymentToken);
        assertEquals(transaction.getRptId(), rptId);
        assertEquals(transaction.getDescription(), description);
        assertEquals(transaction.getAmount(), amount);
        assertEquals(transaction.getStatus(), status);
    }

    @Test
    void shouldIgnoreInvalidEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
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

        TransactionActivatedEvent event = TransactionUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event);

        TransactionActivated expected = TransactionUtils.transactionActivated(event.getCreationDate().toString());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent event = TransactionUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event, event);

        TransactionActivated expected = TransactionUtils.transactionActivated(event.getCreationDate().toString());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(transactionActivatedEvent, authorizationRequestedEvent);

        TransactionActivated transactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization expected = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent
        );

        TransactionActivated transactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization expected = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthCompletedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated transactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization expected = TransactionUtils.transactionWithCompletedAuthorization(
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

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated transactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization expected = TransactionUtils.transactionWithCompletedAuthorization(
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

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureSentEvent closureSentEvent = TransactionUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed expected = TransactionUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureSentEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureSentEvent closureSentEvent = TransactionUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent,
                closureSentEvent
        );

        TransactionActivated transactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed expected = TransactionUtils.transactionClosed(
                closureSentEvent,
                transactionWithCompletedAuthorization
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldUpgradeTransactionActivationRequestedToTransactionActivated() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivationRequestedEvent activationRequestedEvent = TransactionUtils
                .transactionActivationRequestedEvent();
        TransactionActivatedEvent activatedEvent = TransactionUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(activationRequestedEvent, activatedEvent);

        TransactionActivated expected = TransactionUtils
                .transactionActivated(activationRequestedEvent.getCreationDate().toString());

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void transactionClosedStatusChangeWorks() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureSentEvent closureSentEvent = TransactionUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                closureSentEvent,
                closureSentEvent
        );

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> t instanceof TransactionClosed && ((TransactionClosed) t)
                                .withStatus(TransactionStatusDto.AUTHORIZED)
                                .getStatus() == TransactionStatusDto.AUTHORIZED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(e -> e.equals(expected)).verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionUtils.transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent,
                authorizationStatusUpdatedEvent
        );

        TransactionActivated TransactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamWithRecovery() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationStatusUpdatedEvent authorizationStatusUpdatedEvent = TransactionUtils
                .transactionAuthorizationStatusUpdatedEvent(TransactionAuthorizationStatusUpdateData.AuthorizationResult.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionUtils.transactionClosureErrorEvent();
        TransactionClosureSentEvent closureSentEvent = TransactionUtils
                .transactionClosureSentEvent(ClosePaymentResponseDto.OutcomeEnum.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationStatusUpdatedEvent,
                transactionClosureErrorEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate().toString());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionWithCompletedAuthorization transactionWithCompletedAuthorization = TransactionUtils
                .transactionWithCompletedAuthorization(
                        authorizationStatusUpdatedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCompletedAuthorization);

        TransactionClosed expected = TransactionUtils.transactionClosed(closureSentEvent, transactionWithClosureError);

        Mono<Transaction> actual = events.reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }
}
