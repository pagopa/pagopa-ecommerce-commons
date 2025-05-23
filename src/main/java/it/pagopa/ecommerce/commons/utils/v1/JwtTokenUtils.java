package it.pagopa.ecommerce.commons.utils.v1;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.vavr.control.Either;
import it.pagopa.ecommerce.commons.domain.v1.Claims;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Utility class used to generate JWT tokens with custom claims.
 *
 * @see Claims
 */
@Component("jwtTokenUtilsV1")
@Slf4j
public class JwtTokenUtils {

    /**
     * The claim transactionId
     */
    public static final String TRANSACTION_ID_CLAIM = "transactionId";

    /**
     * The claim orderId
     */
    public static final String ORDER_ID_CLAIM = "orderId";

    /**
     * The claim payment methodId
     */
    public static final String PAYMENT_METHOD_ID_CLAIM = "paymentMethodId";
    /**
     * The claim userId
     */
    public static final String USER_ID_CLAIM = "userId";

    /**
     * This method generates a jwt with specific claim
     *
     * @param jwtSecretKey             jwt secret key
     * @param tokenValidityTimeSeconds jwt validity time
     * @param claims                   value object with all claims to set into jwt
     * @return Mono jwt with specific claim
     */
    public Either<JWTTokenGenerationException, String> generateToken(
                                                                     @NotNull SecretKey jwtSecretKey,
                                                                     @NotNull int tokenValidityTimeSeconds,
                                                                     @NotNull Claims claims
    ) {
        try {
            Instant now = Instant.now();
            Date issuedAtDate = Date.from(now);
            Date expiryDate = Date.from(now.plus(Duration.ofSeconds(tokenValidityTimeSeconds)));

            JwtBuilder jwtBuilder = Jwts.builder()
                    .setId(UUID.randomUUID().toString())// jti
                    .setIssuedAt(issuedAtDate)// iat
                    .setExpiration(expiryDate)// exp
                    .signWith(jwtSecretKey);

            if (claims.transactionId() != null) {
                jwtBuilder.claim(TRANSACTION_ID_CLAIM, claims.transactionId().value());// claim TransactionId
            }
            if (claims.orderId() != null) {
                jwtBuilder.claim(ORDER_ID_CLAIM, claims.orderId()); // claim orderId
            }
            if (claims.paymentMethodId() != null) {
                jwtBuilder.claim(PAYMENT_METHOD_ID_CLAIM, claims.paymentMethodId()); // claim paymentMethodId
            }
            if (claims.userId() != null) {
                jwtBuilder.claim(USER_ID_CLAIM, claims.userId().toString()); // claim userId
            }
            return Either.right(jwtBuilder.compact());
        } catch (JwtException e) {
            log.error("Error generating JWT token", e);
            return Either.left(new JWTTokenGenerationException());
        }
    }
}
