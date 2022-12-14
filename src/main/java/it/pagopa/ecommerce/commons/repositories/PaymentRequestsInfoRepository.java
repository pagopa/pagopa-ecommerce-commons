package it.pagopa.ecommerce.commons.repositories;

import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Spring repository for {@link PaymentRequestInfo}. */
@Repository
public interface PaymentRequestsInfoRepository extends CrudRepository<PaymentRequestInfo, RptId> {
}
