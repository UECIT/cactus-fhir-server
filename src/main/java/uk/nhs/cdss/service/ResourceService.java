package uk.nhs.cdss.service;

import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.function.Function;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.repos.ResourceRepository;

@Service
@AllArgsConstructor
public class ResourceService {

	private ResourceRepository resourceRepository;
	private IParser fhirParser;
	
	@Transactional
	public IBaseResource getResource(Long id, Class<? extends IBaseResource> clazz) {

		ResourceEntity resource = resourceRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(new IdDt(id)));

		final IBaseResource dto = clazz.cast(fhirParser.parseResource(resource.getResourceJson()));
		dto.setId(String.valueOf(id));

		return dto;
	}

	@Transactional
	public ResourceEntity save(Resource resource) {
		return resourceRepository.save(toEntity(resource));
	}

	@Transactional
	public ResourceEntity update(Long id, Resource resource) {
		var updated = resourceRepository.findById(id)
				.map(withJson(resource))
				.orElse(toEntity(resource));

		return resourceRepository.save(updated);
	}

	private ResourceEntity toEntity(Resource resource) {
		return ResourceEntity.builder()
				.resourceType(resource.getResourceType())
				.resourceJson(fhirParser.encodeResourceToString(resource))
				.build();
	}

	private Function<ResourceEntity, ResourceEntity> withJson(Resource resource) {
		return entity -> entity.toBuilder()
				.resourceJson(fhirParser.encodeResourceToString(resource))
				.build();
	}

}
