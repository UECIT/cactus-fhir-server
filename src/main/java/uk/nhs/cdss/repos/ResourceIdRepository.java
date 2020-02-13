package uk.nhs.cdss.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.nhs.cdss.entities.ResourceId;

@Repository
public interface ResourceIdRepository extends JpaRepository<ResourceId, Long>, ResourceIdIncrementer {

}
