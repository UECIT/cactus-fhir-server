package uk.nhs.cdss.repos;

public interface ResourceIdIncrementer {
  long incrementAndGet(Long id);
}
