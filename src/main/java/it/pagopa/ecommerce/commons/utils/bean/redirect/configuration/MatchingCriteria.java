package it.pagopa.ecommerce.commons.utils.bean.redirect.configuration;

import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/**
 * Matching criteria for redirect url configuration entry - this specifies the criteria to match for a redirect url configuration entry with transactional extracted parameters.
 *
 * @param paymentTypeCode - the payment method type code to match
 * @param pspId           - the psp id to match
 * @param touchpoint      - the touchpoint to match
 * @param channelId       - the channel id to match
 */
public record MatchingCriteria(
        @NotNull String paymentTypeCode,
        @Nullable String pspId,
        @Nullable String touchpoint,
        @Nullable String channelId
) {
    public MatchingCriteria {
        //payment type code is the only mandatory field
        if (paymentTypeCode == null) {
            throw new IllegalArgumentException("paymentTypeCode is mandatory");
        }
    }

    public Predicate<MatchingCriteria> toPredicate() {

        return compareToCriteria ->
                fieldMatchCondition(compareToCriteria.paymentTypeCode, paymentTypeCode)
                        && fieldMatchCondition(compareToCriteria.channelId, channelId)
                        && fieldMatchCondition(compareToCriteria.touchpoint, touchpoint)
                        && fieldMatchCondition(compareToCriteria.pspId, pspId);
    }

    private boolean fieldMatchCondition(String searchCriteria, String against) {
        return searchCriteria == null || searchCriteria.equals(against);
    }
}