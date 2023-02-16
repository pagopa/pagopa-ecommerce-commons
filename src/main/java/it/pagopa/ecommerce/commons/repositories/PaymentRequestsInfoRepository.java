package it.pagopa.ecommerce.commons.repositories;

import it.pagopa.ecommerce.commons.domain.v1.RptId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring repository for
 * {@link it.pagopa.ecommerce.commons.repositories.PaymentRequestInfo}.
 */
@Repository
public interface PaymentRequestsInfoRepository extends CrudRepository<PaymentRequestInfo, RptId> {
}
