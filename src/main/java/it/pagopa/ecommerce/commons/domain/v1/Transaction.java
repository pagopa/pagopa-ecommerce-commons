package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.annotations.AggregateRoot;
import it.pagopa.ecommerce.commons.documents.v1.TransactionActivatedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionClosedEvent;
import it.pagopa.ecommerce.commons.documents.v1.TransactionClosureErrorEvent;
import it.pagopa.ecommerce.commons.domain.v1.pojos.BaseTransaction;

/**
 * <p>
 * An interface for transaction aggregate roots.
 * </p>
 * <p>
 * Together with the POJOs defined under
 * {@link it.pagopa.ecommerce.commons.domain.v1.pojos} it defines a mechanism to
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
 *
 *          ACTIVATED
 *              │
 *              ├────────► EXPIRED_NOT_AUTHORIZED / CANCELLATION_REQUESTED
 *              │
 *              ▼
 *      AUTHORIZATION_REQUESTED
 *              │
 *              ├────────► EXPIRED ────────────────────────────────┐
 *              │                                                  │
 *              ▼                                                  │
 *     AUTHORIZATION_COMPLETED                                     │───► REFUND_REQUESTED ───► REFUND_ERROR / REFUNDED
 *              │                                                  │
 *              ├──────────► CLOSURE_ERROR──────┐                  │
 *              ├──────────► EXPIRED ───────────┼──────────────────┚
 *              │                               │
 *              │                               │
 *              ──────────► UNAUTHORIZED        │
 *              │                               │
 *              ▼                               │
 *            CLOSED ───────────────────────────┘
 *              │
 *              ▼
 *    NOTIFIED OK / NOTIFIED KO
 *         }
 * </pre>
 * <p>
 * Also, application of events has the following properties:
 * <ul>
 * <li>You can apply a non-error events from the corresponding error events
 * (e.g. if you have applied a {@link TransactionClosureErrorEvent
 * TransactionClosureErrorEvent} you can still apply a
 * {@link TransactionClosedEvent TransactionClosureSentEvent} afterwards)</li>
 * <li>Events in streams that do not follow the flow above are ignored (e.g. a
 * {@link TransactionActivatedEvent TransactionActivatedEvent} after a
 * {@link TransactionClosedEvent TransactionClosureSentEvent})</li>
 * </ul>
 * <p>
 * See also {@link BaseTransaction} for information on how to retrieve the
 * reconstructed transaction data
 */
@AggregateRoot
public sealed interface Transaction permits EmptyTransaction, TransactionActivated, TransactionAuthorizationCompleted, TransactionClosed, TransactionExpired, TransactionExpiredNotAuthorized, TransactionRefunded, TransactionUnauthorized, TransactionUserCanceled, TransactionWithCancellationRequested, TransactionWithClosureError, TransactionWithRefundError, TransactionWithRefundRequested, TransactionWithRequestedAuthorization, TransactionWithUserReceipt {
    /**
     * Applies an event to a transaction
     *
     * @param event a transaction event
     * @return a new transaction object with the event applied
     */
    Transaction applyEvent(Object event);
}
