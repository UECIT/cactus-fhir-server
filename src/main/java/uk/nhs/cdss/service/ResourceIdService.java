package uk.nhs.cdss.service;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceId;
import uk.nhs.cdss.repos.ResourceIdRepository;

@AllArgsConstructor
@Service
public class ResourceIdService {

  private final ResourceIdRepository resourceIdRepository;

  public Long nextId() {
    if (!resourceIdRepository.existsById(1L)) {
      resourceIdRepository.save(new ResourceId());
      return ResourceId.INITIAL_VALUE;
    }

    return resourceIdRepository.incrementAndGet(1L);
  }
}
