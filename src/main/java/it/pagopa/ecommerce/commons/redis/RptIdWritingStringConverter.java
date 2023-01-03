package it.pagopa.ecommerce.commons.redis;

import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

/**
 * Redis converter from {@link it.pagopa.ecommerce.commons.domain.RptId} to a
 * {@link java.lang.String}.
 *
 */
@Component
@WritingConverter
public class RptIdWritingStringConverter implements Converter<RptId, String> {
    /** {@inheritDoc} */
    @Override
    public String convert(RptId source) {
        return source.value();
    }
}
