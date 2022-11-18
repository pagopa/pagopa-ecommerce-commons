package it.pagopa.ecommerce.commons.redis;

import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * Redis converter from a {@link String} to an {@link RptId}.
 */
@Component
@ReadingConverter
public class RptIdReadingStringConverter implements Converter<String, RptId> {
    @Override
    public RptId convert(@NonNull String source) {
        return new RptId(source);
    }
}
