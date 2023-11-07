package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.documents.v1.*;
import it.pagopa.ecommerce.commons.domain.PaymentToken;
import it.pagopa.ecommerce.commons.domain.RptId;
import it.pagopa.ecommerce.commons.domain.TransactionAmount;
import it.pagopa.ecommerce.commons.domain.TransactionDescription;
import it.pagopa.ecommerce.commons.domain.v1.pojos.*;
import it.pagopa.ecommerce.commons.generated.server.model.AuthorizationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.ZonedDateTime;
import java.util.UUID;

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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(
                transactionWithRequestedUserReceipt,
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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                closureSentEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(
                transactionWithRequestedUserReceipt,
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
                .transactionClosureErrorEvent();

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
                .transactionClosureErrorEvent();

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
                .transactionClosureErrorEvent();
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                expiredEvent
        );

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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                expiredEvent
        );

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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                expiredEvent
        );

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

        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closureSentEvent,
                expiredEvent
        );

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

        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionClosedEvent closedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(transactionAuthorizationCompleted, closedEvent);
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                closedEvent,
                expiredEvent
        );

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
                .transactionClosureErrorEvent();
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
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                expiredEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        expiredEvent
                );

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
                .transactionClosureErrorEvent();

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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        TransactionTestUtils.transactionUserReceiptRequestedEvent(
                                TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                        )
                );
        TransactionWithUserReceiptOk transactionWithUserReceiptOk = TransactionTestUtils
                .transactionWithUserReceiptOk(
                        transactionWithRequestedUserReceipt,
                        TransactionTestUtils.transactionUserReceiptAddedEvent(
                                transactionWithRequestedUserReceipt.getTransactionUserReceiptData()
                        )
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
                .transactionClosureErrorEvent();
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
                .transactionClosureErrorEvent();
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

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

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionClosed);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionClosed);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

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

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                        && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                .equals(transactionAuthorizationCompleted)
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
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);

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

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                        && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                .equals(transactionAuthorizationCompleted)
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
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosedEvent,
                transactionExpiredEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);

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

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
                .transactionClosureErrorEvent();
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
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
                .transactionClosureErrorEvent();
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
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);

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

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStreamIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                authorizationRequestedEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredNotAuthorizedFromActivatedStream() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionExpiredEvent
        );

        TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED_NOT_AUTHORIZED)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent
        );

        TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED_NOT_AUTHORIZED)
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
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_REQUESTED)
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
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
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
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
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
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptOk expected = TransactionTestUtils.transactionWithUserReceiptOk(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_OK)
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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptKo expected = TransactionTestUtils.transactionWithUserReceiptKo(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_KO)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedKOIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                authorizedEvent,
                closureSentEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptKo expected = TransactionTestUtils.transactionWithUserReceiptKo(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_KO)
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithExpirationEvent() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithCancellationRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent
        );

        TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

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

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithRequestedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent
        );

        TransactionWithRefundError expected = TransactionTestUtils.transactionWithRefundError(
                transactionWithRefundRequested,
                transactionRefundErrorEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundErrorFromTransactionWithAuthorizationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithRequestedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent
        );

        TransactionWithRefundError expected = TransactionTestUtils.transactionWithRefundError(
                transactionWithRefundRequested,
                transactionRefundErrorEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
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
                .transactionClosureErrorEvent();
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithCompletedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent
        );

        TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(transactionWithRefundRequested, transactionRefundErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
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
                .transactionClosureErrorEvent();
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

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent
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
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                        && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                .equals(transactionAuthorizationCompleted)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithRequestedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);
        TransactionWithRefundError transactionWithRefundError = TransactionTestUtils.transactionWithRefundError(
                transactionWithRefundRequested,
                transactionRefundErrorEvent
        );
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent,
                transactionRefundedEvent

        );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundError, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithRequestedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);
        TransactionWithRefundError transactionWithRefundError = TransactionTestUtils.transactionWithRefundError(
                transactionWithRefundRequested,
                transactionRefundErrorEvent
        );
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent,
                transactionRefundedEvent

        );

        TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundError, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
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
                .transactionClosureErrorEvent();
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSED)
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
                .transactionClosureErrorEvent();
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
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent

        );

        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                (BaseTransactionWithRequestedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionExpiredEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
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
                .transactionClosureErrorEvent();
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
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent

        );

        TransactionWithRefundRequested expected = TransactionTestUtils.transactionWithRefundRequested(
                (BaseTransactionWithCompletedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionRefundRequestedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                                && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                        .equals(transactionAuthorizationCompleted)
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
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
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
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromWithClosureErrorForTransactionWithoutAuthorization() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent

        );

        TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorization() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
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
                (BaseTransactionWithCancellationRequested) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedevent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorizationIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
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
                (BaseTransactionWithCancellationRequested) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedevent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosureErrorFromTransactionWithCancellationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSURE_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithCancellationRequested() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithCancellationRequested);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent

        );

        TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithCancellationRequestedIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithCancellationRequested);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent,
                transactionUserCanceledEvent,
                transactionActivatedEvent

        );

        TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
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
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();

        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionUserCanceledEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureFailedEvent,
                transactionUserCanceledEvent,
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
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils.transactionWithUserReceiptKo(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithUserReceiptKo);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                transactionRefundRequestedEvent
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                                && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                        .equals(transactionWithUserReceiptKo)
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
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils.transactionWithUserReceiptKo(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithUserReceiptKo);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                transactionRefundRequestedEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent
        );

        TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                                && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                        .equals(transactionWithUserReceiptKo)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionIgnoringExpirationEventStartingFromNotAuthorizedState() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                expiredEvent,
                expiredEvent
        );

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
    void shouldConstructTransactionClosureErrorIgnoringTransactionNotAuthorizedAndNotCanceled() {
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(ZonedDateTime.now().toString());
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();
        TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionActivated);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        Transaction result = transactionWithClosureError.applyEvent(transactionActivatedEvent);
        assertEquals(transactionWithClosureError, result);
    }

    @Test
    void shouldIgnoreTransactionRefundRequestedEventFromAuthorizationCompleted() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(expected);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionRefundRequestedEvent

        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus())
                                        .equals(TransactionStatusDto.AUTHORIZATION_COMPLETED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromUserReceiptWithStatusNotifiedKO() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddedEvent transactionUserReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );
        TransactionExpiredEvent expiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        transactionUserReceiptRequestedEvent,
                        transactionUserReceiptAddedEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                expiredEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils.transactionWithUserReceiptKo(
                transactionWithRequestedUserReceipt,
                transactionUserReceiptAddedEvent
        );

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithUserReceiptKo, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionExpired) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
                                && (((TransactionExpired) t).getTransactionAtPreviousState().getStatus())
                                        .equals(TransactionStatusDto.NOTIFIED_KO)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorOKFromTransactionClosed() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptError) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((TransactionWithUserReceiptError) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorOKFromTransactionClosedIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                userReceiptAddErrorEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent,
                closureSentEvent,
                authorizationRequestedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptError) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((TransactionWithUserReceiptError) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)

                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorKOFromTransactionClosed() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.KO);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptError) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((TransactionWithUserReceiptError) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)

                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorKOFromTransactionClosedIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent transactionUserReceiptRequestedEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        transactionUserReceiptRequestedEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                userReceiptAddErrorEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                userReceiptAddErrorEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                userReceiptAddErrorEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptError) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((TransactionWithUserReceiptError) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithAddedUserReceiptOkFromTransactionWithUserReceiptError() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.KO);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        addUserReceiptEvent.getData()
                );
        TransactionUserReceiptAddedEvent userReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        userReceiptAddErrorEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                userReceiptAddedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        TransactionWithUserReceiptOk expected = TransactionTestUtils
                .transactionWithUserReceiptOk(transactionWithUserReceiptError, userReceiptAddedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptOk) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFIED_OK)
                                && (((TransactionWithUserReceiptOk) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithAddedUserReceiptKoFromTransactionWithUserReceiptError() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        addUserReceiptEvent.getData()
                );
        TransactionUserReceiptAddedEvent userReceiptAddedEvent = TransactionTestUtils
                .transactionUserReceiptAddedEvent(
                        userReceiptAddErrorEvent.getData()
                );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                userReceiptAddedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        TransactionWithUserReceiptKo expected = TransactionTestUtils
                .transactionWithUserReceiptKo(transactionWithUserReceiptError, userReceiptAddedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptKo) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFIED_KO)
                                && (((TransactionWithUserReceiptKo) t).getTransactionUserReceiptData()
                                        .equals(transactionUserReceiptData))
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithUserReceiptError() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        addUserReceiptEvent.getData()
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent,
                        userReceiptAddErrorEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                transactionExpiredEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithUserReceiptError, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionExpired) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
                                && (((TransactionExpired) t).getTransactionAtPreviousState().getStatus()
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR))
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromTransactionWithUserReceiptErrorForSendPaymentResultKO() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO)
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        addUserReceiptEvent.getData()
                );
        TransactionRefundRequestedEvent refundRequestedEvent = TransactionTestUtils.transactionRefundRequestedEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent,
                        userReceiptAddErrorEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                refundRequestedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(transactionClosed, addUserReceiptEvent);
        TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        TransactionWithRefundRequested expected = TransactionTestUtils.transactionWithRefundRequested(
                transactionWithUserReceiptError,
                refundRequestedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithRefundRequested) t).getStatus())
                                        .equals(TransactionStatusDto.REFUND_REQUESTED)
                                && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                        .equals(transactionWithUserReceiptError)
                )
                .verifyComplete();
    }

    @Test
    void shouldNotAllowConstructTransactionRefundRequestedFromTransactionWithUserReceiptErrorForSendPaymentResultOK() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionUserReceiptAddErrorEvent userReceiptAddErrorEvent = TransactionTestUtils
                .transactionUserReceiptAddErrorEvent(
                        addUserReceiptEvent.getData()
                );
        TransactionRefundRequestedEvent refundRequestedEvent = TransactionTestUtils.transactionRefundRequestedEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent,
                        userReceiptAddErrorEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                refundRequestedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((TransactionWithUserReceiptError) t).getStatus())
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithRequestedUserReceipt() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                transactionExpiredEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithRequestedUserReceipt,
                transactionExpiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionExpired transactionExpired = (TransactionExpired) t;
                            return expected.equals(transactionExpired)
                                    && transactionExpired.getStatus()
                                            .equals(TransactionStatusDto.EXPIRED)
                                    && transactionExpired
                                            .getTransactionAtPreviousState()instanceof TransactionWithRequestedUserReceipt userReceiptRequested
                                    && userReceiptRequested.getStatus() == TransactionStatusDto.NOTIFICATION_REQUESTED;

                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithRequestedUserReceiptIgnoringInvalidEvents() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                authorizedEvent,
                addUserReceiptEvent,
                transactionExpiredEvent,
                transactionActivatedEvent,
                authorizedEvent
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
        TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithRequestedUserReceipt,
                transactionExpiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionExpired transactionExpired = (TransactionExpired) t;
                            return expected.equals(transactionExpired)
                                    && transactionExpired.getStatus()
                                            .equals(TransactionStatusDto.EXPIRED)
                                    && transactionExpired
                                            .getTransactionAtPreviousState()instanceof TransactionWithRequestedUserReceipt userReceiptRequested
                                    && userReceiptRequested.getStatus() == TransactionStatusDto.NOTIFICATION_REQUESTED;

                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRequestedUserReceiptOKFromTransactionClosed() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent
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
        TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionWithRequestedUserReceipt tx = (TransactionWithRequestedUserReceipt) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.NOTIFICATION_REQUESTED)
                                    && tx.getTransactionUserReceiptData().equals(transactionUserReceiptData);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRequestedUserReceiptKOFromTransactionClosed() {
        EmptyTransaction transaction = new EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        transactionUserReceiptData
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                addUserReceiptEvent
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
        TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionWithRequestedUserReceipt tx = (TransactionWithRequestedUserReceipt) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.NOTIFICATION_REQUESTED)
                                    && tx.getTransactionUserReceiptData().equals(transactionUserReceiptData);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosedEventForNotAuthorizedTransactionInTransactionAuthorizationCompletedState() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionAuthorizationCompleted tx = (TransactionAuthorizationCompleted) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.AUTHORIZATION_COMPLETED);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosureFailedEventForAuthorizedTransactionInTransactionAuthorizationCompletedState() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureFailedEvent closureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureFailedEvent
        );

        TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionAuthorizationCompleted tx = (TransactionAuthorizationCompleted) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.AUTHORIZATION_COMPLETED);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosedEventForNotAuthorizedTransactionInTransactionClosureErrorState() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.KO);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
                transactionClosedEvent
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
                .expectNextMatches(
                        t -> {
                            TransactionWithClosureError tx = (TransactionWithClosureError) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.CLOSURE_ERROR);
                        }
                ).verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosureFailedEventForAuthorizedTransactionInTransactionClosureErrorState() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(AuthorizationResultDto.OK);
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureErrorEvent,
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

        TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionWithClosureError tx = (TransactionWithClosureError) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.CLOSURE_ERROR);
                        }
                ).verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                    "",
                    "transactionIdtransactionIdtransa"
            }
    )
    @NullSource
    void shouldFailReducingEvent(String transactionId) {

        TransactionActivatedEvent transactionActivatedEvent = Mockito
                .spy(TransactionTestUtils.transactionActivateEvent());
        Mockito.when(transactionActivatedEvent.getTransactionId()).thenReturn(transactionId);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> reducedEvent = Flux.just(transactionActivatedEvent)
                .reduce(new EmptyTransaction(), it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);
        StepVerifier.create(reducedEvent)
                .expectError(IllegalArgumentException.class)
                .verify();

    }

    @Test
    void shouldConvertTransactionIdSuccessfully() {
        UUID transactionId = UUID.randomUUID();
        String trimmedTransactionId = transactionId.toString().replace("-", "");

        TransactionActivatedEvent transactionActivatedEvent = Mockito
                .spy(TransactionTestUtils.transactionActivateEvent());
        Mockito.when(transactionActivatedEvent.getTransactionId()).thenReturn(trimmedTransactionId);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> reducedEvent = Flux.just(transactionActivatedEvent)
                .reduce(new EmptyTransaction(), it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);
        StepVerifier.create(reducedEvent)
                .expectNextMatches(t -> {
                    TransactionActivated tx = (TransactionActivated) t;
                    return tx.getTransactionId().uuid()
                            .equals(transactionId)
                            && tx.getTransactionId().value().equals(trimmedTransactionId);
                })
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionWithUserReceiptRecoveringFromExpiredTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent,
                transactionExpiredEvent,
                addUserReceiptEvent
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
        TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionWithRequestedUserReceipt transactionUserReceiptRequested = (TransactionWithRequestedUserReceipt) t;
                            return expected.equals(transactionUserReceiptRequested);

                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldNotConstructTransactionWithUserReceiptRecoveringFromExpiredTransactionForInvalidTransactionStatusBeforeExpiration() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionUserReceiptRequestedEvent addUserReceiptEvent = TransactionTestUtils
                .transactionUserReceiptRequestedEvent(
                        TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils.transactionExpiredEvent(
                TransactionTestUtils.reduceEvents(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizedEvent,
                        closureSentEvent,
                        addUserReceiptEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                transactionExpiredEvent,
                addUserReceiptEvent
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
        TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v1.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v1.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            TransactionExpired transactionExpired = (TransactionExpired) t;
                            return expected.equals(transactionExpired)
                                    && transactionExpired.getStatus()
                                            .equals(TransactionStatusDto.EXPIRED)
                                    && transactionExpired
                                            .getTransactionAtPreviousState()instanceof TransactionAuthorizationCompleted userReceiptRequested
                                    && userReceiptRequested.getStatus() == TransactionStatusDto.AUTHORIZATION_COMPLETED;

                        }
                )
                .verifyComplete();
    }

}
