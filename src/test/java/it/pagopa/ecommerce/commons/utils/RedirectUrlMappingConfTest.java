package it.pagopa.ecommerce.commons.utils;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationException;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.RedirectUrlMappingCriteria;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.RedirectUrlMappingEntry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RedirectUrlMappingConfTest {

    private final String urlConfiguration = """
            [
                {
                    "url": "http://localhost/psp1",
                    "matchingCriteria": {
                        "PAYMENT_TYPE_CODE": "paymentTypeCode1",
                        "PSP_CHANNEL_ID": "noMatch"
                    }
                },
                {
                    "url": "http://localhost/psp2",
                    "matchingCriteria": {
                        "PAYMENT_TYPE_CODE": "paymentTypeCode2",
                        "PSP_ID": "pspId2",
                        "PSP_CHANNEL_ID": "multiMatchKey"
                    }
                },
                {
                    "url": "http://localhost/psp3",
                    "matchingCriteria": {
                        "PSP_ID": "pspId3",
                        "TOUCHPOINT": "touchpoint3",
                        "PAYMENT_TYPE_CODE": "paymentTypeCode3",
                        "PSP_CHANNEL_ID": "multiMatchKey"
                    }
                }
            ]
            """;

    private final String expectedMatchingCriteria = """
            [
                {
                    "PAYMENT_TYPE_CODE": "paymentTypeCode1"
                },
                {
                    "PAYMENT_TYPE_CODE": "paymentTypeCode2",
                    "PSP_ID": "pspId2"
                },
                {
                    "PSP_ID": "pspId3",
                    "TOUCHPOINT": "touchpoint3",
                    "PAYMENT_TYPE_CODE": "paymentTypeCode3"
                }
            ]
            """;

    private final String overlappingConf = """
            [
                {
                    "url": "http://localhost/psp1",
                    "matchingCriteria": {
                        "PAYMENT_TYPE_CODE": "paymentTypeCode",
                        "PSP_ID": "pspId"
                    }
                },
                {
                    "url": "http://localhost/psp2",
                    "matchingCriteria": {
                        "PAYMENT_TYPE_CODE": "paymentTypeCode",
                        "PSP_ID": "pspId",
                        "PSP_CHANNEL_ID": "channelId"
                    }
                }
            ]
            """;
    private final RedirectUrlMappingConf redirectUrlMappingConf = new RedirectUrlMappingConf(
            urlConfiguration,
            expectedMatchingCriteria
    );

    @Test
    public void shouldNotInstantiateConfForInvalidConfigurationConf() {
        RedirectConfigurationException exception = assertThrows(
                RedirectConfigurationException.class,
                () -> new RedirectUrlMappingConf(
                        "{}",
                        expectedMatchingCriteria
                )
        );
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Invalid redirect url configuration: error parsing json values",
                exception.getMessage()
        );
    }

    @Test
    public void shouldNotInstantiateConfForMissingRequiredKeys() {
        RedirectConfigurationException exception = assertThrows(
                RedirectConfigurationException.class,
                () -> new RedirectUrlMappingConf(
                        urlConfiguration,
                        """
                                [
                                    {
                                        "PAYMENT_TYPE_CODE": "missing"
                                    }
                                ]
                                """
                )
        );
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Redirect url configuration does not match expected criteria: Error parsing Redirect PSP BACKEND_URLS configuration, cause: No configuration found for the provided matching criteria: {PAYMENT_TYPE_CODE=missing}",
                exception.getMessage()
        );
    }

    @Test
    public void shouldLoadConfigurationSuccessfully() {
        RedirectUrlMappingEntry entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(Map.of(RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE, "paymentTypeCode1"))
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
    }

    @Test
    public void shouldFetchConfigurationForPartialMatching() {
        RedirectUrlMappingEntry entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(Map.of(RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE, "paymentTypeCode1"))
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode2"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode2",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId2"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode3",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode3",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId3",
                        RedirectUrlMappingCriteria.TOUCHPOINT,
                        "touchpoint3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
    }

    @Test
    public void shouldFetchConfigurationForFullCriteriaMatching() {
        RedirectUrlMappingEntry entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode1",
                        RedirectUrlMappingCriteria.TOUCHPOINT,
                        "test"
                )
        )
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode2",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId2",
                        RedirectUrlMappingCriteria.TOUCHPOINT,
                        "test"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode3",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId3",
                        RedirectUrlMappingCriteria.TOUCHPOINT,
                        "touchpoint3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
    }

    @Test
    public void shouldReturnErrorForNoKeyFoundForCriteria() {
        Either<RedirectConfigurationException, RedirectUrlMappingEntry> entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(
                        Map.of(
                                RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                                "noMatch"
                        )
                );
        assertTrue(entry.isLeft());
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: No configuration found for the provided matching criteria: {PAYMENT_TYPE_CODE=noMatch}",
                entry.getLeft().getMessage()
        );
    }

    @Test
    public void shouldReturnErrorForMultipleEntriesFoundForCriteria() {
        // same key associated to multiple conf entries
        Either<RedirectConfigurationException, RedirectUrlMappingEntry> entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(
                        Map.of(
                                RedirectUrlMappingCriteria.PSP_CHANNEL_ID,
                                "multiMatchKey"
                        )
                );
        assertTrue(entry.isLeft());
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Multiple configurations found: [RedirectUrlMappingEntry[url=http://localhost/psp2, matchingCriteria={PAYMENT_TYPE_CODE=paymentTypeCode2, PSP_ID=pspId2, PSP_CHANNEL_ID=multiMatchKey}], RedirectUrlMappingEntry[url=http://localhost/psp3, matchingCriteria={PAYMENT_TYPE_CODE=paymentTypeCode3, PSP_ID=pspId3, TOUCHPOINT=touchpoint3, PSP_CHANNEL_ID=multiMatchKey}]] for the provided matching criteria: {PSP_CHANNEL_ID=multiMatchKey}",
                entry.getLeft().getMessage()
        );
    }

    @Test
    public void shouldReturnConfigurationWithHigherMatchingParametersCount() {

        RedirectUrlMappingConf conf = new RedirectUrlMappingConf(overlappingConf, "[]");
        Either<RedirectConfigurationException, RedirectUrlMappingEntry> result = conf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId",
                        RedirectUrlMappingCriteria.PSP_CHANNEL_ID,
                        "channelId"
                )
        );
        assertTrue(result.isRight());
        assertEquals("http://localhost/psp2", result.get().url().toString());
    }

    @Test
    public void shouldReturnErrorForConfigurationWithSameMatchingParametersCount() {

        RedirectUrlMappingConf conf = new RedirectUrlMappingConf(overlappingConf, "[]");
        Either<RedirectConfigurationException, RedirectUrlMappingEntry> result = conf.getRedirectUrlForCriteria(
                Map.of(
                        RedirectUrlMappingCriteria.PAYMENT_TYPE_CODE,
                        "paymentTypeCode",
                        RedirectUrlMappingCriteria.PSP_ID,
                        "pspId"
                )
        );
        assertTrue(result.isLeft());
        assertTrue(
                result.getLeft().getMessage().startsWith(
                        "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Multiple configurations found"
                )

        );
    }

}
