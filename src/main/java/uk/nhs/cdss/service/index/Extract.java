package uk.nhs.cdss.service.index;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Extract {

  String value() default "";
}
