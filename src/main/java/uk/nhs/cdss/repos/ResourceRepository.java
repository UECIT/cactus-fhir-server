package uk.nhs.cdss.repos;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.ResourceEntity;
import uk.nhs.cdss.entities.ResourceEntity.IdVersion;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, IdVersion>{
   Optional<ResourceEntity> findFirstByIdVersion_IdOrderByIdVersion_VersionDesc(Long id);
}
