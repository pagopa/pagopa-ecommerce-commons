import { createMachine, assign } from "xstate";

createMachine(
  {
    id: "eCommerce",
    initial: "ACTIVATED",
    states: {
      AUTH_REQUESTED: {
        on: {
          AUTHORIZATION_COMPLETED: {
            target: "AUTHORIZATION_COMPLETED",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
        },
      },
      AUTHORIZATION_COMPLETED: {
        entry: assign({ auth_outcome: "OK" }),
        on: {
          CLOSED: {
            target: "CLOSED",
            cond: "auth_outcome_ok"
          },
          CLOSURE_FAILED: {
            target: "UNAUTHORIZED",
            cond: "auth_outcome_ko"
          },
          CLOSURE_ERROR: {
            target: "CLOSURE_ERROR",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
        },
      },
      CLOSED: {
        entry: assign({
          closepayment_outcome: "OK",
          closepayment_response: "KO",
        }),
        on: {
          ADD_USER_RECEIPT: {
            target: "NOTIFIED",
            cond: "closepayment_response_ok",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND: {
            target: "REFUNDED",
          },
        },
      },
      CANCELLATION_REQUESTED: {
        entry: assign({ was_canceled: true }),
        on: {
          CLOSED: {
            target: "CANCELED"
          },
          CLOSURE_ERROR: {
            target: "CLOSURE_ERROR"
          },
          EXPIRE: {
            target: "EXPIRED"
          }
        }
      },
      UNAUTHORIZED: {
        type: "final",
      },
      CLOSURE_ERROR: {
        on: {
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND: {
            target: "REFUNDED",
          },
          CLOSURE_RETRIED: {},
          CLOSED: [
            {
              target: "CLOSED",
              cond: "auth_outcome_ok"
            },
            {
              target: "CANCELED",
              cond: "was_canceled"
            }
          ],
          CLOSURE_FAILED: {
            target: "UNAUTHORIZED",
            cond: "auth_outcome_ko"
          }
        },
      },
      NOTIFIED: {
        type: "final",
      },
      CANCELED: {
        type: "final"
      },
      ACTIVATED: {
        on: {
          TRANSACTION_AUTH_REQUESTED_EVENT: {
            target: "AUTH_REQUESTED",
          },
          EXPIRE: {
            target: "EXPIRED_NOT_AUTHORIZED",
          },
          USER_CANCELED: {
            target: "CANCELLATION_REQUESTED",
          },
        },
      },
      EXPIRED: {
        on: {
          REFUND: {
            target: "REFUNDED",
          },
          REFUND_ERROR: {
            target: "REFUND_ERROR"
          }
        },
      },
      EXPIRED_NOT_AUTHORIZED: {
        type: "final",
      },
      REFUNDED: {
        type: "final",
      },
      REFUND_ERROR: {
        on: {
          REFUND: {
            target: "REFUNDED"
          },
          REFUND_RETRIED: {}
        }
      }
    },
    context: {
      auth_requested: false,
      closepayment_outcome: null,
      closepayment_response: null,
      auth_outcome: null,
      was_canceled: null
    },
    predictableActionArguments: true,
    preserveActionOrder: true,
  },
  {
    guards: {
      auth_requested: (context, event) => context.auth_requested,
      closepayment_outcome_ok: (context, event) =>
        context.closepayment_outcome == "OK",
      closepayment_outcome_ko: (context, event) =>
        context.closepayment_outcome == "KO",
      auth_outcome_ok: (context, event) => context.auth_outcome == "OK",
      auth_outcome_ko: (context, event) => context.auth_outcome == "KO",
      closepayment_response_ok: (context, event) =>
        context.closepayment_response == "OK",
      was_canceled: (context, event) => context.was_canceled,
    },
  }
);
