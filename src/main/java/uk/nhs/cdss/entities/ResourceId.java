package uk.nhs.cdss.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "resource_id")
public class ResourceId {

  /**
   * A global namespace to apply IDs for any resource type
   */
  public static final long GLOBAL = 1;
  public static final long INITIAL_VALUE = 1;

  @Id
  private long id = GLOBAL;
  private long value = INITIAL_VALUE;

  public ResourceId(long id) {
    this(id, INITIAL_VALUE);
  }

  public ResourceId(long id, long value) {
    this.id = id;
    this.value = value;
  }
}
