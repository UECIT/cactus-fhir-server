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
public class SupplierPartitioned {

  @Column(name = "supplierId")
  String supplierId;

}