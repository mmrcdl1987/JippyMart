package com.jippy.mart.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ExternalUserDbConfig {

// ================= PRIMARY DATASOURCE (Order DB) =================

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Primary
    @Bean(name = "jdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(@Qualifier("primaryDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // ================= SECONDARY DATASOURCE (External User DB) =================

    @Bean
    @ConfigurationProperties("spring.datasource.external-user")
    public DataSourceProperties externalUserDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource externalUserDataSource() {
        return externalUserDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "externalUserJdbcTemplate")
    public JdbcTemplate externalUserJdbcTemplate(@Qualifier("externalUserDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
