package uk.nhs.cdss.repos;

import java.math.BigInteger;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ResourceIdIncrementerImpl implements ResourceIdIncrementer {

  private EntityManager entityManager;

  @Override
  @Transactional
  public long incrementAndGet(Long id) {
    entityManager.createNativeQuery(
        "UPDATE resource_id "
            + "SET value = value + 1 "
            + "WHERE id = :id")
        .setParameter("id", id)
        .executeUpdate();

    var query = entityManager.createNativeQuery(
        "SELECT value FROM resource_id WHERE id = :id")
        .setParameter("id", id);

    return ((BigInteger) query.getSingleResult()).longValue();
  }
}
