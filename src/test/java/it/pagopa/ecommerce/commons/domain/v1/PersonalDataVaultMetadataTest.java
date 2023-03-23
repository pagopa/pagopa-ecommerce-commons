package it.pagopa.ecommerce.commons.domain.v1;

import it.pagopa.ecommerce.commons.domain.PersonalDataVaultMetadata;
import it.pagopa.ecommerce.commons.utils.ConfidentialDataManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class PersonalDataVaultMetadataTest {
    @Test
    void modeIsCorrect() {
        PersonalDataVaultMetadata personalDataVaultMetadata = new PersonalDataVaultMetadata();

        assertEquals(ConfidentialDataManager.Mode.PERSONAL_DATA_VAULT, personalDataVaultMetadata.getMode());
    }
}
