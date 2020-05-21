package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "blob_resources")
@Data
@NoArgsConstructor
public class BlobEntity extends SupplierPartitioned {

  @Id
  private String id;

  @Column(name = "content_type")
  private String contentType;

  @Column(name = "resource_data")
  @Lob()
  private byte[] resourceData;

  @Builder(toBuilder = true)
  public BlobEntity(String id, String supplierId, String contentType, byte[] resourceData) {
    super(supplierId);
    this.id = id;
    this.contentType = contentType;
    this.resourceData = resourceData;
  }
}
