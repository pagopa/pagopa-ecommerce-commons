package it.pagopa.ecommerce.commons.utils;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.MatchingCriteria;
import it.pagopa.ecommerce.commons.utils.bean.redirect.configuration.RedirectUrlMappingEntry;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class RedirectUrlMappingConf {

    private final List<RedirectUrlMappingEntry> urlConfiguration;

    private static final ObjectMapper objectMapper;

    static {
        //configure object mapper for deserialization
        objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_NULL_CREATOR_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true)
                .configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    public RedirectUrlMappingConf(String urlConfigurationJsonValue, String expectedMatchingCriteriaJsonValue) {
        List<MatchingCriteria> expectedMatchingCriteria;
        try {
            urlConfiguration = objectMapper.readValue(urlConfigurationJsonValue, new TypeReference<>() {
            });
            expectedMatchingCriteria = objectMapper.readValue(expectedMatchingCriteriaJsonValue, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Invalid redirect url configuration: error parsing json values", e);
        }

    }


    public Optional<RedirectUrlMappingEntry> getUrlForCriteria(MatchingCriteria matchingCriteria) {
        return this.urlConfiguration.stream()
                .filter(entry -> entry.matchingCriteria().paymentTypeCode().equals(matchingCriteria.paymentTypeCode()));


    }

    private Predicate<String> matchCriteriaPredicate(MatchingCriteria matchingCriteria) {

    }
}
