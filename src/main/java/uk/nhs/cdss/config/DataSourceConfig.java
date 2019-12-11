package uk.nhs.cdss.config;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
public class DataSourceConfig {

  @Value("${datasource.showSql:false}")
  private boolean showSql;

  @Bean
  public EntityManagerFactory entityManagerFactory(DataSource dataSource) {
    final Database database = Database.valueOf("MYSQL");

    final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    vendorAdapter.setShowSql(showSql);
    vendorAdapter.setGenerateDdl(true);
    vendorAdapter.setDatabase(database);

    final LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
    factory.setJpaVendorAdapter(vendorAdapter);
    factory.setPackagesToScan("uk.nhs.cdss");
    factory.setDataSource(dataSource);
    factory.afterPropertiesSet();

    return factory.getObject();
  }

}
