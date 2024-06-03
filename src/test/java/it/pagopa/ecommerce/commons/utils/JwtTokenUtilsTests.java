package it.pagopa.ecommerce.commons.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;

class JwtTokenUtilsTests {
    private static final String STRONG_KEY = "ODMzNUZBNTZENDg3NTYyREUyNDhGNDdCRUZDNzI3NDMzMzQwNTFEREZGQ0MyQzA5Mjc1RjY2NTQ1NDk5MDMxNzU5NDc0NUVFMTdDMDhGNzk4Q0Q3RENFMEJBODE1NURDREExNEY2Mzk4QzFEMTU0NTExNjUyMEExMzMwMTdDMDk";
    private static final int TOKEN_VALIDITY_TIME_SECONDS = 900;
    private final SecretKey jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(STRONG_KEY));
    private final JwtTokenUtils jwtTokenUtils = new JwtTokenUtils();

    @Test
    void shouldGenerateValidJwtTokenWithOrderIdAndTransactionId() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                orderId,
                null,
                null
        );

        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);
        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(orderId, claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.PAYMENT_METHOD_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateValidJwtTokenWithOnlyTransactionId() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                null,
                null,
                null
        );
        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);
        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateValidJwtTokenWithWithOrderIdAndTransactionIdAndPaymentMethodId() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        String paymentMethodId = UUID.randomUUID().toString();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                orderId,
                paymentMethodId,
                null
        );
        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);

        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(orderId, claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertEquals(paymentMethodId, claims.get(JwtTokenUtils.PAYMENT_METHOD_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateValidJwtTokenWithOnlyUserId() {
        UUID userId = UUID.randomUUID();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                null,
                null,
                null,
                userId
        );
        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);
        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(userId.toString(), claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateValidJwtTokenWithTransactionIdAndUserId() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        UUID userId = UUID.randomUUID();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                null,
                null,
                userId
        );
        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);
        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(userId.toString(), claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertNull(claims.get(JwtTokenUtils.PAYMENT_METHOD_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateValidJwtTokenWithWithAllClaims() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        String paymentMethodId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                orderId,
                paymentMethodId,
                userId
        );
        Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);

        assertTrue(generatedToken.isRight());
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken.get())
                        .getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(orderId, claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertEquals(paymentMethodId, claims.get(JwtTokenUtils.PAYMENT_METHOD_ID_CLAIM, String.class));
        assertEquals(userId.toString(), claims.get(JwtTokenUtils.USER_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }

    @Test
    void shouldGenerateExceptionJWTTokenGenerationException() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        String paymentMethodId = UUID.randomUUID().toString();
        UUID userId = UUID.randomUUID();
        it.pagopa.ecommerce.commons.domain.Claims jwtClaims = new it.pagopa.ecommerce.commons.domain.Claims(
                transactionId,
                orderId,
                paymentMethodId,
                userId
        );
        try (MockedStatic<Jwts> mockedJwts = Mockito.mockStatic(Jwts.class)) {
            JwtBuilder jwtBuilder = Mockito.mock(JwtBuilder.class);
            mockedJwts.when(Jwts::builder).thenReturn(jwtBuilder);
            given(jwtBuilder.setId(any())).willReturn(jwtBuilder);
            given(jwtBuilder.setIssuedAt(any())).willReturn(jwtBuilder);
            given(jwtBuilder.setExpiration(any())).willReturn(jwtBuilder);
            given(jwtBuilder.signWith(any())).willReturn(jwtBuilder);
            given(jwtBuilder.claim(any(), any())).willReturn(jwtBuilder);
            doThrow(new JwtException("Exception")).when(jwtBuilder).compact();

            Either<JWTTokenGenerationException, String> generatedToken = jwtTokenUtils
                    .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, jwtClaims);
            assertTrue(generatedToken.isLeft());

            assertEquals(
                    JWTTokenGenerationException.class,
                    generatedToken.getLeft().getClass()
            );
        }
    }
}
