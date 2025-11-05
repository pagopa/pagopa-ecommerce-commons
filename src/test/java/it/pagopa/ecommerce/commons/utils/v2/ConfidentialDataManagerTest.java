package it.pagopa.ecommerce.commons.utils.v2;

import it.pagopa.ecommerce.commons.domain.Confidential;
import it.pagopa.ecommerce.commons.domain.v2.Email;
import it.pagopa.ecommerce.commons.domain.v2.FiscalCode;
import it.pagopa.ecommerce.commons.exceptions.ConfidentialDataException;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
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

import java.nio.charset.Charset;
import java.util.UUID;

import static it.pagopa.ecommerce.commons.v2.TransactionTestUtils.EMAIL_STRING;
import static it.pagopa.ecommerce.commons.v2.TransactionTestUtils.FISCAL_CODE_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ConfidentialDataManagerTest {

    private final TokenApi pdvClient = Mockito.mock(TokenApi.class);

    private final ConfidentialDataManager confidentialDataManager = new ConfidentialDataManager(pdvClient);

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
                .encrypt(email).block();
        Email decrypted = confidentialDataManager.decrypt(encrypted, Email::new).block();

        /* assertions */
        assertEquals(email, decrypted);
    }

    @Test
    void shouldEncryptAndDecryptFiscalCodeSuccessfullyWithPDV() {
        FiscalCode fiscalCode = new FiscalCode(FISCAL_CODE_STRING);

        TokenResourceDto pdvToken = new TokenResourceDto().token(UUID.randomUUID());

        /* preconditions */
        Mockito.when(pdvClient.saveUsingPUT(new PiiResourceDto().pii(FISCAL_CODE_STRING)))
                .thenReturn(Mono.just(pdvToken));
        Mockito.when(pdvClient.findPiiUsingGET(pdvToken.getToken().toString()))
                .thenReturn(Mono.just(new PiiResourceDto().pii(FISCAL_CODE_STRING)));

        /* test */
        Confidential<FiscalCode> encrypted = confidentialDataManager
                .encrypt(fiscalCode).block();
        FiscalCode decrypted = confidentialDataManager.decrypt(encrypted, FiscalCode::new).block();

        /* assertions */
        assertEquals(fiscalCode, decrypted);
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
        StepVerifier.create(confidentialDataManager.encrypt(email))
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
                .encrypt(email).block();

        /* assertions */
        StepVerifier.create(confidentialDataManager.decrypt(encrypted, Email::new))
                .expectError(ConfidentialDataException.class)
                .verify();
    }
}
