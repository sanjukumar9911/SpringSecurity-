package com.ford.vdcc.poc.batch.dao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class SqlinmemoryConfig {

    @Bean
    @Primary
    public DataSource getSqlDataSource()
    {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.h2.Driver");
        dataSourceBuilder.url("jdbc:h2:mem:testdb;DB_CLOSE_ON_EXIT=FALSE");
        dataSourceBuilder.username("ROOT");
        dataSourceBuilder.password("root");
        return dataSourceBuilder.build();
    }

    @Bean(name = "jdbcH2Template")
    public JdbcTemplate getSqlJDBCTemplate() throws IOException {
        return new JdbcTemplate(getSqlDataSource());
    }


}
