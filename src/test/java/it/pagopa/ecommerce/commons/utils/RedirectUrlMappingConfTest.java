package it.pagopa.ecommerce.commons.utils;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationException;
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
                        "paymentTypeCode": "paymentTypeCode1",
                        "multiMatchKey": "noMatch"
                    }
                },
                {
                    "url": "http://localhost/psp2",
                    "matchingCriteria": {
                        "paymentTypeCode": "paymentTypeCode2",
                        "pspId": "pspId2",
                        "multiMatchKey": "multiMatchKey"
                    }
                },
                {
                    "url": "http://localhost/psp3",
                    "matchingCriteria": {
                        "pspId": "pspId3",
                        "touchpoint": "touchpoint3",
                        "paymentTypeCode": "paymentTypeCode3",
                        "multiMatchKey": "multiMatchKey"
                    }
                }
            ]
            """;

    private final String expectedMatchingCriteria = """
            [
                {
                    "paymentTypeCode": "paymentTypeCode1"
                },
                {
                    "paymentTypeCode": "paymentTypeCode2",
                    "pspId": "pspId2"
                },
                {
                    "pspId": "pspId3",
                    "touchpoint": "touchpoint3",
                    "paymentTypeCode": "paymentTypeCode3"
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
                                        "paymentTypeCode": "missing"
                                    }
                                ]
                                """
                )
        );
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Redirect url configuration does not match expected criteria: Error parsing Redirect PSP BACKEND_URLS configuration, cause: No configuration found for the provided matching criteria: {paymentTypeCode=missing}",
                exception.getMessage()
        );
    }

    @Test
    public void shouldLoadConfigurationSuccessfully() {
        RedirectUrlMappingEntry entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(Map.of("paymentTypeCode", "paymentTypeCode1"))
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
    }

    @Test
    public void shouldFetchConfigurationForPartialMatching() {
        RedirectUrlMappingEntry entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(Map.of("paymentTypeCode", "paymentTypeCode1"))
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode2"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode2",
                        "pspId",
                        "pspId2"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode3",
                        "pspId",
                        "pspId3"
                )
        )
                .get();
        assertEquals("http://localhost/psp3", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode3",
                        "pspId",
                        "pspId3",
                        "touchpoint",
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
                        "paymentTypeCode",
                        "paymentTypeCode1",
                        "duplicatedKey",
                        "test"
                )
        )
                .get();
        assertEquals("http://localhost/psp1", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode2",
                        "pspId",
                        "pspId2",
                        "duplicatedKey",
                        "test"
                )
        )
                .get();
        assertEquals("http://localhost/psp2", entry.url().toString());
        entry = redirectUrlMappingConf.getRedirectUrlForCriteria(
                Map.of(
                        "paymentTypeCode",
                        "paymentTypeCode3",
                        "pspId",
                        "pspId3",
                        "touchpoint",
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
                                "paymentTypeCode",
                                "noMatch"
                        )
                );
        assertTrue(entry.isLeft());
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: No configuration found for the provided matching criteria: {paymentTypeCode=noMatch}",
                entry.getLeft().getMessage()
        );
    }

    @Test
    public void shouldReturnErrorForMultipleEntriesFoundForCriteria() {
        // same key associated to multiple conf entries
        Either<RedirectConfigurationException, RedirectUrlMappingEntry> entry = redirectUrlMappingConf
                .getRedirectUrlForCriteria(
                        Map.of(
                                "multiMatchKey",
                                "multiMatchKey"
                        )
                );
        assertTrue(entry.isLeft());
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Multiple configurations found: [RedirectUrlMappingEntry[url=http://localhost/psp2, matchingCriteria={paymentTypeCode=paymentTypeCode2, pspId=pspId2, multiMatchKey=multiMatchKey}], RedirectUrlMappingEntry[url=http://localhost/psp3, matchingCriteria={pspId=pspId3, touchpoint=touchpoint3, paymentTypeCode=paymentTypeCode3, multiMatchKey=multiMatchKey}]] for the provided matching criteria: {multiMatchKey=multiMatchKey}",
                entry.getLeft().getMessage()
        );
    }

}
