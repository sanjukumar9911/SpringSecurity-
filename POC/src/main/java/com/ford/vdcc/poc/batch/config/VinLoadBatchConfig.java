package com.ford.vdcc.poc.batch.config;


import com.ford.vdcc.poc.batch.dao.vilLoadMapper;
import com.ford.vdcc.poc.batch.util.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.SocketTimeoutException;
import java.util.List;

@Configuration
@EnableBatchProcessing  
public class VinLoadBatchConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("hiveDataBase")
    private DataSource dataSource1;

    @Autowired
    private DataSource dataSource;

    @Value("${hadoop_vdcc_table}")
    private String hadoopTableName;


    @Bean("vinloadjob")
    public Job vinLoadJob(JobBuilderFactory jobBuilders,
                                  StepBuilderFactory stepBuilders) {
        return jobBuilders.get("vinloadJob")
                .start(plantMappingStep(stepBuilders))      //Get ALl The Plants
                .next(activeProgramStep(stepBuilders))// Get All the Active Program Step
                .next(LoadTempTableStep(stepBuilders))   // Load Temp Table
                .next(step2(stepBuilders))              // Read,Process,Write -> Read the data from Hadoop and post the data in Database.
                .build();
    }

    @Bean
    public Step plantMappingStep(StepBuilderFactory stepBuilders) {
        return stepBuilders.get("plantmappingStep")
                .tasklet(plantMappingStepTasklet()).build();
    }

    @Bean
    public Step activeProgramStep(StepBuilderFactory stepBuilders) {
        return stepBuilders.get("activeProgramStep")
                .tasklet(activeProgramStepTasklet()).build();
    }

    @Bean
    public Step LoadTempTableStep(StepBuilderFactory stepBuilders) {
        return stepBuilders.get("LoadTempTable")
                .tasklet(loadTempTableTasklet()).build();
    }

    @Bean
    public Tasklet loadTempTableTasklet() {
        return new LoadTempTableTasklet();
    }

    @Bean
    public PlantMappingsTasklet plantMappingStepTasklet() {
        return new PlantMappingsTasklet();
    }

   @Bean
    public ActiveProgramsTasklet activeProgramStepTasklet() {
        return new ActiveProgramsTasklet();
    }

    @Bean
    public Step step2(StepBuilderFactory stepBuilders) {
        return stepBuilders.get("step2").
                <List<String>, List<String>>chunk(5000).
                reader(readHadoop()).faultTolerant().retryLimit(3).retry(SocketTimeoutException.class).
                processor(processor()).
                writer(writer()).faultTolerant().retryLimit(3).retry(Exception.class).
                build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<List<String>> readHadoop() {
        logger.info("READER START ");

        JdbcCursorItemReader<List<String>> cursorItemReader = new JdbcCursorItemReader<>();
        cursorItemReader.setDataSource(dataSource1);
        String selectQuery = QueryConstants.HADOOP_VINLOAD_WITH_TEMP_TABLE.replace("hadoopTableName", hadoopTableName);
        logger.info("READER SQL :: "+selectQuery);
        cursorItemReader.setSql(selectQuery);
        cursorItemReader.setRowMapper(new vilLoadMapper());
        logger.info("READER END ");
        return cursorItemReader;
    }


    @Bean
    @StepScope
    public ItemWriter writer() {
        logger.info("WRITER START");
        return new VinLoadWriterWriter();
    }

    @Bean
    @StepScope
    public ItemProcessor processor() {
        logger.info("WRITER START");
        return new VinLoadProcessor();
    }

    @Bean
    public ExecutionContextPromotionListener promotionListener() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[] {"programs" });
        listener.setKeys(new String[] {"plants" });
        return listener;
    }

}
