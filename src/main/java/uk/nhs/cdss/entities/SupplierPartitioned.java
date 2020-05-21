package uk.nhs.cdss.entities;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public abstract class SupplierPartitioned {

  @Column(name = "supplierId")
  protected String supplierId;

}