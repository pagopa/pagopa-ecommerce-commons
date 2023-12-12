package it.pagopa.ecommerce.commons.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import it.pagopa.ecommerce.commons.domain.Claims;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import javax.crypto.SecretKey;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

/**
 * This class generate jwt token
 */
@Component
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
     * This method generates a jwt with specific claim
     *
     * @param jwtSecretKey             jwt secret key
     * @param tokenValidityTimeSeconds jwt validity time
     * @param claims                   value object with all claims to set into jwt
     * @return Mono jwt with specific claim
     */
    public Mono<String> generateToken(
                                      @NotNull SecretKey jwtSecretKey,
                                      @NotNull int tokenValidityTimeSeconds,
                                      @NotNull Claims claims
    ) {
        try {
            Calendar calendar = Calendar.getInstance();
            Date issuedAtDate = calendar.getTime();
            calendar.add(Calendar.SECOND, tokenValidityTimeSeconds);
            Date expiryDate = calendar.getTime();

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
            return Mono.just(jwtBuilder.compact());
        } catch (JwtException e) {
            log.error("Error generating JWT token", e);
            return Mono.error(new JWTTokenGenerationException());
        }

    }

}
