package it.pagopa.ecommerce.commons.client;

import it.pagopa.ecommerce.commons.exceptions.JwtIssuerClientException;
import it.pagopa.ecommerce.commons.exceptions.NpgResponseException;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.ApiClient;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.api.JwtIssuerApi;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenRequestDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.CreateTokenResponseDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKResponseDto;
import it.pagopa.ecommerce.commons.generated.jwtissuer.v1.dto.JWKSResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class JwtIssuerClientTests {

    @Mock
    private ApiClient apiClient;
    @Mock
    @Qualifier("jwtIssuerWebClient")
    private JwtIssuerApi jwtIssuerApi;

    private JwtIssuerClient jwtIssuerClient;

    @BeforeEach
    public void init() {
        jwtIssuerClient = new JwtIssuerClient(jwtIssuerApi);
    }

    @Test
    void shouldRetrieveJWKS() {
        JWKResponseDto jwkResponseDto = new JWKResponseDto();
        jwkResponseDto.alg("alg");
        jwkResponseDto.use("use");
        jwkResponseDto.e("e");
        jwkResponseDto.n("n");
        jwkResponseDto.kid("kid");
        jwkResponseDto.kty(JWKResponseDto.KtyEnum.RSA);
        JWKSResponseDto jwksResponseDto = new JWKSResponseDto().addKeysItem(jwkResponseDto);

        Mockito.when(
                jwtIssuerApi.getTokenPublicKeys()
        ).thenReturn(Mono.just(jwksResponseDto));

        StepVerifier
                .create(
                        jwtIssuerClient.getKeys()
                )
                .expectNext(jwksResponseDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenGetKeysThrows() {

        Mockito.when(
                jwtIssuerApi.getTokenPublicKeys()
        ).thenReturn(
                Mono.error(
                        new WebClientResponseException(
                                "Test error when calling getTokenPublicKeys",
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                null,
                                null,
                                null
                        )
                )
        );

        StepVerifier
                .create(
                        jwtIssuerClient.getKeys()
                )
                .expectError(JwtIssuerClientException.class)
                .verify();
    }

    @Test
    void shouldGenerateValidJWTToken() {
        String audience = "audience";
        Integer duration = 150000;

        Map<String, String> privateClaimsMap = new HashMap<>();
        privateClaimsMap.put("claim1", "value1");
        privateClaimsMap.put("claim2", "value2");
        privateClaimsMap.put("claim3", "value3");

        CreateTokenRequestDto createTokenRequestDto = new CreateTokenRequestDto()
                .audience(audience)
                .duration(duration)
                .privateClaims(privateClaimsMap);

        CreateTokenResponseDto createTokenResponseDto = new CreateTokenResponseDto();
        createTokenResponseDto.token("exampleToken");

        Mockito.when(
                jwtIssuerApi.createJwtToken(createTokenRequestDto)
        ).thenReturn(Mono.just(createTokenResponseDto));

        StepVerifier
                .create(
                        jwtIssuerClient.createJWTToken(audience, 150000, privateClaimsMap)
                )
                .expectNext(createTokenResponseDto)
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenCreateJwtTokenThrows() {
        String audience = "audience";
        Integer duration = 150000;

        Map<String, String> privateClaimsMap = new HashMap<>();
        privateClaimsMap.put("claim1", "value1");
        privateClaimsMap.put("claim2", "value2");
        privateClaimsMap.put("claim3", "value3");

        CreateTokenRequestDto createTokenRequestDto = new CreateTokenRequestDto()
                .audience(audience)
                .duration(duration)
                .privateClaims(privateClaimsMap);

        Mockito.when(
                jwtIssuerApi.createJwtToken(createTokenRequestDto)
        ).thenReturn(
                Mono.error(
                        new WebClientResponseException(
                                "Test error when calling createJwtToken",
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                                null,
                                null,
                                null
                        )
                )
        );

        StepVerifier
                .create(
                        jwtIssuerClient.createJWTToken(audience, 150000, privateClaimsMap)
                )
                .expectError(JwtIssuerClientException.class)
                .verify();
    }

}
