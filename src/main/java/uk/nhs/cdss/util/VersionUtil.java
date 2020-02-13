package uk.nhs.cdss.util;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;
import org.hl7.fhir.instance.model.api.IBaseResource;

@UtilityClass
public class VersionUtil {

  public <T extends IBaseResource> List<T> collectLatest(Stream<T> resources) {
    return resources
        // Collect all grouped by ID
        .collect(Collectors.groupingBy(res -> res.getIdElement().getIdPart()))
        .values().stream()
        .map(list -> list.stream()
            // Get resource with the highest version for each ID
            .max(Comparator.comparingLong(res -> res.getIdElement().getVersionIdPartAsLong()))
            .orElseThrow())
        .collect(Collectors.toUnmodifiableList());
  }
}
