package it.pagopa.ecommerce.commons.utils;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import it.pagopa.ecommerce.commons.domain.TransactionId;
import it.pagopa.ecommerce.commons.exceptions.JWTTokenGenerationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
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
     * @param transactionId            the optional transactionId to set into jwt
     *                                 claim
     * @param orderId                  the optional orderId to set into jwt claim
     * @param paymentMethodId          the optional paymentMethodId to set into jwt
     *                                 claim
     * @return Mono jwt with specific claim
     */
    public Mono<String> generateToken(
                                      @NotNull SecretKey jwtSecretKey,
                                      @NotNull int tokenValidityTimeSeconds,
                                      @Nullable TransactionId transactionId,
                                      @Nullable String orderId,
                                      @Nullable String paymentMethodId
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

            if (transactionId != null) {
                jwtBuilder.claim(TRANSACTION_ID_CLAIM, transactionId.value());// claim TransactionId
            }
            if (orderId != null) {
                jwtBuilder.claim(ORDER_ID_CLAIM, orderId); // claim orderId
            }
            if (paymentMethodId != null) {
                jwtBuilder.claim(PAYMENT_METHOD_ID_CLAIM, paymentMethodId); // claim paymentMethodId
            }
            return Mono.just(jwtBuilder.compact());
        } catch (JwtException e) {
            log.error("Error generating JWT token", e);
            return Mono.error(new JWTTokenGenerationException());
        }

    }

}
