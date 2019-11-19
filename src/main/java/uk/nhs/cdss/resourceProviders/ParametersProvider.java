package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Parameters;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.ResourceService;

@Component
@AllArgsConstructor
public class ParametersProvider implements IResourceProvider{

	private ResourceService resourceService;

	@Override
	public Class<Parameters> getResourceType() {
		return Parameters.class;
	}

	@Read
	public Parameters getParametersById(@IdParam IdType id) {
		return resourceService.getResource(id.getIdPartAsLong(), getResourceType());
	}

	@Create
	public MethodOutcome createParameters(@ResourceParam Parameters parameters) {
		return new MethodOutcome(new IdType(resourceService.save(parameters).getId()), true);
	}
}
