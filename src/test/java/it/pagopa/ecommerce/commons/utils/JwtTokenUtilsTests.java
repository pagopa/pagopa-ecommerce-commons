package it.pagopa.ecommerce.commons.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenUtilsTests {
    private static final String STRONG_KEY = "ODMzNUZBNTZENDg3NTYyREUyNDhGNDdCRUZDNzI3NDMzMzQwNTFEREZGQ0MyQzA5Mjc1RjY2NTQ1NDk5MDMxNzU5NDc0NUVFMTdDMDhGNzk4Q0Q3RENFMEJBODE1NURDREExNEY2Mzk4QzFEMTU0NTExNjUyMEExMzMwMTdDMDk";

    private static final int TOKEN_VALIDITY_TIME_SECONDS = 900;
    private final SecretKey jwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(STRONG_KEY));
    private final JwtTokenUtils jwtTokenUtils = new JwtTokenUtils();

    @Test
    void shouldGenerateValidJwtTokenWithOrderIdAndTransactionId() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        String generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, transactionId, orderId, null).block();
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken).getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(orderId, claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
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
        String generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, transactionId, null, null).block();
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken).getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
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
    void shouldGenerateValidJwtTokenWithAllClaims() {
        TransactionId transactionId = new TransactionId(UUID.randomUUID());
        String orderId = UUID.randomUUID().toString();
        String paymentMethodId = UUID.randomUUID().toString();
        String generatedToken = jwtTokenUtils
                .generateToken(jwtSecretKey, TOKEN_VALIDITY_TIME_SECONDS, transactionId, orderId, paymentMethodId)
                .block();
        assertNotNull(generatedToken);
        Claims claims = assertDoesNotThrow(
                () -> Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(generatedToken).getBody()
        );
        assertEquals(transactionId.value(), claims.get(JwtTokenUtils.TRANSACTION_ID_CLAIM, String.class));
        assertEquals(orderId, claims.get(JwtTokenUtils.ORDER_ID_CLAIM, String.class));
        assertEquals(paymentMethodId, claims.get(JwtTokenUtils.PAYMENT_METHOD_ID_CLAIM, String.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertEquals(
                Duration.ofSeconds(TOKEN_VALIDITY_TIME_SECONDS).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime()
        );
    }
}
