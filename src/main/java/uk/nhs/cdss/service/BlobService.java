package uk.nhs.cdss.service;

import java.util.Optional;
import javax.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.nhs.cdss.entities.BlobEntity;
import uk.nhs.cdss.repos.BlobRepository;

@Service
@AllArgsConstructor
public class BlobService {

	private BlobRepository blobRepository;

	@Transactional
	public Optional<BlobEntity> getResource(String id) {
		return blobRepository.findById(id);
	}

	@Transactional
	public BlobEntity save(byte[] data, String contentType) {

		byte[] digest = DigestUtils.sha1(data);

		BlobEntity entity = BlobEntity.builder()
				.id(Base64.encodeBase64URLSafeString(digest))
				.contentType(contentType)
				.resourceData(data)
				.build();

		return blobRepository.save(entity);
	}

}
