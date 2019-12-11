package uk.nhs.cdss.controllers;

import java.net.URI;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.cdss.entities.BlobEntity;
import uk.nhs.cdss.service.BlobService;

@AllArgsConstructor
@RestController
@RequestMapping("/blob")
public class BlobController {

  private final BlobService blobService;

  private String formatDigest(BlobEntity blob) {
    return String.format("SHA=%s",
        Base64.encodeBase64URLSafeString(DigestUtils.sha1(blob.getResourceData())));
  }

  @RequestMapping(path = "{id}", method = RequestMethod.HEAD)
  public ResponseEntity<String> head(@PathVariable String id) {
    return blobService.getResource(id)
        .map(blob -> ResponseEntity.ok()
            .contentType(MediaType.valueOf(blob.getContentType()))
            .header("Digest", formatDigest(blob))
            .contentLength(blob.getResourceData().length)
            .body(""))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping(path = "{id}")
  public ResponseEntity<byte[]> get(@PathVariable String id) {
    return blobService.getResource(id)
        .map(blob -> ResponseEntity.ok()
            .contentType(MediaType.valueOf(blob.getContentType()))
            .header("Digest", formatDigest(blob))
            .contentLength(blob.getResourceData().length)
            .body(blob.getResourceData()))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PutMapping
  public ResponseEntity<String> put(@RequestBody byte[] data,
      @RequestHeader(HttpHeaders.CONTENT_TYPE) MediaType contentType) {
    BlobEntity blob = blobService.save(data, contentType.toString());
    return ResponseEntity.created(URI.create("/blob/" + blob.getId()))
        .contentType(MediaType.valueOf(blob.getContentType()))
        .header("Digest", formatDigest(blob))
        .contentLength(0)
        .build();
  }
}
