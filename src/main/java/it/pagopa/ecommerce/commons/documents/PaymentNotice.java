package it.pagopa.ecommerce.commons.documents;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Base persistence view for notice code.
 */
@Data
@Document
@NoArgsConstructor
@Generated
public class PaymentNotice {
    private String paymentToken;
    private String rptId; // TODO To be splitted
    private String description;
    private Integer amount;
    private String paymentContextCode;
    private List<PaymentTransferInformation> transferList;
    private boolean isAllCCP;
    private String companyName;
    @Nullable
    private String creditorReferenceId;

    /**
     * All-args constructor
     *
     * @param paymentToken        the payment token
     * @param rptId               the rptId
     * @param description         the transaction description
     * @param amount              the transaction amount
     * @param paymentContextCode  the payment context code
     * @param transferList        the list of transfer information
     * @param isAllCCP            the isAllCCP flag
     * @param companyName         the company name
     * @param creditorReferenceId the creditor reference id
     */
    @SuppressWarnings("java:S107") // Constructor has 9 parameters, required for payment notice model
    public PaymentNotice(
            String paymentToken,
            String rptId,
            String description,
            Integer amount,
            String paymentContextCode,
            List<PaymentTransferInformation> transferList,
            boolean isAllCCP,
            String companyName,
            String creditorReferenceId
    ) {
        this.paymentToken = paymentToken;
        this.rptId = rptId;
        this.description = description;
        this.amount = amount;
        this.paymentContextCode = paymentContextCode;
        this.transferList = transferList;
        this.isAllCCP = isAllCCP;
        this.companyName = companyName;
        this.creditorReferenceId = creditorReferenceId;
    }
}
