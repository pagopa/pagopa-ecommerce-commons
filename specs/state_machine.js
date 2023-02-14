/* State machine defined via xstate library. Viewable on stately.ai/viz */
import { createMachine, assign } from "xstate";

createMachine(
  {
    id: "eCommerce",
    initial: "ACTIVATED",
    states: {
      AUTH_REQUESTED: {
        on: {
          AUTHORIZED: {
            target: "AUTHORIZED",
          },
          AUTHORIZATION_FAILED: {
            target: "AUTHORIZATION_FAILED",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
        },
      },
      AUTHORIZED: {
        entry: assign({ previously_authorized: true }),
        on: {
          CLOSURE_SENT: {
            target: "CLOSED",
          },
          CLOSURE_ERROR: {
            target: "CLOSURE_ERROR",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
        },
      },
      AUTHORIZATION_FAILED: {
        on: {
          CLOSURE_FAILED: {
            target: "CLOSURE_FAILED",
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
        on: {
          ADD_USER_RECEIPT_OK: {
            target: "NOTIFIED",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
          ADD_USER_RECEIPT_KO: {
            target: "NOTIFIED_FAILED",
          },
        },
      },
      CLOSURE_FAILED: {
        on: {
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND: {
            target: "REFUNDED",
          },
        },
      },
      CLOSURE_ERROR: {
        on: {
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND: {
            target: "REFUNDED",
          },
          CLOSURE_SENT: {
            target: "CLOSED",
          },
          CLOSURE_RETRIED: {},
        },
      },
      NOTIFIED: {
        type: "final",
      },
      ACTIVATED: {
        on: {
          TRANSACTION_AUTH_REQUESTED_EVENT: {
            target: "AUTH_REQUESTED",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
        },
      },
      EXPIRED: {
        on: {
          REFUND: {
            target: "REFUNDED",
            cond: "previously_authorized",
          },
        },
      },
      REFUNDED: {
        type: "final",
      },
      NOTIFIED_FAILED: {
        type: "final",
      },
    },
    context: { previously_authorized: false },
    predictableActionArguments: true,
    preserveActionOrder: true,
  },
  {
    guards: {
      previously_authorized: (context, event) => context.previously_authorized,
    },
  }
);
