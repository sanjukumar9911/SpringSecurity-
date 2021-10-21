package com.ford.vdcc.poc.batch.dao;

import com.ford.vdcc.poc.batch.util.QueryConstants;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.hadoop.security.UserGroupInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;

@Configuration
public class HiveDaoConfig {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${hive.connectionURL}")
    private String hiveConnectionURL;


    @Bean("hiveDataBase")
    public DataSource getHiveDataSource() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();

        File krb5File = new File(classLoader.getResource("krb5.conf").getFile());
        File keytabFile = new File(classLoader.getResource("prxcvhcl.keytab").getFile());

        if (!krb5File.exists() || !keytabFile.exists()) {
            try {
                throw new IOException("krb5 and keytab files do not exists");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("KRFFile Path ::"+krb5File.getAbsolutePath());
        System.out.println("KeyTab Path ::"+keytabFile.getAbsolutePath());

        System.setProperty("hadoop.home.dir", "C:/Users/sbellur/IdeaProjects/ford/vdcc-utilities1/vdcc-scheduler-tool/src/main/resources/scheduler-template/src/main/resources");
        System.setProperty("java.security.krb5.conf", krb5File.getAbsolutePath());
        System.setProperty("javax.security.auth.useSubjectCredsOnly", "false");

        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();
        try {
            conf.set("hadoop.security.authentication", "Kerberos");
            conf.set("hive.metastore.client.socket.timeout", "3800");
            conf.set("hive.server2.session.check.interval", "900000");
            conf.set("hive.server2.idle.operation.timeout", "3800000");
            conf.set("hive.server2.idle.session.timeout", "3200000");
            conf.set("hive.server2.idle.session.check.operation", "true");
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab("prxcvhcl@HPC.FORD.COM", keytabFile.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("Hive Login Exception", e);
            try {
                throw e;
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
       // BasicDataSource dataSource = new BasicDataSource();
        dataSource.setUrl(this.hiveConnectionURL);
        dataSource.setDriverClassName("org.apache.hive.jdbc.HiveDriver");

        return dataSource;
    }

    @Bean(name = "jdbcHiveTemplate")
    public JdbcTemplate getJDBCTemplate() throws IOException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(getHiveDataSource());
        for (String s : QueryConstants.SETTING_QUERIES) {
            LOGGER.info("Settings " + s);
            jdbcTemplate.execute(s);
        }
        return jdbcTemplate;
    }



}


