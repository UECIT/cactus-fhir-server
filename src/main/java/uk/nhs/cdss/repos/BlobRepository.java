package uk.nhs.cdss.repos;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.BlobEntity;
import uk.nhs.cdss.entities.ResourceEntity;

@Repository
public interface BlobRepository extends JpaRepository<BlobEntity, String>{
	
}
