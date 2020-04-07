package uk.nhs.cdss.service;

import static java.util.Arrays.asList;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Appointment;
import org.hl7.fhir.dstu3.model.Reference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final ResourceService resourceService;

  public List<Appointment> getByReferrals(List<String> referralRequestRefs) {
    return resourceService.get(Appointment.class)
        .by(asList(
            Appointment::hasIncomingReferral,
            app -> app.getIncomingReferral().stream()
                .map(Reference::getReference)
                .anyMatch(referralRequestRefs::contains)));
  }

}
