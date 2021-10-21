package com.ford.vdcc.poc.batch.dao;/*package com.ford.vdcc.poc.batch.config;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

@Configuration
public class InMemoryConfig {
	private final String SAMPLE_DATA = "classpath:sample_data_h2.sql";
	
	@Autowired
	private DataSource datasource;
	
	@PostConstruct
	public void loadIfInMemory() throws Exception {
		Resource resource = webApplicationContext.getResource(SAMPLE_DATA);
		ScriptUtils.executeSqlScript(datasource.getConnection(), resource);
	}
}*/
