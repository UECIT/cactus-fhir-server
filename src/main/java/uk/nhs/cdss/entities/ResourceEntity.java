package uk.nhs.cdss.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.ResourceType;

@Entity
@Table(name = "resource_versioned")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEntity {

	@EmbeddedId
	private IdVersion idVersion;
	
	@Enumerated
	@Column(name = "resource_type")
	private ResourceType resourceType;
	
	@Column(name = "resource_json")
	@Setter
	@Lob
	private String resourceJson;

//	/**
//	 * Virtual generated index column on the first to have a context.reference or encounter.reference property
//	 * (For ReferralRequest and Composition types)
//	 */
//	@Generated(value = GenerationTime.ALWAYS)
//	@Column(name = "encounter_id",
//			columnDefinition = "VARCHAR(1000) AS (JSON_UNQUOTE(JSON_EXTRACT(JSON_EXTRACT(resource_json, "
//					+ "\"$.context.reference\", "
//					+ "\"$.encounter.reference\"), "
//					+ "\"$[0]\")) )")
//	private String encounterId;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class IdVersion implements Serializable {
		private Long id;
		private Long version;
	}

}
