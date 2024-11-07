package it.pagopa.ecommerce.commons.v2;

import it.pagopa.ecommerce.commons.documents.DeadLetterEvent;
import it.pagopa.ecommerce.commons.documents.v2.TransactionAuthorizationRequestData;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterNpgTransactionInfoDetailsData;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterRedirectTransactionInfoDetailsData;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterTransactionInfo;
import it.pagopa.ecommerce.commons.documents.v2.deadletter.DeadLetterTransactionInfoDetailsData;
import it.pagopa.ecommerce.commons.generated.npg.v1.dto.OperationResultDto;
import it.pagopa.ecommerce.commons.generated.server.model.TransactionStatusDto;

import java.util.List;

public class DeadLetterTestUtils {

    public static DeadLetterEvent deadLetterEventWithoutTransactionInfo() {
        return new DeadLetterEvent(
                "id",
                "queue name",
                "2024-08-27T10:07:20.768428223Z",
                "Dead letter data",
                null
        );
    }

    public static DeadLetterEvent deadLetterEventWithIncompleteTransactionInfo() {
        return new DeadLetterEvent(
                "id",
                "queue name",
                "2024-08-27T10:07:20.768428223Z",
                "Dead letter data",
                new DeadLetterTransactionInfo(
                        null,
                        null,
                        null,
                        null,
                        null,
                        "pspId",
                        "paymentMethodName",
                        120,
                        "rrn",
                        null
                )
        );
    }

    public static DeadLetterEvent deadLetterEventWithTransactionInfo(
                                                                     TransactionAuthorizationRequestData.PaymentGateway gateway
    ) {
        DeadLetterTransactionInfoDetailsData details;
        switch (gateway) {
            case NPG -> details = new DeadLetterNpgTransactionInfoDetailsData(
                    OperationResultDto.CANCELED,
                    "operationId",
                    "correlationaId",
                    "paymentEndToEndId"
            );
            case REDIRECT -> details = new DeadLetterRedirectTransactionInfoDetailsData(
                    "outcome"
            );
            default -> details = null;
        }
        return new DeadLetterEvent(
                "id",
                "queue name",
                "2024-08-27T10:07:20.768428223Z",
                "Dead letter data",
                new DeadLetterTransactionInfo(
                        "transactionId",
                        "authorizationRequestId",
                        TransactionStatusDto.EXPIRED,
                        gateway,
                        List.of("payment token"),
                        "pspId",
                        "paymentMethodName",
                        120,
                        "rrn",
                        details
                )
        );
    }
}
