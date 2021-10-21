package com.ford.vdcc.poc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entity")
@RefreshScope // important!
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    JobLauncher jobLauncher;

 //   @Autowired
 //   @Qualifier("firstJob")
 //   Job job1;


    @Autowired
    @Qualifier("vinloadjob")
    Job job2;

    @Autowired
    Environment env;

    @Value("${name}")
    String name;

 /*   @GetMapping("/start")
    public String getProduct() throws Exception {

        JobParameters params = new JobParametersBuilder()
                .addString("exportPeronJob", String.valueOf(System.currentTimeMillis()))
                .addString("flag", "true")
                .toJobParameters();
        jobLauncher.run(job1, params);
        logger.info("EXIT :: END OF JOB");
        return "OK";
    }  */

    @GetMapping("/vinload")
    public String startVinLoad() throws Exception {
        logger.info("ENTER: vinloadJob :: ");
        JobParameters params = new JobParametersBuilder()
                .addString("exportPeronJob", String.valueOf(System.currentTimeMillis()))
                .addString("flag", "true")
                .toJobParameters();
        jobLauncher.run(job2, params);
        logger.info("EXIT :: END OF JOB");
        return "OK";
    }

    @GetMapping("/testnext")
    public String goTestNext() {
        logger.info("NAME : goTest :: " + name);

        try {
        } catch (Exception ex) {
            logger.info("EXCEPTION: goTest :: ");
            ex.printStackTrace();
        }
        logger.info("END: goTest :: ");
        return name;
    }

    @PostMapping("/data")
    public TestObject goTest(@RequestBody TestObject data) throws Exception {
        logger.info("ENTER: goTest :: "+data.toString());

        return data;
    }


}
