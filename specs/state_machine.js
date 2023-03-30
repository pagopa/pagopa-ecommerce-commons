import { createMachine, assign } from "xstate";

createMachine(
  {
    id: "eCommerce",
    initial: "ACTIVATED",
    states: {
      AUTH_REQUESTED: {
        entry: assign({ auth_requested: true }),
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
          }
        },
      },
      CLOSED: {
        entry: assign({
          closepayment_outcome: "OK",
          closepayment_response: "KO",
          sendpaymentresult_response: "KO"
        }),
        on: {
          SEND_USER_RECEIPT: {
            target: "NOTIFICATION_REQUESTED",

          },
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND_REQUESTED: {
            target: "REFUND_REQUESTED",
          }
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
            target: "CANCELLATION_EXPIRED"
          }
        }
      },
      UNAUTHORIZED: {
        type: "final",
      },
      CLOSURE_ERROR: {
        on: {
          EXPIRE: [
            {
              target: "EXPIRED",
              cond: "auth_requested",
            },
            {
              target: "CANCELLATION_EXPIRED",
              cond: "was_canceled",
            },
          ],
          REFUND_REQUESTED: {
            target: "REFUND_REQUESTED",
            cond: "auth_requested",
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
      NOTIFICATION_REQUESTED: {
        on: {
          USER_RECEIPT_ADDED: [{
            target: "NOTIFIED_OK",
            cond: "sendpaymentresult_response_ok",
          }, {
            target: "NOTIFIED_KO",
            cond: "sendpaymentresult_response_ko",
          }],
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND_REQUESTED: {
            target: "REFUND_REQUESTED",
            cond: "sendpaymentresult_response_ko"
          },
          ADD_USER_RECEIPT_ERROR:{
            target: "NOTIFICATION_ERROR"
          }
        },
      },
       NOTIFICATION_ERROR: {
        on: {
          USER_RECEIPT_ADDED: [{
            target: "NOTIFIED_OK",
            cond: "sendpaymentresult_response_ok",
          }, {
            target: "NOTIFIED_KO",
            cond: "sendpaymentresult_response_ko",
          }],
          EXPIRE: {
            target: "EXPIRED",
          },
          REFUND_REQUESTED: {
            target: "REFUND_REQUESTED",
            cond: "sendpaymentresult_response_ko"
          },
          ADD_USER_RECEIPT_RETRY:{
            target: "NOTIFICATION_ERROR"
          }
        },
      },
      NOTIFIED_OK: {
        type: "final",
      },
      NOTIFIED_KO: {
        on: {
            REFUND_REQUESTED: {
                target: "REFUND_REQUESTED"
            },
            EXPIRE: {
                target: "EXPIRED"
            }
        }
      },
      CANCELED: {
        type: "final"
      },
      CANCELLATION_EXPIRED: {
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
            target: "CANCELLATION_REQUESTED",
          },
        },
      },
      EXPIRED: {
        on: {
          REFUND_REQUESTED: {
            target: "REFUND_REQUESTED",
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
      REFUND_REQUESTED: {
        on: {
          REFUND: {
            target: "REFUNDED"
          },
          REFUND_ERROR: {
            target: "REFUND_ERROR"
          }
        }
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
      sendpaymentresult_response: null,
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
      sendpaymentresult_response_ok: (context, event) =>
        context.sendpaymentresult_response == "OK" && context.closepayment_response == "OK",
      sendpaymentresult_response_ko: (context, event) =>
        context.sendpaymentresult_response == "KO",
      was_canceled: (context, event) => context.was_canceled,
    },
  }
);
