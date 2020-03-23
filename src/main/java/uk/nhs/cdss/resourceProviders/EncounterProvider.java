package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.EncounterReportService;

@Component
@RequiredArgsConstructor
public class EncounterProvider implements IResourceProvider {

  private final EncounterReportService encounterReportService;

  @Search
  public Bundle getEncounterReport(
      @RequiredParam(name = Encounter.SP_RES_ID) TokenParam encounterParam,
      @IncludeParam(reverse = true) Set<Include> revIncludes, //Ignored
      @IncludeParam Set<Include> include //Ignored
  ) {

    Bundle bundle = new Bundle();
    bundle.setType(BundleType.DOCUMENT);

    long encounterId = Long.parseLong(encounterParam.getValue());

    String encounterRef = encounterReportService.addEncounter(bundle, encounterId);
    encounterReportService.addReferralRequests(bundle, encounterRef);
    encounterReportService.addCompositions(bundle, encounterRef);
    encounterReportService.addLists(bundle, encounterRef);
    encounterReportService.addCarePlans(bundle, encounterRef);
    encounterReportService.addQuestionnaireResponses(bundle, encounterRef);

    return bundle;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Encounter.class;
  }
}
