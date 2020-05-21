package uk.nhs.cdss.repos;

import java.util.List;
import java.util.Optional;
import org.hl7.fhir.dstu3.model.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, IdVersion> {

  Optional<ResourceEntity> findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(Long id);

  List<ResourceEntity> findAllBySupplierIdAndResourceType(String supplierId, ResourceType resourceType);
}
