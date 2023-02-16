package it.pagopa.ecommerce.commons.redis;

import it.pagopa.ecommerce.commons.domain.v1.RptId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Redis converter from {@link RptId} to a byte array.
 */
@Component
@WritingConverter
public class RptIdWritingByteConverter implements Converter<RptId, byte[]> {
    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] convert(RptId source) {
        return source.value().getBytes(StandardCharsets.UTF_8);
    }
}
