package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.Data;

@Data
@MappedSuperclass
public class SupplierPartitioned {

  @Column(name = "supplierToken")
  private String supplierToken;

}
