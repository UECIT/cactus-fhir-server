package uk.nhs.cdss.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.ResourceEntity;

@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, Long>{
	
}
