package uk.nhs.cdss.resourceProviders;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import lombok.AllArgsConstructor;
import org.hl7.fhir.dstu3.model.CarePlan;
import org.hl7.fhir.dstu3.model.IdType;
import org.springframework.stereotype.Component;
import uk.nhs.cdss.service.ResourceService;

@Component
@AllArgsConstructor
public class CarePlanProvider implements IResourceProvider {

  private ResourceService resourceService;

	@Override
	public Class<CarePlan> getResourceType() {
		return CarePlan.class;
	}

	@Read
	public CarePlan getCarePlanById(@IdParam IdType id) {
		return resourceService.getResource(id.getIdPartAsLong(), getResourceType());
	}

	@Create
	public MethodOutcome createCarePlan(@ResourceParam CarePlan carePlan) {
		return new MethodOutcome(new IdType(resourceService.save(carePlan).getId()), true);
	}
}
