package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hl7.fhir.dstu3.model.ResourceType;

@Entity
@Table(name = "resource")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated
	@Column(name = "resource_type")
	private ResourceType resourceType;
	
	@Column(name = "resource_json")
	private String resourceJson;

	/**
	 * Virtual generated index column on the first to have a context.reference or encounter.reference property
	 * (For ReferralRequest and Composition types)
	 */
	@Generated(value = GenerationTime.ALWAYS)
	@Column(name = "encounter_id",
			columnDefinition = "VARCHAR(1000) AS (JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(resource_json, "
					+ "\"$.context.reference\", "
					+ "\"$.encounter.reference\"), "
					+ "\"$[0]\")) )")
	private String encounterId;

}
