package com.ford.vdcc.poc.batch.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class SqlDaoConfig {

    @Value("${sql.datasource.driver}")
    private String userdrivername;

    @Value("${sql.datasource.username}")
    private String username;

    @Value("${sql.datasource.password}")
    private String password;

    @Value("${sql.datasource.url}")
    private String url;

    @Value("${spring.datasource.hikari.connection-timeout}")
    private int connTimeOut;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minIdle;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.idle-timeout}")
    private int idleTimeOut;

    @Value("${spring.datasource.hikari.max-lifetime}")
    private int maxLifeTime;

    public DataSource getSqlDataSource()
    {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(userdrivername);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaxLifetime(maxLifeTime);
        config.setIdleTimeout(idleTimeOut);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connTimeOut);

        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    @Bean(name = "jdbcSqlTemplate")
    public JdbcTemplate getSqlJDBCTemplate() throws IOException {
        return new JdbcTemplate(getSqlDataSource());
    }



}
