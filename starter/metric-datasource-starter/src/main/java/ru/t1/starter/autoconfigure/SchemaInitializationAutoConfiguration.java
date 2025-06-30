package ru.t1.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@AutoConfiguration
public class SchemaInitializationAutoConfiguration {

    @Bean
    public DataSourceInitializer schemaDataSourceInitializer(DataSource dataSource) {

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));

        DataSourceInitializer init = new DataSourceInitializer();
        init.setDataSource(dataSource);
        init.setDatabasePopulator(populator);

        init.setEnabled(true);
        return init;
    }
}
