package com.ford.vdcc.poc.batch.config;

import javax.sql.DataSource;

import com.ford.vdcc.poc.batch.dao.BatteryRowMapper;
import com.ford.vdcc.poc.batch.listeners.ReaderListener;
import com.ford.vdcc.poc.batch.listeners.StepListener;
import com.ford.vdcc.poc.batch.listeners.WriterListener;
import com.ford.vdcc.poc.batch.processor.BatItemProcessor;
import com.ford.vdcc.poc.batch.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.SocketTimeoutException;

//@Configuration
//@EnableBatchProcessing
public class BatchConfig {
    private static Logger logger = LoggerFactory.getLogger(BatchConfig.class);

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("hiveDataBase")
    private DataSource dataSource1;

    @Autowired
    private DataSource dataSource;

    @Bean
    @StepScope
    public JdbcCursorItemReader<String> reader() {
        logger.info("READER START ");
        String sql = Utility.prepareQuery(1, 60);
        JdbcCursorItemReader<String> cursorItemReader = new JdbcCursorItemReader<>();
        cursorItemReader.setDataSource(dataSource1);
        logger.info("READER SQL :: "+sql);
        cursorItemReader.setSql(sql);
        cursorItemReader.setRowMapper(new BatteryRowMapper());
        logger.info("READER END ");
        return cursorItemReader;
    }

    @Bean
    @StepScope
    public BatItemProcessor processor() {
        logger.info("Processor ");
        return new BatItemProcessor();
    }

    @Bean
    @StepScope
    public ItemWriter writer() {
        logger.info("WRITER START");
        return new BatItemWriter();
    }

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1").
                <String, String>chunk(1000).
                reader(reader()).faultTolerant().retryLimit(3).retry(SocketTimeoutException.class).
                processor(processor()).
                writer(writer()).faultTolerant().retryLimit(3).retry(Exception.class).
                listener(StepListener.class).
                listener(ReaderListener.class).
                listener(WriterListener.class).
                build();
    }

    @Bean("firstJob")
    public Job exportPerosnJob() {
        //return jobBuilderFactory.get("exportPeronJob").incrementer(new RunIdIncrementer()).start(step1()).next(step2()).next(step3()).build();
        return jobBuilderFactory.get("exportPeronJob").incrementer(new RunIdIncrementer()).start(step1()).build();
    }
}
