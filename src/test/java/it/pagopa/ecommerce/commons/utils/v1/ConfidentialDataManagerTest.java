package it.pagopa.ecommerce.commons.utils.v1;

import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v1.Email;
import it.pagopa.ecommerce.commons.exceptions.ConfidentialDataException;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
import it.pagopa.ecommerce.commons.v1.TransactionTestUtils;
import it.pagopa.generated.pdv.v1.api.TokenApi;
import it.pagopa.generated.pdv.v1.dto.PiiResourceDto;
import it.pagopa.generated.pdv.v1.dto.TokenResourceDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.util.UUID;

import static it.pagopa.ecommerce.commons.v1.TransactionTestUtils.EMAIL_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class ConfidentialDataManagerTest {

    private final TokenApi pdvClient = Mockito.mock(TokenApi.class);

    private final ConfidentialDataManager confidentialDataManager = new ConfidentialDataManager(
            TransactionTestUtils.constructKeySpec(),
            pdvClient
    );

    @Test
    void shouldEncryptAndDecryptMailSuccessfullyWithAES() {
        Email email = new Email(EMAIL_STRING);
        Confidential<Email> encrypted = confidentialDataManager
                .encrypt(ConfidentialDataManager.Mode.AES_GCM_NOPAD, email).block();
        Email decrypted = confidentialDataManager.decrypt(encrypted, Email::new).block();

        assertEquals(email, decrypted);
    }

    @Test
    void shouldEncryptAndDecryptMailSuccessfullyWithPDV() {
        Email email = new Email(EMAIL_STRING);

        TokenResourceDto pdvToken = new TokenResourceDto().token(UUID.randomUUID());

        /* preconditions */
        Mockito.when(pdvClient.saveUsingPUT(new PiiResourceDto().pii(EMAIL_STRING))).thenReturn(Mono.just(pdvToken));
        Mockito.when(pdvClient.findPiiUsingGET(pdvToken.getToken().toString()))
                .thenReturn(Mono.just(new PiiResourceDto().pii(EMAIL_STRING)));

        /* test */
        Confidential<Email> encrypted = confidentialDataManager
                .encrypt(ConfidentialDataManager.Mode.PERSONAL_DATA_VAULT, email).block();
        Email decrypted = confidentialDataManager.decrypt(encrypted, Email::new).block();

        /* assertions */
        assertEquals(email, decrypted);
    }

    @Test
    void shouldFailEncryptionForInvalidConfiguredKey() {
        ConfidentialDataManager misconfiguredKeyConfidentialDataManager = new ConfidentialDataManager(
                new SecretKeySpec(new byte[1], "AES"),
                pdvClient
        );

        assertThrows(
                ConfidentialDataException.class,
                () -> misconfiguredKeyConfidentialDataManager
                        .encrypt(ConfidentialDataManager.Mode.AES_GCM_NOPAD, new Email(EMAIL_STRING)).block()
        );

    }

    @Test
    void shouldFailDecryptionForInvalidConfiguredKey() {
        ConfidentialDataManager misconfiguredKeyConfidentialDataManager = new ConfidentialDataManager(
                new SecretKeySpec(new byte[1], "AES"),
                pdvClient
        );
        assertThrows(
                ConfidentialDataException.class,
                () -> misconfiguredKeyConfidentialDataManager
                        .encrypt(ConfidentialDataManager.Mode.AES_GCM_NOPAD, new Email(EMAIL_STRING)).block()
        );
    }

    @Test
    void shouldWrapWebClientResponseExceptionOnEncryptionWithPDV() {
        Email email = new Email(EMAIL_STRING);

        WebClientResponseException responseException = WebClientResponseException.create(
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                HttpHeaders.EMPTY,
                null,
                Charset.defaultCharset(),
                null
        );

        /* preconditions */
        Mockito.when(pdvClient.saveUsingPUT(new PiiResourceDto().pii(EMAIL_STRING)))
                .thenReturn(Mono.error(responseException));

        /* assertions */
        StepVerifier.create(confidentialDataManager.encrypt(ConfidentialDataManager.Mode.PERSONAL_DATA_VAULT, email))
                .expectError(ConfidentialDataException.class)
                .verify();
    }

    @Test
    void shouldWrapWebClientResponseExceptionOnDecryptionWithPDV() {
        Email email = new Email(EMAIL_STRING);

        TokenResourceDto pdvToken = new TokenResourceDto().token(UUID.randomUUID());

        WebClientResponseException responseException = WebClientResponseException.create(
                HttpStatus.BAD_GATEWAY.value(),
                HttpStatus.BAD_GATEWAY.getReasonPhrase(),
                HttpHeaders.EMPTY,
                null,
                Charset.defaultCharset(),
                null
        );

        /* preconditions */
        Mockito.when(pdvClient.saveUsingPUT(new PiiResourceDto().pii(EMAIL_STRING))).thenReturn(Mono.just(pdvToken));
        Mockito.when(pdvClient.findPiiUsingGET(pdvToken.getToken().toString()))
                .thenReturn(Mono.error(responseException));

        /* test */
        Confidential<Email> encrypted = confidentialDataManager
                .encrypt(ConfidentialDataManager.Mode.PERSONAL_DATA_VAULT, email).block();

        /* assertions */
        StepVerifier.create(confidentialDataManager.decrypt(encrypted, Email::new))
                .expectError(ConfidentialDataException.class)
                .verify();
    }
}