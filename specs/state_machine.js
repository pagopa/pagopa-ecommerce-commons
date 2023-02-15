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
        entry: assign({ auth_outcome: "KO" }),
        on: {
          CLOSED: {
            target: "CLOSED",
          },
          CLOSURE_FAILED: {
            target: "UNAUTHORIZED",
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
        entry: assign({ closepayment_outcome: "KO" }),
        on: {
          ADD_USER_RECEIPT: {
            target: "NOTIFIED",
          },
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND: {
            target: "REFUNDED",
          },
        },
      },
      CANCELED: {
        type: "final",
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
            target: "EXPIRED_NOT_AUTHORIZED",
          },
          USER_CANCELED: {
            target: "CANCELED",
          },
        },
      },
      EXPIRED: {
        on: {
          REFUND: {
            target: "REFUNDED",
          },
        },
      },
      EXPIRED_NOT_AUTHORIZED: {
        type: "final",
      },
      REFUNDED: {
        type: "final",
      },
    },
    context: {
      auth_requested: false,
      closepayment_outcome: null,
      auth_outcome: null,
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
    },
  }
);
