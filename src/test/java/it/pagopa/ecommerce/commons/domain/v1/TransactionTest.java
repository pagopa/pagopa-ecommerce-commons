package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransactionWithCompletedAuthorization;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed expected = TransactionTestUtils.transactionClosed(

                transactionAuthorizationCompleted,
                closureSentEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionAuthorizationCompleted,
                        closureSentEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closureSentEvent,
                closureSentEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                transactionAuthorizationCompleted,
                closureSentEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionAuthorizationCompleted(authorizedEvent, transactionWithRequestedAuthorization);
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithCompletedAuthorization,
                        closureSentEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(e -> e.equals(expected)).verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

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
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        TransactionClosed expected = TransactionTestUtils
                .transactionClosed(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        closureSentEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromActivatedTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());

        TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((TransactionExpiredNotAuthorized) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED_NOT_AUTHORIZED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromAuthorizationRequestedTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected) && ((TransactionExpired) v).getTransactionAtPreviousState()
                                .equals(transactionWithRequestedAuthorization)
                                && ((TransactionExpired) v).getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromAuthorizedTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected) && ((TransactionExpired) v).getTransactionAtPreviousState()
                                .equals(transactionAuthorizationCompleted)
                                && ((TransactionExpired) v).getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosedTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closureSentEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        closureSentEvent
                );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((TransactionExpired) v).getTransactionAtPreviousState()
                                        .equals(transactionClosed)
                                && ((TransactionExpired) v).getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosureFailedTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);
        TransactionClosedEvent closedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closedEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(transactionAuthorizationCompleted, closedEvent);

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((TransactionExpired) v).getTransactionAtPreviousState()
                                        .equals(transactionClosed)
                                && ((TransactionExpired) v).getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosureErrorTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                expiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithClosureError.getTransactionAtPreviousState(), expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected) && ((TransactionExpired) v).getTransactionAtPreviousState()
                                .equals(transactionWithClosureError.getTransactionAtPreviousState())
                                && ((TransactionExpired) v).getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
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
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        assertEquals(TransactionStatusDto.AUTHORIZATION_COMPLETED, transactionAuthorizationCompleted.getStatus());
        assertEquals(
                TransactionTestUtils.AUTHORIZATION_CODE,
                transactionAuthorizationCompleted.getTransactionAuthorizationCompletedData().getAuthorizationCode()
        );
        assertEquals(
                TransactionTestUtils.AUTHORIZATION_RESULT_DTO,
                transactionAuthorizationCompleted.getTransactionAuthorizationCompletedData().getAuthorizationResultDto()
        );
    }

    @Test
    void transactionWithClosureErrorHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        assertEquals(TransactionStatusDto.CLOSURE_ERROR, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionClosedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithCompletedAuthorization,
                        TransactionTestUtils.transactionClosedEvent(TransactionClosureData.Outcome.OK)
                );

        assertEquals(TransactionStatusDto.CLOSED, transactionClosed.getStatus());
    }

    @Test
    void transactionNotifiedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        TransactionTestUtils.transactionClosedEvent(TransactionClosureData.Outcome.OK)
                );

        TransactionWithUserReceiptOk transactionWithUserReceiptOk = TransactionTestUtils
                .transactionWithUserReceiptOk(
                        transactionClosed,
                        TransactionTestUtils.transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.OK)
                );

        assertEquals(TransactionStatusDto.NOTIFIED_OK, transactionWithUserReceiptOk.getStatus());
    }

    @Test
    void shouldConstructTransactionRefundFromClosureErrorEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSURE_ERROR);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils.transactionWithClosureError(
                transactionClosureErrorEvent,
                transactionAuthorizationCompleted
        );

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSURE_ERROR);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent,
                transactionRefundedEvent,
                transactionClosureErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils.transactionWithClosureError(
                transactionClosureErrorEvent,
                transactionAuthorizationCompleted
        );
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.CLOSED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        transactionClosedEvent
                );

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        transactionClosedEvent
                );

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromExpiredEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.EXPIRED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromExpiredEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundRequestedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.EXPIRED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionRefundedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionExpiredEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        transactionClosedEvent
                );
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosedEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionAuthorizationCompleted,
                        transactionClosedEvent
                );
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosureErrorEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        transactionWithClosureError.getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosureErrorEventStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        transactionWithClosureError.getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationCompletedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationCompletedStreamIgnoringInvalidEventsIgnoringInvalidEvents() {

        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                authorizationRequestedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredNotAuthorizedFromActivatedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionExpiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED_NOT_AUTHORIZED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredNotAuthorizedFromActivatedStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.ACTIVATED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.EXPIRED_NOT_AUTHORIZED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromActivatedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested expected = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.CANCELLATION_REQUESTED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromActivatedStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureFailedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.UNAUTHORIZED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedOK() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionAuthorizationCompleted,
                        closureSentEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.NOTIFIED_OK.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedKO() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.KO);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionAuthorizationCompleted,
                        closureSentEvent
                );
        TransactionWithUserReceiptKo expected = TransactionTestUtils.transactionWithUserReceiptKo(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.NOTIFIED_KO.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithClosureEvent() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithClosureEventIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithExpirationEvent() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CANCELLATION_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundEvent() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.EXPIRED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundEventIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.EXPIRED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionExpiredEvent,
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionRefundedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(TransactionStatusDto.EXPIRED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundErrorEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundError expected = TransactionTestUtils.transactionWithRefundError(
                (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                transactionRefundErrorEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUND_ERROR.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredIgnoringEventsForTransactionNotAuthorized() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(TransactionStatusDto.EXPIRED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionExpiredEvent,
                transactionRefundErrorEvent
        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundErrorFromRefundRequestedEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSURE_ERROR);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(TransactionStatusDto.REFUND_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils.transactionWithClosureError(
                transactionClosureErrorEvent,
                transactionAuthorizationCompleted
        );

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(transactionWithRefundRequested, transactionRefundErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUND_ERROR.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromClosureErrorEventStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils.transactionWithClosureError(
                transactionClosureErrorEvent,
                transactionAuthorizationCompleted
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundedFromRefundError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(TransactionStatusDto.EXPIRED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundErrorEvent,
                transactionRefundedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundError transactionWithRefundError = TransactionTestUtils.transactionWithRefundError(
                (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                transactionRefundErrorEvent
        );
        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundError, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundedFromRefundErrorIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.AUTHORIZATION_COMPLETED);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(TransactionStatusDto.EXPIRED);
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(TransactionStatusDto.REFUND_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundErrorEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent,
                transactionRefundedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionWithRefundError transactionWithRefundError = TransactionTestUtils.transactionWithRefundError(
                (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                transactionRefundErrorEvent
        );
        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundError, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUNDED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosedFromWithClosureError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionClosedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionClosed expected = TransactionTestUtils.transactionClosed(
                (BaseTransactionWithCompletedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CLOSED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromWithClosureError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithClosureError.getTransactionAtPreviousState(),
                transactionExpiredEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromWithClosureError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.CLOSURE_ERROR);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionWithRefundRequested expected = TransactionTestUtils.transactionWithRefundRequested(
                (BaseTransactionWithCompletedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionRefundRequestedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromWithClosureError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionClosureFailedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionUnauthorized expected = TransactionTestUtils.transactionUnauthorized(
                (BaseTransactionWithCompletedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosureFailedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.UNAUTHORIZED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromWithClosureErrorIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.KO);
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionClosureFailedEvent,
                transactionAuthorizationRequestedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);
        TransactionUnauthorized expected = TransactionTestUtils.transactionUnauthorized(
                (BaseTransactionWithCompletedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosureFailedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.UNAUTHORIZED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromWithClosureErrorForTransactionWithoutAuthorization() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSURE_ERROR);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithClosureError.getTransactionAtPreviousState(),
                transactionExpiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorization() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionClosedEvent transactionClosedevent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent,
                transactionClosedevent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        TransactionUserCanceled expected = TransactionTestUtils.transactionUserCanceled(
                transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedevent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorizationIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        TransactionClosedEvent transactionClosedevent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent,
                transactionUserCanceledEvent,
                transactionClosedevent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        TransactionUserCanceled expected = TransactionTestUtils.transactionUserCanceled(
                transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedevent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromTransactionWithCancellationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionUserCanceled expected = TransactionTestUtils.transactionUserCanceled(
                transactionWithCancellationRequested,
                transactionClosedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CANCELED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosureErrorFromTransactionWithCancellationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionWithClosureError expected = TransactionTestUtils.transactionWithClosureError(
                transactionClosureErrorEvent,
                transactionWithCancellationRequested
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.CLOSURE_ERROR.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithCancellationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CANCELLATION_REQUESTED);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent

        );

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithCancellationRequested,
                transactionExpiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.EXPIRED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.UNAUTHORIZED);
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionUserCanceledEvent,
                transactionClosureFailedEvent,
                transactionExpiredEvent,
                transactionUserCanceledEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.UNAUTHORIZED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRefundRequestedFromNotificationKoStatus() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.KO);

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.NOTIFIED_KO);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                transactionRefundRequestedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionAuthorizationCompleted,
                        closureSentEvent
                );
        TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils.transactionWithUserReceiptKo(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRefundRequestedFromNotificationKoStatusIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(TransactionUserReceiptData.Outcome.KO);

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(TransactionStatusDto.NOTIFIED_KO);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                transactionRefundRequestedEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionAuthorizationCompleted,
                        closureSentEvent
                );
        TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils.transactionWithUserReceiptKo(

                transactionClosed,
                transactionUserReceiptAddedEvent
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && TransactionStatusDto.REFUND_REQUESTED.equals(((BaseTransaction) t).getStatus())
                )
                .verifyComplete();
    }
}
