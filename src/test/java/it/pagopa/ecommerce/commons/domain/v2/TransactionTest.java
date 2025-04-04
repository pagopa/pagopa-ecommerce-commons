package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.documents.v2.*;
import it.pagopa.ecommerce.commons.documents.v2.authorization.*;
import it.pagopa.ecommerce.commons.domain.v2.pojos.*;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;
import it.pagopa.ecommerce.commons.v2.TransactionTestUtils;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionTest {
    @Test
    void shouldIgnoreInvalidEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(authorizationRequestedEvent);

        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction expected = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent event = TransactionTestUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated expected = TransactionTestUtils
                .transactionActivated(event.getCreationDate());

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStreamWithNpgInfo() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent event = TransactionTestUtils
                .transactionActivateEvent(TransactionTestUtils.npgTransactionGatewayActivationData());

        Flux<Object> events = Flux.just(event);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated expected = TransactionTestUtils
                .transactionActivated(
                        event.getCreationDate(),
                        TransactionTestUtils.npgTransactionGatewayActivationData()
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromInitEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent event = TransactionTestUtils.transactionActivateEvent();

        Flux<Object> events = Flux.just(event, event);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated expected = TransactionTestUtils
                .transactionActivated(event.getCreationDate());

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(transactionActivatedEvent, authorizationRequestedEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventWithNpgDetails() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        NpgTransactionGatewayAuthorizationRequestedData npgAuthRequestedData = (NpgTransactionGatewayAuthorizationRequestedData) TransactionTestUtils
                .npgTransactionGatewayAuthorizationRequestedData();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent(npgAuthRequestedData);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .assertNext(e -> {
                    NpgTransactionGatewayAuthorizationRequestedData npgTransactionGatewayAuthorizationRequestedData = (NpgTransactionGatewayAuthorizationRequestedData) ((BaseTransactionWithRequestedAuthorization) e)
                            .getTransactionAuthorizationRequestData().getTransactionGatewayAuthorizationRequestedData();
                    assertEquals(npgAuthRequestedData, npgTransactionGatewayAuthorizationRequestedData);
                })
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthRequestEventWithRedirectDetails() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        RedirectTransactionGatewayAuthorizationRequestedData redirectAuthRequestedData = (RedirectTransactionGatewayAuthorizationRequestedData) TransactionTestUtils
                .redirectTransactionGatewayAuthorizationRequestedData();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent(redirectAuthRequestedData);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .assertNext(e -> {
                    RedirectTransactionGatewayAuthorizationRequestedData redirectTransactionGatewayAuthorizationRequestedData = (RedirectTransactionGatewayAuthorizationRequestedData) ((BaseTransactionWithRequestedAuthorization) e)
                            .getTransactionAuthorizationRequestData().getTransactionGatewayAuthorizationRequestedData();
                    assertEquals(redirectAuthRequestedData, redirectTransactionGatewayAuthorizationRequestedData);
                })
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizedEventStreamNpgGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizedEventStreamRedirectGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.OK,
                                null
                        )
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthCompletedEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils.transactionClosed(

                transactionWithClosureRequested,
                closureSentEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptAddedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk expected = TransactionTestUtils
                .transactionWithUserReceiptOk(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosedEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils.transactionClosed(
                transactionWithClosureRequested,
                closureSentEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptAddedEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionAuthorizationCompleted(authorizedEvent, transactionWithRequestedAuthorization);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionWithCompletedAuthorization);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk expected = TransactionTestUtils
                .transactionWithUserReceiptOk(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);
        StepVerifier.create(actual).expectNextMatches(e -> e.equals(expected)).verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromClosureErrorEventStreamWithRecovery() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils
                .transactionClosed(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        closureSentEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromActivatedTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                expiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpiredNotAuthorized) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED_NOT_AUTHORIZED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromAuthorizationRequestedTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                expiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getTransactionAtPreviousState()
                                        .equals(transactionWithRequestedAuthorization)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromAuthorizedTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                expiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getTransactionAtPreviousState()
                                        .equals(transactionAuthorizationCompleted)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosedTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        closureSentEvent
                );

        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent,
                expiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getTransactionAtPreviousState()
                                        .equals(transactionClosed)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosureFailedTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        TransactionClosedEvent closedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(transactionWithClosureRequested, closedEvent);
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, expiredEvent);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                closedEvent,
                expiredEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getTransactionAtPreviousState()
                                        .equals(transactionClosed)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromExpiredEventStreamFromClosureErrorTransaction() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        expiredEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        v -> v.equals(expected)
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getTransactionAtPreviousState()
                                        .equals(transactionWithClosureError.getTransactionAtPreviousState())
                                && ((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) v)
                                        .getStatus() == TransactionStatusDto.EXPIRED
                )
                .verifyComplete();
    }

    @Test
    void transactionWithRequestedAuthorizationHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);

        assertEquals(TransactionStatusDto.AUTHORIZATION_REQUESTED, transactionWithRequestedAuthorization.getStatus());
    }

    @Test
    void transactionAuthorizedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
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
                OperationResultDto.EXECUTED,
                ((NpgTransactionGatewayAuthorizationData) transactionAuthorizationCompleted
                        .getTransactionAuthorizationCompletedData().getTransactionGatewayAuthorizationData())
                                .getOperationResult()
        );
    }

    @Test
    void transactionWithClosureErrorHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionAuthorizationCompleted);

        assertEquals(TransactionStatusDto.CLOSURE_ERROR, transactionWithClosureError.getStatus());
    }

    @Test
    void transactionClosedHasCorrectStatus() {
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionWithCompletedAuthorization = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionWithCompletedAuthorization);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
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
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        TransactionTestUtils.transactionClosedEvent(TransactionClosureData.Outcome.OK)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        TransactionTestUtils.transactionUserReceiptRequestedEvent(
                                TransactionTestUtils.transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK)
                        )
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk transactionWithUserReceiptOk = TransactionTestUtils
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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(
                        transactionClosureErrorEvent,
                        transactionWithClosureRequested
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosureErrorEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(
                        transactionClosureErrorEvent,
                        transactionWithClosureRequested
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent,
                transactionRefundedEvent,
                transactionClosureErrorEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        transactionClosedEvent
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionClosed);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromClosedEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        transactionClosedEvent
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionClosed);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        TransactionRefundedEvent transactionRefundedEvent = TransactionTestUtils
                .transactionRefundedEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                authorizationRequestedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent,
                transactionRefundedEvent,
                transactionClosedEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundFromExpiredEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        transactionClosedEvent
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionClosed);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosedEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
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
                transactionClosureRequestedEvent,
                transactionClosedEvent,
                transactionActivatedEvent,
                transactionExpiredEvent,
                transactionAuthorizationCompletedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionClosed, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosureErrorEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromClosureErrorEventStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(
                        (BaseTransactionWithRequestedAuthorization) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionExpiredEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationCompletedStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationCompletedStreamIgnoringInvalidEventsIgnoringInvalidEvents() {

        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromAuthorizationRequestedStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredNotAuthorizedFromActivatedStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED_NOT_AUTHORIZED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredNotAuthorizedFromActivatedStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, transactionExpiredEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED_NOT_AUTHORIZED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromActivatedStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested expected = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_REQUESTED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromActivatedStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationCompletedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStreamNpgGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DENIED_BY_RISK)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosedFromAuthorizationCompletedStreamNpgGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils
                .transactionClosed(transactionWithClosureRequested, transactionClosedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStreamRedirectGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.KO,
                                "Insufficient funds"
                        )
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosedFromAuthorizationCompletedStreamRedirectGateway() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.OK,
                                null
                        )
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils
                .transactionClosed(transactionWithClosureRequested, transactionClosedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSED)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedOK() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk expected = TransactionTestUtils
                .transactionWithUserReceiptOk(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_OK)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedKO() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo expected = TransactionTestUtils
                .transactionWithUserReceiptKo(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_KO)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromUserReceiptWithStatusNotifiedKOIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                transactionUserReceiptAddedEvent,
                authorizedEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo expected = TransactionTestUtils
                .transactionWithUserReceiptKo(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.NOTIFIED_KO)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithClosureEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithClosureEventIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

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

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(transactionWithCancellationRequested, transactionClosedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromCancellationRequestedWithExpirationEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithCancellationRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundEventIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionExpired.getTransactionAtPreviousState(),
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundRequested, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromRefundRequestedWithRefundError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(
                        transactionWithRefundRequested,
                        transactionRefundErrorEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundErrorFromTransactionWithAuthorizationRequested() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(
                        transactionWithRefundRequested,
                        transactionRefundErrorEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundErrorFromRefundRequestedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(
                        transactionClosureErrorEvent,
                        transactionWithClosureRequested
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(transactionWithRefundRequested, transactionRefundErrorEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_ERROR)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromClosureErrorEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(
                        transactionClosureErrorEvent,
                        transactionWithClosureRequested
                );

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                        && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                .equals(transactionWithClosureRequested)
        )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundedFromRefundError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithRequestedAuthorization) transactionExpired.getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils
                .transactionRefundErrorEvent(transactionWithRefundRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundError transactionWithRefundError = TransactionTestUtils
                .transactionWithRefundError(
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionRefunded expected = TransactionTestUtils
                .transactionRefunded(transactionWithRefundError, transactionRefundedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundedFromRefundErrorIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionAuthorizationCompleted, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
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

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUNDED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosedFromWithClosureError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosedEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed expected = TransactionTestUtils.transactionClosed(
                (BaseTransactionWithClosureRequested) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionClosedEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromWithClosureError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils.transactionExpired(
                (BaseTransactionWithRequestedAuthorization) transactionWithClosureError.getTransactionAtPreviousState(),
                transactionExpiredEvent
        );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromWithClosureError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithClosureError);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionRefundRequestedEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionRefundRequestedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.REFUND_REQUESTED)
                                && (((BaseTransactionWithRefundRequested) t).getTransactionAtPreviousState())
                                        .equals(transactionWithClosureRequested)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromWithClosureError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosureFailedEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionClosureFailedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromWithClosureErrorIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionAuthorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosureFailedEvent,
                transactionAuthorizationRequestedEvent,
                transactionClosureErrorEvent,
                transactionAuthorizationCompletedEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(
                        (BaseTransactionWithClosureRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionClosureFailedEvent
                );
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromWithClosureErrorForTransactionWithoutAuthorization() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithClosureError);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent,
                transactionExpiredEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorization() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

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

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(
                        (BaseTransactionWithCancellationRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionClosedevent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromWithClosureErrorForTransactionWithoutAuthorizationIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

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

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithCancellationRequested);
        it.pagopa.ecommerce.commons.domain.v2.TransactionUserCanceled expected = TransactionTestUtils
                .transactionUserCanceled(
                        (BaseTransactionWithCancellationRequested) transactionWithClosureError
                                .getTransactionAtPreviousState(),
                        transactionClosedevent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUserCanceledFromTransactionWithCancellationRequested() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosedEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionUserCanceled expected = TransactionTestUtils.transactionUserCanceled(
                transactionWithCancellationRequested,
                transactionClosedEvent
        );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionClosureErrorFromTransactionWithCancellationRequested() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionClosureErrorEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(
                        transactionClosureErrorEvent,
                        transactionWithCancellationRequested
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSURE_ERROR)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithCancellationRequested() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithCancellationRequested transactionWithCancellationRequested = TransactionTestUtils
                .transactionWithCancellationRequested(transactionActivated, transactionUserCanceledEvent);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithCancellationRequested);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                transactionUserCanceledEvent,
                transactionExpiredEvent

        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionCancellationExpired expected = TransactionTestUtils
                .transactionCancellationExpired(transactionWithCancellationRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithCancellationRequestedIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
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

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CANCELLATION_EXPIRED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionUnauthorizedFromAuthorizationCompletedStreamIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionUserCanceledEvent transactionUserCanceledEvent = TransactionTestUtils.transactionUserCanceledEvent();

        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionUserCanceledEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureFailedEvent,
                transactionUserCanceledEvent,
                transactionUserCanceledEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionUnauthorized expected = TransactionTestUtils
                .transactionUnauthorized(transactionAuthorizationCompleted, transactionClosureFailedEvent);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.UNAUTHORIZED)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRefundRequestedFromNotificationKoStatus() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils
                .transactionWithUserReceiptKo(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithUserReceiptKo);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                transactionRefundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils
                .transactionWithUserReceiptKo(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithUserReceiptKo);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                transactionClosureRequestedEvent,
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

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithUserReceiptKo, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionExpiredEvent expiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionActivated);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                expiredEvent,
                expiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpiredNotAuthorized expected = TransactionTestUtils
                .transactionExpiredNotAuthorized(transactionActivated, expiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(ZonedDateTime.now().toString());
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils.transactionClosureErrorEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError transactionWithClosureError = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionActivated);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        Transaction result = transactionWithClosureError.applyEvent(transactionActivatedEvent);
        assertEquals(transactionWithClosureError, result);
    }

    @Test
    void shouldIgnoreTransactionRefundRequestedEventFromAuthorizationCompleted() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent transactionAuthorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(transactionAuthorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
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

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                        transactionClosureRequestedEvent,
                        closureSentEvent,
                        transactionUserReceiptRequestedEvent,
                        transactionUserReceiptAddedEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                transactionUserReceiptAddedEvent,
                expiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo transactionWithUserReceiptKo = TransactionTestUtils
                .transactionWithUserReceiptKo(
                        transactionWithRequestedUserReceipt,
                        transactionUserReceiptAddedEvent
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithUserReceiptKo, expiredEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) t).getStatus())
                                        .equals(TransactionStatusDto.EXPIRED)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) t)
                                        .getTransactionAtPreviousState().getStatus())
                                                .equals(TransactionStatusDto.NOTIFIED_KO)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorOKFromTransactionClosed() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getTransactionUserReceiptData())
                                                .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorOKFromTransactionClosedIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                userReceiptAddErrorEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent,
                closureSentEvent,
                authorizationRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getTransactionUserReceiptData())
                                                .equals(transactionUserReceiptData)

                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorKOFromTransactionClosed() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                transactionUserReceiptRequestedEvent,
                userReceiptAddErrorEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getTransactionUserReceiptData())
                                                .equals(transactionUserReceiptData)

                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithNotificationErrorKOFromTransactionClosedIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                userReceiptAddErrorEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                userReceiptAddErrorEvent,
                transactionUserReceiptRequestedEvent,
                closureSentEvent,
                authorizationRequestedEvent,
                userReceiptAddErrorEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        transactionUserReceiptRequestedEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFICATION_ERROR)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError) t)
                                        .getTransactionUserReceiptData())
                                                .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithAddedUserReceiptOkFromTransactionWithUserReceiptError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                userReceiptAddedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk expected = TransactionTestUtils
                .transactionWithUserReceiptOk(transactionWithUserReceiptError, userReceiptAddedEvent);
        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptOk) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFIED_OK)
                                && (((TransactionWithUserReceiptOk) t).getTransactionUserReceiptData())
                                        .equals(transactionUserReceiptData)
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithAddedUserReceiptKoFromTransactionWithUserReceiptError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                userReceiptAddedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo expected = TransactionTestUtils
                .transactionWithUserReceiptKo(transactionWithUserReceiptError, userReceiptAddedEvent);
        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptKo) t)
                                        .getStatus())
                                                .equals(TransactionStatusDto.NOTIFIED_KO)
                                && (((TransactionWithUserReceiptKo) t).getTransactionUserReceiptData()
                                        .equals(transactionUserReceiptData))
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithUserReceiptError() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                        transactionClosureRequestedEvent,
                        closureSentEvent,
                        addUserReceiptEvent,
                        userReceiptAddErrorEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithUserReceiptError, transactionExpiredEvent);
        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> expected.equals(t)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) t).getStatus())
                                        .equals(TransactionStatusDto.EXPIRED)
                                && (((it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) t)
                                        .getTransactionAtPreviousState().getStatus()
                                        .equals(TransactionStatusDto.NOTIFICATION_ERROR))
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionRefundRequestedFromTransactionWithUserReceiptErrorForSendPaymentResultKO() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                refundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(transactionClosed, addUserReceiptEvent);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError transactionWithUserReceiptError = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(
                        transactionWithUserReceiptError,
                        refundRequestedEvent
                );
        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                userReceiptAddErrorEvent,
                refundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithUserReceiptError expected = TransactionTestUtils
                .transactionWithUserReceiptError(transactionWithRequestedUserReceipt, userReceiptAddErrorEvent);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithRequestedUserReceipt,
                transactionExpiredEvent
        );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = (it.pagopa.ecommerce.commons.domain.v2.TransactionExpired) t;
                            return expected.equals(transactionExpired)
                                    && transactionExpired.getStatus()
                                            .equals(TransactionStatusDto.EXPIRED)
                                    && transactionExpired
                                            .getTransactionAtPreviousState()instanceof it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt userReceiptRequested
                                    && userReceiptRequested.getStatus() == TransactionStatusDto.NOTIFICATION_REQUESTED;

                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionExpiredFromTransactionWithRequestedUserReceiptIgnoringInvalidEvents() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                authorizedEvent,
                addUserReceiptEvent,
                transactionExpiredEvent,
                transactionActivatedEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt transactionWithRequestedUserReceipt = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils.transactionExpired(
                transactionWithRequestedUserReceipt,
                transactionExpiredEvent
        );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionExpired transactionExpired = (TransactionExpired) t;
                            return expected.equals(transactionExpired)
                                    && transactionExpired.getStatus()
                                            .equals(TransactionStatusDto.EXPIRED)
                                    && transactionExpired
                                            .getTransactionAtPreviousState()instanceof it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt userReceiptRequested
                                    && userReceiptRequested.getStatus() == TransactionStatusDto.NOTIFICATION_REQUESTED;

                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithRequestedUserReceiptOKFromTransactionClosed() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.OK);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt tx = (it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt) t;
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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionUserReceiptData transactionUserReceiptData = TransactionTestUtils
                .transactionUserReceiptData(TransactionUserReceiptData.Outcome.KO);
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                transactionClosureRequestedEvent,
                closureSentEvent,
                addUserReceiptEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedUserReceipt tx = (TransactionWithRequestedUserReceipt) t;
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
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosedEvent closureSentEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureSentEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted tx = (it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.AUTHORIZATION_COMPLETED);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosureFailedEventForAuthorizedTransactionInTransactionAuthorizationCompletedState() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureFailedEvent closureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        authorizedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted tx = (it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.AUTHORIZATION_COMPLETED);
                        }
                )
                .verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosedEventForNotAuthorizedTransactionInTransactionClosureErrorState() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError tx = (it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.CLOSURE_ERROR);
                        }
                ).verifyComplete();
    }

    @Test
    void shouldIgnoreTransactionClosureFailedEventForAuthorizedTransactionInTransactionClosureErrorState() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureErrorEvent transactionClosureErrorEvent = TransactionTestUtils
                .transactionClosureErrorEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.OK);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureErrorEvent,
                transactionClosureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError expected = TransactionTestUtils
                .transactionWithClosureError(transactionClosureErrorEvent, transactionWithClosureRequested);

        Mono<Transaction> actual = events
                .reduce(transaction, Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNextMatches(
                        t -> {
                            it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureError tx = (TransactionWithClosureError) t;
                            return expected.equals(tx)
                                    && tx.getStatus()
                                            .equals(TransactionStatusDto.CLOSURE_ERROR);
                        }
                ).verifyComplete();
    }

    @Test
    void shouldHandleTransactionClosureRequestedAndIgnoreClosedEventForUnauthorizedRequest() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.DECLINED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);
        StepVerifier.create(actual).expectNextMatches(
                t -> transactionWithClosureRequested.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSURE_REQUESTED)
        )
                .verifyComplete();
    }

    @Test
    void shouldHandleTransactionClosureRequestedAndIgnoreClosureFailedEventForAuthorizedRequest() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        TransactionClosureFailedEvent transactionClosureFailedEvent = TransactionTestUtils
                .transactionClosureFailedEvent(TransactionClosureData.Outcome.KO);
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosureFailedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);
        StepVerifier.create(actual).expectNextMatches(
                t -> transactionWithClosureRequested.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.CLOSURE_REQUESTED)
        )
                .verifyComplete();
    }

    @Test
    void shouldHandleTransactionExpiredFromTransactionCloseRequestedEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();

        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(TransactionStatusDto.CLOSURE_REQUESTED);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionExpiredEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);

        it.pagopa.ecommerce.commons.domain.v2.TransactionExpired expected = TransactionTestUtils
                .transactionExpired(transactionWithClosureRequested, transactionExpiredEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);
        StepVerifier.create(actual).expectNextMatches(
                t -> expected.equals(t)
                        && (((BaseTransaction) t).getStatus()).equals(TransactionStatusDto.EXPIRED)
        )
                .verifyComplete();
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

        Mono<Transaction> reducedEvent = Flux.just(transactionActivatedEvent)
                .reduce(new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction(), Transaction::applyEvent);
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

        Mono<Transaction> reducedEvent = Flux.just(transactionActivatedEvent)
                .reduce(new EmptyTransaction(), Transaction::applyEvent);
        StepVerifier.create(reducedEvent)
                .expectNextMatches(t -> {
                    it.pagopa.ecommerce.commons.domain.v2.TransactionActivated tx = (TransactionActivated) t;
                    return tx.getTransactionId().uuid()
                            .equals(transactionId)
                            && tx.getTransactionId().value().equals(trimmedTransactionId);
                })
                .verifyComplete();

    }

    @Test
    void shouldConstructTransactionFromAuthRequestRetriedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationOutcomeWaitingEvent authorizationOutcomeWaitingEvent = TransactionTestUtils
                .transactionAuthorizationOutcomeWaitingEvent(0);

        Flux<Object> events = Flux
                .just(transactionActivatedEvent, authorizationRequestedEvent, authorizationOutcomeWaitingEvent);

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromMoreAuthRequestRetriedEventStream() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationOutcomeWaitingEvent authorizationOutcomeWaitingEvent = TransactionTestUtils
                .transactionAuthorizationOutcomeWaitingEvent(0);
        TransactionAuthorizationOutcomeWaitingEvent authorizationOutcomeWaitingEvent1 = TransactionTestUtils
                .transactionAuthorizationOutcomeWaitingEvent(1);
        TransactionAuthorizationOutcomeWaitingEvent authorizationOutcomeWaitingEvent2 = TransactionTestUtils
                .transactionAuthorizationOutcomeWaitingEvent(2);
        Flux<Object> events = Flux
                .just(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizationOutcomeWaitingEvent,
                        authorizationOutcomeWaitingEvent1,
                        authorizationOutcomeWaitingEvent2
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization expected = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionFromAuthorizationEventStreamWithAuthReqeustedRetriedEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationOutcomeWaitingEvent authorizationOutcomeWaitingEvent = TransactionTestUtils
                .transactionAuthorizationOutcomeWaitingEvent(0);
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );

        Flux<Object> events = Flux
                .just(
                        transactionActivatedEvent,
                        authorizationRequestedEvent,
                        authorizationOutcomeWaitingEvent,
                        transactionAuthorizationCompletedEvent
                );

        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated transactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, transactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted expected = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual)
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void shouldConstructTransactionWithUserReceiptRecoveringFromExpiredTransaction() {
        EmptyTransaction transaction = new EmptyTransaction();

        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent authorizedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.OK,
                                null
                        )
                );
        TransactionClosureRequestedEvent closureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
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
                        closureRequestedEvent,
                        closureSentEvent,
                        addUserReceiptEvent
                )
        );
        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                authorizedEvent,
                closureRequestedEvent,
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
        TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(
                        transactionAuthorizationCompleted
                );
        TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(

                        transactionWithClosureRequested,
                        closureSentEvent
                );
        TransactionWithRequestedUserReceipt expected = TransactionTestUtils
                .transactionWithRequestedUserReceipt(
                        transactionClosed,
                        addUserReceiptEvent
                );

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.redirectTransactionGatewayAuthorizationData(
                                RedirectTransactionGatewayAuthorizationData.Outcome.OK,
                                null
                        )
                );
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

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

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

    @Test
    void shouldReturnAuthorizationGatewayDataForARefundRequestedTransactionWithAuthCompletedEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        TransactionAuthorizationCompletedEvent transactionAuthorizationCompletedEvent = TransactionTestUtils
                .transactionAuthorizationCompletedEvent(
                        TransactionTestUtils.npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED)
                );
        TransactionClosedEvent transactionClosedEvent = TransactionTestUtils
                .transactionClosedEvent(TransactionClosureData.Outcome.OK);
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        it.pagopa.ecommerce.commons.domain.v2.TransactionAuthorizationCompleted transactionAuthorizationCompleted = TransactionTestUtils
                .transactionAuthorizationCompleted(
                        transactionAuthorizationCompletedEvent,
                        transactionWithRequestedAuthorization
                );
        TransactionClosureRequestedEvent transactionClosureRequestedEvent = TransactionTestUtils
                .transactionClosureRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithClosureRequested transactionWithClosureRequested = TransactionTestUtils
                .transactionWithClosureRequested(transactionAuthorizationCompleted);

        it.pagopa.ecommerce.commons.domain.v2.TransactionClosed transactionClosed = TransactionTestUtils
                .transactionClosed(
                        transactionWithClosureRequested,
                        transactionClosedEvent
                );
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionClosed, null);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionAuthorizationCompletedEvent,
                transactionClosureRequestedEvent,
                transactionClosedEvent,
                transactionRefundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionClosed, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).assertNext(
                t -> {
                    assertEquals(expected, t);
                    assertEquals(TransactionStatusDto.REFUND_REQUESTED, ((BaseTransaction) t).getStatus());
                    assertTrue(
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData()
                                    .isPresent()
                    );
                    assertEquals(
                            transactionAuthorizationCompleted.getTransactionAuthorizationCompletedData()
                                    .getTransactionGatewayAuthorizationData(),
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData().get()
                    );
                }

        )
                .verifyComplete();
    }

    @Test
    void shouldReturnAuthorizationGatewayDataRetrievingFromRefundRequestedEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        TransactionGatewayAuthorizationData gatewayAuthorizationData = TransactionTestUtils
                .npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired, gatewayAuthorizationData);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithRequestedAuthorization, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).assertNext(
                t -> {
                    assertEquals(expected, t);
                    assertEquals(TransactionStatusDto.REFUND_REQUESTED, ((BaseTransaction) t).getStatus());
                    assertTrue(
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData()
                                    .isPresent()
                    );
                    assertEquals(
                            gatewayAuthorizationData,
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData().get()
                    );
                }

        )
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyAuthorizationGatewayDataRetrievingFromRefundRequestedEventWithoutGatewayInformation() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);
        TransactionExpired transactionExpired = TransactionTestUtils
                .transactionExpired(transactionWithRequestedAuthorization, transactionExpiredEvent);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionExpired, null);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent
        );

        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRefundRequested expected = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithRequestedAuthorization, transactionRefundRequestedEvent);

        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).assertNext(
                t -> {
                    assertEquals(expected, t);
                    assertEquals(TransactionStatusDto.REFUND_REQUESTED, ((BaseTransaction) t).getStatus());
                    assertTrue(
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData().isEmpty()
                    );
                }

        )
                .verifyComplete();
    }

    @Test
    void shouldReturnAuthorizationGatewayDataRetrievingFromRefundRetryEvent() {
        it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction transaction = new it.pagopa.ecommerce.commons.domain.v2.EmptyTransaction();
        TransactionActivatedEvent transactionActivatedEvent = TransactionTestUtils.transactionActivateEvent();
        TransactionAuthorizationRequestedEvent authorizationRequestedEvent = TransactionTestUtils
                .transactionAuthorizationRequestedEvent();
        it.pagopa.ecommerce.commons.domain.v2.TransactionActivated TransactionActivated = TransactionTestUtils
                .transactionActivated(transactionActivatedEvent.getCreationDate());
        it.pagopa.ecommerce.commons.domain.v2.TransactionWithRequestedAuthorization transactionWithRequestedAuthorization = TransactionTestUtils
                .transactionWithRequestedAuthorization(authorizationRequestedEvent, TransactionActivated);
        TransactionExpiredEvent transactionExpiredEvent = TransactionTestUtils
                .transactionExpiredEvent(transactionWithRequestedAuthorization);
        TransactionGatewayAuthorizationData gatewayAuthorizationData = TransactionTestUtils
                .npgTransactionGatewayAuthorizationData(OperationResultDto.EXECUTED);
        TransactionRefundRequestedEvent transactionRefundRequestedEvent = TransactionTestUtils
                .transactionRefundRequestedEvent(transactionWithRequestedAuthorization, null);
        TransactionRefundErrorEvent transactionRefundErrorEvent = TransactionTestUtils.transactionRefundErrorEvent();
        TransactionRefundRetriedEvent transactionRefundRetriedEvent = TransactionTestUtils
                .transactionRefundRetriedEvent(1, gatewayAuthorizationData);

        Flux<Object> events = Flux.just(
                transactionActivatedEvent,
                authorizationRequestedEvent,
                transactionExpiredEvent,
                transactionRefundRequestedEvent,
                transactionRefundErrorEvent,
                transactionRefundRetriedEvent
        );

        TransactionWithRefundRequested transactionWithRefundRequested = TransactionTestUtils
                .transactionWithRefundRequested(transactionWithRequestedAuthorization, transactionRefundRequestedEvent);
        TransactionWithRefundError transactionWithRefundError = TransactionTestUtils
                .transactionWithRefundError(transactionWithRefundRequested, transactionRefundErrorEvent);
        TransactionWithRefundError expected = TransactionTestUtils
                .transactionWithRefundError(transactionWithRefundError, gatewayAuthorizationData);
        Mono<it.pagopa.ecommerce.commons.domain.v2.Transaction> actual = events
                .reduce(transaction, it.pagopa.ecommerce.commons.domain.v2.Transaction::applyEvent);

        StepVerifier.create(actual).assertNext(
                t -> {
                    assertEquals(expected, t);
                    assertEquals(TransactionStatusDto.REFUND_ERROR, ((BaseTransaction) t).getStatus());
                    assertTrue(
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData()
                                    .isPresent()
                    );
                    assertEquals(
                            gatewayAuthorizationData,
                            ((BaseTransactionWithRefundRequested) t).getTransactionAuthorizationGatewayData().get()
                    );
                }

        )
                .verifyComplete();
    }

}
