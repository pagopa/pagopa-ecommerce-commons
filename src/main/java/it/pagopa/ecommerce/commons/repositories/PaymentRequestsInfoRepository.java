package it.pagopa.ecommerce.commons.repositories;

import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.data.repository.CrudRepository;

/**
 * Spring repository for {@link PaymentRequestInfo}.
 */
public interface PaymentRequestsInfoRepository extends CrudRepository<PaymentRequestInfo, RptId> {}
