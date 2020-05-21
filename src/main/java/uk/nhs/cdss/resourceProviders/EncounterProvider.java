package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.IncludeParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleType;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.EncounterReportService;
import uk.nhs.cdss.service.ResourceIndexService;
import uk.nhs.cdss.service.ResourceService;
import uk.nhs.cdss.service.index.EncounterExtractor;
import uk.nhs.cdss.service.index.IndexMappers;

@Component
@RequiredArgsConstructor
@Slf4j
public class EncounterProvider implements IResourceProvider {

  private final EncounterReportService encounterReportService;
  private final ResourceService resourceService;
  private final ResourceIndexService resourceIndexService;
  private final FhirContext context;

  @Search
  public List<Encounter> searchByPatient(
      @RequiredParam(name = Encounter.SP_PATIENT, chainWhitelist = Patient.SP_IDENTIFIER) ReferenceParam param) {

    TokenParam identifierParam = param.toTokenParam(context);

    return resourceIndexService.search(Encounter.class)
        .eq(EncounterExtractor.PATIENT_IDENTIFIER,
            IndexMappers.mapCoding(identifierParam.getSystem(), identifierParam.getValue()));
  }

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
