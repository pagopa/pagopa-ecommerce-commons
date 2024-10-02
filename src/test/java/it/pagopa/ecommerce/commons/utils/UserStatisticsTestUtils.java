package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.documents.UserStatistics;

import java.time.Instant;
import java.util.UUID;

import static it.pagopa.ecommerce.commons.documents.UserStatistics.LastUsage.PaymentType.GUEST_PAYMENT_METHOD;
import static it.pagopa.ecommerce.commons.documents.UserStatistics.LastUsage.PaymentType.SAVED_WALLET;

public class UserStatisticsTestUtils {

    public static UserStatistics userStatisticsWallet() {
        return new UserStatistics("userId", lastUsageWallet());
    }

    private static UserStatistics.LastUsage lastUsageWallet() {
        return new UserStatistics.LastUsage(SAVED_WALLET, UUID.randomUUID(), Instant.now());
    }

    public static UserStatistics userStatisticsGuest() {
        return new UserStatistics("userId", lastUsageGuest());
    }

    private static UserStatistics.LastUsage lastUsageGuest() {
        return new UserStatistics.LastUsage(GUEST_PAYMENT_METHOD, UUID.randomUUID(), Instant.now());
    }
}
