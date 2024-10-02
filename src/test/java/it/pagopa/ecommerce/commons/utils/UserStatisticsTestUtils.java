package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage;
import it.pagopa.ecommerce.commons.documents.userstats.v1.UserStatistics;

import java.time.Instant;
import java.util.UUID;

import static it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage.PaymentType.GUEST_PAYMENT_METHOD;
import static it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage.PaymentType.SAVED_WALLET;

public class UserStatisticsTestUtils {

    public static UserStatistics userStatisticsWallet() {
        return new UserStatistics("userId", lastUsageWallet());
    }

    private static LastUsage lastUsageWallet() {
        return new LastUsage(SAVED_WALLET, UUID.randomUUID(), Instant.now());
    }

    public static UserStatistics userStatisticsGuest() {
        return new UserStatistics("userId", lastUsageGuest());
    }

    private static LastUsage lastUsageGuest() {
        return new LastUsage(GUEST_PAYMENT_METHOD, UUID.randomUUID(), Instant.now());
    }
}
