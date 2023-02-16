package it.pagopa.ecommerce.commons.redis;

import it.pagopa.ecommerce.commons.domain.v1.RptId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis converter from a byte array to an {@link RptId}.
 */
@Component
@ReadingConverter
public class RptIdReadingByteConverter implements Converter<byte[], RptId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public RptId convert(@NonNull byte[] source) {
        return new RptId(new String(source, StandardCharsets.UTF_8));
    }
}
