package uk.nhs.cdss.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "resource_id")
public class ResourceId {
  public static final Long INITIAL_VALUE = 0L;

  @Id
  @GeneratedValue
  private Long id;
  private long value;

  public ResourceId() {
    this.value = INITIAL_VALUE;
  }
}
