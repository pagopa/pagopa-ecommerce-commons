package it.pagopa.ecommerce.commons.repositories;

import it.pagopa.ecommerce.commons.domain.RptId;
import org.springframework.data.repository.CrudRepository;

public interface PaymentRequestsInfoRepository extends CrudRepository<PaymentRequestInfo, RptId> {}
