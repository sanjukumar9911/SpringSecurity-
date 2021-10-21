package com.ford.vdcc.poc.batch.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

public class ActiveProgramsTasklet implements Tasklet {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    private SQLProcessorService sqlProcessorService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("ENTER :: ActiveProgramsTasklet Started");

        JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();

        List<Map<String, String>> programs = sqlProcessorService.getActivePrograms();

        ExecutionContext stepContext =  contribution.getStepExecution().getExecutionContext();

        jobContext.put("programs",programs);
        logger.info("EXIT :: ActiveProgramsTasklet END");
        return RepeatStatus.FINISHED;
    }
}
