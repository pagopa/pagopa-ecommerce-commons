package it.pagopa.ecommerce.commons.domain;

import it.pagopa.ecommerce.commons.annotations.AggregateRoot;

/**
 * <p>
 * An interface for transaction aggregate roots.
 * </p>
 * <p>
 * Together with the POJOs defined under
 * {@link it.pagopa.ecommerce.commons.domain.pojos} it defines a mechanism to
 * reconstruct a transaction from event streams, e.g.
 * </p>
 *
 * <pre>
 * {@code
 *     Transaction replayAllEvents(Stream<TransactionEvent> events, Transaction baseTransaction) {
 *         return events.reduce(baseTransaction, (t, e) -> t.applyEvent(e));
 *     }
 * }
 * </pre>
 * <p>
 * Application of transaction events should adhere to the following flowchart
 * (here transaction states are represented):
 * </p>
 *
 * <pre>
 *     {@code
 *      ACTIVATION_REQUESTED
 *              │
 *              ▼
 *          ACTIVATED
 *              │
 *              ▼
 *      AUTHORIZATION_REQUESTED
 *              │
 *              ├────────► AUTHORIZATION_ERROR
 *              │
 *              ▼
 *   AUTHORIZED / AUTHORIZATION_FAILED
 *              │
 *              ├──────────► CLOSURE_ERROR
 *              │
 *              ▼
 *       CLOSED / CLOSURE_FAILED
 *              │
 *              ▼
 *           NOTIFIED
 *         }
 * </pre>
 * <p>
 * Also, application of events has the following properties:
 * <ul>
 * <li>You can apply a non-error events from the corresponding error events
 * (e.g. if you have applied a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureErrorEvent
 * TransactionClosureErrorEvent} you can still apply a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent
 * TransactionClosureSentEvent} afterwards)</li>
 * <li>Events in streams that do not follow the flow above are ignored (e.g. a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionActivatedEvent
 * TransactionActivatedEvent} after a
 * {@link it.pagopa.ecommerce.commons.documents.TransactionClosureSentEvent
 * TransactionClosureSentEvent})</li>
 * </ul>
 *
 * See also {@link it.pagopa.ecommerce.commons.domain.pojos.BaseTransaction} for
 * information on how to retrieve the reconstructed transaction data
 */
@AggregateRoot
public sealed interface Transaction permits EmptyTransaction,TransactionActivated,TransactionActivationRequested,TransactionWithRequestedAuthorization,TransactionAuthorized,TransactionWithFailedAuthorization,TransactionWithCompletedAuthorization,TransactionClosed,TransactionWithClosureError,TransactionWithUserReceipt {
    /**
     * Applies an event to a transaction
     *
     * @param event a transaction event
     * @return a new transaction object with the event applied
     */
    Transaction applyEvent(Object event);
}
