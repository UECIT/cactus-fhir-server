package uk.nhs.cdss.service.index;

public class IndexMappers {

  public static String mapCoding(String system, String value) {
    return String.format("%s|%s", system, value);
  }
}
