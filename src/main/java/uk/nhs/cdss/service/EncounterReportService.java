package uk.nhs.cdss.service;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
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

  private final ResourceLookupService resourceLookupService;
  private final ResourceIndexService resourceIndexService;
  private final ReferenceService referenceService;
  private final AppointmentService appointmentService;
  private final GenericResourceLocator resourceLocator;

  public String addEncounter(Bundle bundle, long encounterId) {
    Encounter encounter = resourceLookupService.getResource(
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
    List<ReferralRequest> referralRequests = resourceIndexService.search(ReferralRequest.class)
        .eq(ReferralRequest.SP_CONTEXT, encounterId);

    referralRequests.forEach(addResource(bundle));

    // Recursive include ReferralRequest.requester.agent
    referralRequests.stream()
        .map(ReferralRequest::getRequester)
        .map(ReferralRequestRequesterComponent::getAgent)
        .forEach(addReferencedResource(bundle));

    // Recursive revinclude on Appointment.incomingReferral
    addAppointments(referralRequests, bundle);
  }

  private void addAppointments(List<ReferralRequest> referralRequests, Bundle bundle) {
    List<String> referralRequestReferences = referralRequests.stream()
        .map(rr -> referenceService
            .buildUrl(ResourceType.ReferralRequest, rr.getIdElement().toVersionless()))
        .collect(Collectors.toUnmodifiableList());

    appointmentService.getByReferrals(referralRequestReferences)
        .forEach(addResource(bundle));
  }

  public void addCarePlans(Bundle bundle, String encounterId) {
    resourceIndexService.search(CarePlan.class)
        .eq(CarePlan.SP_CONTEXT, encounterId)
        .forEach(addResource(bundle));
  }

  public void addCompositions(Bundle bundle, String encounterId) {
    resourceIndexService.search(Composition.class)
        .eq(Composition.SP_ENCOUNTER, encounterId)
        .forEach(addResource(bundle));
  }

  public void addLists(Bundle bundle, String encounterId) {
    resourceIndexService.search(ListResource.class)
        .eq(ListResource.SP_ENCOUNTER, encounterId)
        .forEach(addResource(bundle));
  }

  public void addQuestionnaireResponses(Bundle bundle, String encounterId) {
    List<QuestionnaireResponse> qrs = resourceIndexService.search(QuestionnaireResponse.class)
        .eq(QuestionnaireResponse.SP_CONTEXT, encounterId);

    qrs.forEach(addResource(bundle));

    // Recursive include Questionnaires
    qrs.stream()
        .map(QuestionnaireResponse::getQuestionnaire)
        .forEach(addReferencedResource(bundle));
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
