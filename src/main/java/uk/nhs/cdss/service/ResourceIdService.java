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
    if (!resourceIdRepository.existsById(ResourceId.GLOBAL)) {
      ResourceId id = resourceIdRepository.save(new ResourceId(ResourceId.GLOBAL));
      return id.getValue();
    }

    return resourceIdRepository.incrementAndGet(ResourceId.GLOBAL);
  }
}
