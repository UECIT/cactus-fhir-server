package uk.nhs.cdss.entities;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.Index;
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
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resource_versioned")
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

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Embeddable
	public static class IdVersion implements Serializable {
		private Long id;
		private Long version;
	}

}
