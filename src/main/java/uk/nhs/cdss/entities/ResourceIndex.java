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
@NoArgsConstructor
@Table(name = "resource_index", indexes = {
    @Index(columnList = "supplierId,resource_type,path,value")
})
public class ResourceIndex extends SupplierPartitioned {

  @Id
  @GeneratedValue
  private Long id;

  private Long resourceId;

  @Enumerated
  @Column(name = "resource_type")
  private ResourceType resourceType;

  private String path;
  private String value;

  @Builder(toBuilder = true)
  public ResourceIndex(Long id, String supplierId, Long resourceId, ResourceType resourceType, String path, String value) {
    super(supplierId);
    this.id = id;
    this.resourceId = resourceId;
    this.resourceType = resourceType;
    this.path = path;
    this.value = value;
  }
}
