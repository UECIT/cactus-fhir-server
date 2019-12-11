package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.ResourceType;

@Entity
@Table(name = "blob_resources")
@Getter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BlobEntity {

  @Id
  private String id;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "resource_data")
	@Lob()
  private byte[] resourceData;

}
