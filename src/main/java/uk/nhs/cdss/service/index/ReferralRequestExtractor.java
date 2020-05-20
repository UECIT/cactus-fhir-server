package uk.nhs.cdss.service.index;

import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class ReferralRequestExtractor extends AbstractExtractor<ReferralRequest> {

  @Extract
  public Reference context(ReferralRequest referralRequest) {
    return referralRequest.getContext();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.ReferralRequest;
  }
}
