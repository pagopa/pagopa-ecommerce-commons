package it.pagopa.ecommerce.commons.utils;

import it.pagopa.ecommerce.commons.domain.Confidential;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;

public class ConfidentialDataManagerTest {
    public static ConfidentialDataManager getMock() {
        ConfidentialDataManager mockedConfidentialDataManager = Mockito.mock(ConfidentialDataManager.class);

        Mockito.when(mockedConfidentialDataManager.encrypt(any()))
                .thenAnswer(
                        invocation -> Mono.just(
                                new Confidential<>(
                                        ((ConfidentialDataManager.ConfidentialData) invocation.getArgument(0))
                                                .toStringRepresentation()
                                )
                        )
                );

        Mockito.when(mockedConfidentialDataManager.decrypt(any()))
                .thenAnswer(invocation -> Mono.just((((Confidential<?>) invocation.getArgument(0)).opaqueData())));

        Mockito.when(mockedConfidentialDataManager.decrypt(any(), any())).thenCallRealMethod();

        return mockedConfidentialDataManager;
    }
}
