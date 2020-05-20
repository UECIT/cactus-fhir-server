package uk.nhs.cdss.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.dstu3.model.Appointment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentService {

  private final ResourceLookupService resourceLookupService;
  private final ResourceIndexService resourceIndexService;

  public List<Appointment> getByReferrals(List<String> referralRequestRefs) {
    return referralRequestRefs.stream()
        .flatMap(ref -> resourceIndexService.search(Appointment.class)
            .eq(Appointment.SP_INCOMINGREFERRAL, ref)
            .stream())
        .collect(Collectors.toList());
  }

}
