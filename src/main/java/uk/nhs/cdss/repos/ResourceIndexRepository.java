package uk.nhs.cdss.repos;

import java.util.List;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.ResourceIndex;

@Repository
public interface ResourceIndexRepository extends JpaRepository<ResourceIndex, Long> {

  List<ResourceIndex> findAllBySupplierIdAndResourceTypeAndPathAndValue(
      String supplierId, ResourceType type, String path, String value);

  void deleteAllByResourceId(long resourceId);
}
