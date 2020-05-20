package uk.nhs.cdss.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Objects;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.cdss.fixtures.PatientFixtures;

@RunWith(MockitoJUnitRunner.class)
public class GenericResourceLocatorTest {

  @InjectMocks
  GenericResourceLocator resourceLocator;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  FhirContext fhirContext;

  @Mock
  ResourceLookupService resourceLookupService;

  @Test
  public void localTypedReference() {
    Patient patient = PatientFixtures.patient();
    when(resourceLookupService.getResource(1L, null, Patient.class))
        .thenReturn(patient);

    Optional<Patient> resource = resourceLocator
        .findResource(new Reference("Patient/1"), Patient.class);

    assertTrue(resource.isPresent());
    assertEquals(patient, resource.get());
  }

  @Test(expected = ResourceNotFoundException.class)
  public void localTypedReferenceNotFound() {
    when(resourceLookupService.getResource(1L, null, Patient.class))
        .thenThrow(ResourceNotFoundException.class);

    resourceLocator.findResource(new Reference("Patient/1"), Patient.class);
  }

  @Test
  @SuppressWarnings({"unchecked","rawtypes"})
  public void localUntypedReference() {
    Patient patient = PatientFixtures.patient();
    when(Objects.requireNonNull(fhirContext
        .getElementDefinition("Patient"))
        .getImplementingClass())
        .thenReturn((Class) Patient.class);

    when(resourceLookupService.getResource(1L, null, Patient.class))
        .thenReturn(patient);

    var resource = resourceLocator.findResource(new Reference("Patient/1"));

    assertTrue(resource.isPresent());
    assertEquals(patient, resource.get());
  }

  @Test(expected = ResourceNotFoundException.class)
  @SuppressWarnings({"unchecked","rawtypes"})
  public void localUntypedReferenceNotFound() {
    Patient patient = PatientFixtures.patient();
    when(Objects.requireNonNull(fhirContext
        .getElementDefinition("Patient"))
        .getImplementingClass())
        .thenReturn((Class) Patient.class);

    when(resourceLookupService.getResource(1L, null, Patient.class))
        .thenThrow(ResourceNotFoundException.class);

    resourceLocator.findResource(new Reference("Patient/1"));
  }

}