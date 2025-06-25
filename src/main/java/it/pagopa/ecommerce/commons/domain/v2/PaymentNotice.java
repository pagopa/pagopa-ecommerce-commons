package it.pagopa.ecommerce.commons.domain.v2;

import it.pagopa.ecommerce.commons.annotations.ValueObject;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * <p>
 * A value object holding a notice code params.
 * </p>
 *
 * @param paymentToken           payment token used to perform Node activate
 * @param rptId                  payment notice RPT id
 * @param transactionAmount      payment notice amount
 * @param transactionDescription payment notice description, as received by Node
 *                               in activate response
 * @param paymentContextCode     payment context code
 * @param transferList           transfer list as received by Node in activate
 *                               response
 * @param isAllCCP               boolean flag indicating if all transfers are
 *                               associated to postal IBAN's
 * @param companyName            payment notice company name as received by Node
 *                               in activate response
 * @param creditorReferenceId    the creditor reference identifier
 */
@ValueObject
public record PaymentNotice(
        PaymentToken paymentToken,
        RptId rptId,
        TransactionAmount transactionAmount,
        TransactionDescription transactionDescription,
        PaymentContextCode paymentContextCode,

        List<PaymentTransferInfo> transferList,

        boolean isAllCCP,

        CompanyName companyName,
        @Nullable String creditorReferenceId
) {
}
