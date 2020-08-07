package uk.nhs.cdss.service.index;

import java.util.List;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.stereotype.Component;

@Component
public class AppointmentExtractor extends AbstractExtractor<Appointment> {

  @Extract(Appointment.SP_INCOMINGREFERRAL)
  public List<Reference> incomingReferrals(Appointment appointment) {
    return appointment.getIncomingReferral();
  }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.Appointment;
  }
}
