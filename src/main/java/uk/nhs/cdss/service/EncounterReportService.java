package uk.nhs.cdss.service;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.dstu3.model.Bundle.BundleEntrySearchComponent;
import org.hl7.fhir.dstu3.model.Bundle.SearchEntryMode;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.Composition;
import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterLocationComponent;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.ListResource;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.ReferralRequest.ReferralRequestRequesterComponent;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EncounterReportService {

  private final ResourceService resourceService;
  private final ReferenceService referenceService;
  private final GenericResourceLocator resourceLocator;

  public String addEncounter(Bundle bundle, long encounterId) {
    Encounter encounter = (Encounter) resourceService.getResource(
        encounterId,
        null, //We can never specify version in a search with ID, find most recent by default.
        Encounter.class);

    String ref = referenceService
        .buildUrl(ResourceType.Encounter, encounter.getIdElement().toVersionless());
    bundle.addEntry(new BundleEntryComponent()
        .setFullUrl(ref)
        .setResource(encounter));

    addReferencedResource(bundle).accept(encounter.getSubject()); //Add patient
    encounter.getEpisodeOfCare()
        .forEach(addReferencedResource(bundle)); //Add episode of care
    encounter.getLocation().stream()
        .map(EncounterLocationComponent::getLocation)
        .forEach(addReferencedResource(bundle)); //Add locations
    encounter.getParticipant().stream()
        .map(EncounterParticipantComponent::getIndividual)
        .forEach(addReferencedResource(bundle)); //Add participants (Practitioner/RelatedPerson)
    addReferencedResource(bundle).accept(encounter.getServiceProvider()); //Add service provider

    return ref;
  }

  public void addReferralRequests(Bundle bundle, String encounterId) {
    List<ReferralRequest> referralRequests = resourceService.get(ReferralRequest.class)
        .by(asList(
            ReferralRequest::hasContext,
            rr -> rr.getContext().getReference().equals(encounterId)
        ));

    referralRequests.forEach(addResource(bundle));

    // Recursive include ReferralRequest.requester.agent
    referralRequests.stream()
        .map(ReferralRequest::getRequester)
        .map(ReferralRequestRequesterComponent::getAgent)
        .forEach(addReferencedResource(bundle));
  }

  public void addCarePlans(Bundle bundle, String encounterId) {
    resourceService.get(CarePlan.class)
        .by(asList(
            CarePlan::hasContext,
            cp -> cp.getContext().getReference().equals(encounterId)
        ))
        .forEach(addResource(bundle));
  }

  public void addCompositions(Bundle bundle, String encounterId) {
    List<Composition> compositions = resourceService.get(Composition.class)
        .by(asList(
            Composition::hasEncounter,
            comp -> comp.getEncounter().getReference().equals(encounterId)
        ));

    compositions.forEach(addResource(bundle));

    // Recursive include ReferralRequest.requester.agent
    compositions.stream()
        .map(Composition::addAuthor)
        .forEach(addReferencedResource(bundle));
  }

  public void addLists(Bundle bundle, String encounterId) {
    resourceService.get(ListResource.class)
        .by(asList(
            ListResource::hasEncounter,
            list -> list.getEncounter().getReference().equals(encounterId)
        ))
        .forEach(addResource(bundle));
  }

  public void addQuestionnaireResponses(Bundle bundle, String encounterId) {
    resourceService.get(QuestionnaireResponse.class)
        .by(asList(
            QuestionnaireResponse::hasContext,
            list -> list.getContext().getReference().equals(encounterId)
        ))
        .forEach(addResource(bundle));
  }

  private Consumer<IBaseResource> addResource(Bundle bundle) {
    return res -> {
      Resource resource = (Resource) res;
      bundle.addEntry(new BundleEntryComponent()
          .setSearch(new BundleEntrySearchComponent()
              .setMode(SearchEntryMode.INCLUDE))
          .setFullUrl(
              referenceService.buildUrl(resource.getResourceType(), resource.getIdElement()))
          .setResource(resource));
    };
  }

  private Consumer<Reference> addReferencedResource(Bundle bundle) {
    return ref -> resourceLocator.findResource(ref)
        .ifPresent(addResource(bundle));
  }

}
