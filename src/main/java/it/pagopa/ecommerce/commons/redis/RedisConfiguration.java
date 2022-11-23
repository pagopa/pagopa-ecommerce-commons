package it.pagopa.ecommerce.commons.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.convert.RedisCustomConversions;

import java.util.Arrays;

/**
 * <p>Redis configuration enabling automatic conversion from/to {@link it.pagopa.ecommerce.commons.domain.RptId}</p>
 */
@Configuration
public class RedisConfiguration {
    /**
     * Bean registering redis custom conversions
     * @param readingByteConverter byte[] -> RptId converter
     * @param writingByteConverter RptId -> byte[] converter
     * @param readingStringConverter String -> RptId converter
     * @param writingStringConverter RptId -> String converter
     * @return redis conversions bean configured for {@link it.pagopa.ecommerce.commons.domain.RptId}
     */
    @Bean
    public RedisCustomConversions redisCustomConversions(RptIdReadingByteConverter readingByteConverter,
                                                         RptIdWritingByteConverter writingByteConverter,
                                                         RptIdReadingStringConverter readingStringConverter,
                                                         RptIdWritingStringConverter writingStringConverter) {
        return new RedisCustomConversions(
                Arrays.asList(
                        readingByteConverter,
                        writingByteConverter,
                        readingStringConverter,
                        writingStringConverter
                )
        );
    }
}
