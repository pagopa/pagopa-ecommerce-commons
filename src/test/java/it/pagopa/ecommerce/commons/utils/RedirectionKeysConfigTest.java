package it.pagopa.ecommerce.commons.utils;

import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.exceptions.RedirectConfigurationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class RedirectionKeysConfigTest {

    private static Stream<Arguments> redirectRetrieveUrlPaymentMethodsTestSearch() throws URISyntaxException {

        return Stream.of(
                Arguments.of(
                        "CHECKOUT",
                        "psp1",
                        "RBPR",
                        new URI("http://localhost:8096/redirections1/CHECKOUT")
                ),
                Arguments.of(
                        "IO",
                        "psp1",
                        "RBPR",
                        new URI("http://localhost:8096/redirections1/IO")
                ),
                Arguments.of(
                        "CHECKOUT",
                        "psp2",
                        "RBPB",
                        new URI("http://localhost:8096/redirections2")
                ),
                Arguments.of(
                        "IO",
                        "psp2",
                        "RBPB",
                        new URI("http://localhost:8096/redirections2")
                ),
                Arguments.of(
                        "CHECKOUT",
                        "psp3",
                        "RBPS",
                        new URI("http://localhost:8096/redirections3")
                ),
                Arguments.of(
                        "IO",
                        "psp3",
                        "RBPS",
                        new URI("http://localhost:8096/redirections3")
                )
        );
    }

    @ParameterizedTest
    @MethodSource("redirectRetrieveUrlPaymentMethodsTestSearch")
    void shouldReturnURIDuringSearchRedirectURLSearchingIteratively(
                                                                    String touchpoint,
                                                                    String pspId,
                                                                    String paymentMethodId,
                                                                    URI expectedUri
    ) {
        Map<String, String> redirectUrlMapping = Map.of(
                "CHECKOUT-psp1-RBPR",
                "http://localhost:8096/redirections1/CHECKOUT",
                "IO-psp1-RBPR",
                "http://localhost:8096/redirections1/IO",
                "psp2-RBPB",
                "http://localhost:8096/redirections2",
                "RBPS",
                "http://localhost:8096/redirections3"
        );
        Set<String> codeTypeList = Set.of(
                "CHECKOUT-psp1-RBPR",
                "IO-psp1-RBPR",
                "psp2-RBPB",
                "RBPS"
        );

        RedirectKeysConfiguration redirectionKeysConfig = new RedirectKeysConfiguration(
                redirectUrlMapping,
                codeTypeList
        );
        Either<RedirectConfigurationException, URI> result = redirectionKeysConfig
                .getRedirectUrlForPsp(touchpoint, pspId, paymentMethodId);
        assertTrue(result.isRight());
        assertEquals(expectedUri, result.get());
    }

    @Test
    void shouldReturnErrorDuringSearchRedirectURLforInvalidSearchKey() {
        Map<String, String> redirectUrlMapping = Map.of(
                "CHECKOUT-psp1-RBPR",
                "http://localhost:8096/redirections1/CHECKOUT",
                "IO-psp1-RBPR",
                "http://localhost:8096/redirections1/IO",
                "psp2-RBPB",
                "http://localhost:8096/redirections2",
                "RBPS",
                "http://localhost:8096/redirections3"
        );
        Set<String> codeTypeList = Set.of(
                "CHECKOUT-psp1-RBPR",
                "IO-psp1-RBPR",
                "psp2-RBPB",
                "RBPS"
        );
        String touchpoint = "CHECKOUT";
        String pspId = "psp1";
        String paymentMethodId = "RBPP";

        RedirectKeysConfiguration redirectionKeysConfig = new RedirectKeysConfiguration(
                redirectUrlMapping,
                codeTypeList
        );
        Either<RedirectConfigurationException, URI> result = redirectionKeysConfig
                .getRedirectUrlForPsp(touchpoint, pspId, paymentMethodId);
        assertTrue(result.isLeft());
        assertEquals(
                "Error parsing Redirect PSP BACKEND_URLS configuration, cause: Missing key for redirect return url with following search parameters: touchpoint: [%s] pspId: [%s] paymentTypeCode: [%s]"
                        .formatted(touchpoint, pspId, paymentMethodId),
                result.getLeft().getMessage()
        );
    }
}
