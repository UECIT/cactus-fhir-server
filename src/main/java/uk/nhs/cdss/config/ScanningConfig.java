package uk.nhs.cdss.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"uk.nhs.cactus.common"})
public class ScanningConfig {
}
