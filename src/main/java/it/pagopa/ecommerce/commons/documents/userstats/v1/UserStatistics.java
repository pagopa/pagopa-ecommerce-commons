package it.pagopa.ecommerce.commons.documents.userstats.v1;

import it.pagopa.ecommerce.commons.documents.userstats.v1.LastUsage;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

/**
 * Persistence document for user statistics.
 */
@Data
@Document
@AllArgsConstructor
@NoArgsConstructor
@Generated
public class UserStatistics {

    @Id
    @NotNull
    private String userId;

    @NotNull
    private LastUsage lastUsage;
}
