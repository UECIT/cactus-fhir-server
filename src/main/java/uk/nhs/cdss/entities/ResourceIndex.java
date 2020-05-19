package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hl7.fhir.dstu3.model.ResourceType;

@Entity
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "resource_index", indexes = {
    @Index(columnList = "supplierId,type,path,value")
})
public class ResourceIndex {

  @Column(name="supplierId")
  private String supplierId;

  @Id
  @GeneratedValue
  private Long id;

  private Long resourceId;

  @Enumerated
  private ResourceType type;
  private String path;
  private String value;
}
