package it.pagopa.ecommerce.commons.redis;

import it.pagopa.ecommerce.commons.domain.v1.RptId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Redis converter from a {@link java.lang.String} to an {@link RptId}.
 */
@Component
@ReadingConverter
public class RptIdReadingStringConverter implements Converter<String, RptId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public RptId convert(@NonNull String source) {
        return new RptId(source);
    }
}
