package com.ford.vdcc.poc.batch.config;

import com.ford.vdcc.poc.batch.util.QueryConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class LoadTempTableTasklet implements Tasklet {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    @Qualifier("jdbcHiveTemplate")
    private JdbcTemplate template;

    @Value("${hadoop_vdcc_table}")
    private String hadoopTableName;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.info("ENTER :: LoadTempTableTasklet Started");
        JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();

        if(jobContext.get("programs")!=null) {
            List<Map<String, String>> programs = (List<Map<String, String>>) jobContext.get("programs");
            Map<String, String> plants = (Map<String, String>) jobContext.get("plants");

            String sql = String.format(QueryConstants.HADOOP_CREATE_TEMP_TABLE, buildCondition(programs));

            logger.info("SQL  :: "+sql);

            template.execute(sql);

            logger.info("EXIT :: LoadTempTableTasklet END");
            return RepeatStatus.FINISHED;
        }else {
            logger.info("EXIT :: LoadTempTableTasklet END, NULL, No Programs Found");
            return null;
        }
    }

    private CharSequence buildCondition(List<Map<String, String>> programs) {
        StringBuilder programSb = new StringBuilder();
        programs.forEach(program -> {
            programSb.append("(mveproducttype9  = '")
                    .append(program.get("productCode").substring(0, 1))
                    .append("' AND mvevehln8 = '")
                    .append(program.get("productCode").substring(1))
                    .append("' AND mvemodyr7 = ")
                    .append(program.get("year"))
                    .append(") OR ");
        });
        return programSb.subSequence(0, programSb.length() - 3);
    }

    private List<Map<String, String>> processHadoopData(List<List<String>> dataList, List<Map<String, String>> programs, Map<String, String> plants) {
        logger.info("Processing data received from Hadoop ... " + dataList.size());
//        AtomicInteger count = new AtomicInteger();
        List<Map<String, String>> data = new ArrayList<>();
        dataList.stream().forEach(
                d -> {
//                    count.getAndIncrement();
//                    if (count.get() % 500 == 0) logger.info("Processed " + count.get() + " records");
                    String vin = d.get(0);
                    String programData = d.get(1);
                    if (programData != null) {
                        for (Map<String, String> p : programs) {
                            if (p.get("program") != null && programData.toLowerCase().contains(p.get("program").toLowerCase())) {
                                programData = p.get("program");
                            }
                        }
                    }
                    String program = programData;
                    String team = "Unassigned";
                    String mfalData = d.get(2);
                    if (mfalData != null) {
                        if (mfalData.contains("HUKAL")) {
                            mfalData = "HUKAL";
                        } else if (mfalData.contains("HUKAM")) {
                            mfalData = "HUKAM";
                        } else if (mfalData.contains("HUKAN")) {
                            mfalData = "HUKAN";
                        } else if (mfalData.contains("HUKAK")) {
                            mfalData = "HUKAK";
                        } else {
                            mfalData = null;
                        }
                    }
                    String mfal = mfalData;
                    String hadoopPlant = d.get(3);
                    String plantCd = null;
                    String plantName = null;
                    if (hadoopPlant != null) {
                        String tempPlant = plants.get(hadoopPlant.toUpperCase().trim());
                        if (!Objects.isNull(tempPlant)) {
                            String[] plantcd_plant = tempPlant.split(",");
                            plantCd = plantcd_plant[0];
                            plantName = plantcd_plant[1];
                        }
                    }
                    String buildChar = d.get(4);
                    String buildDate = d.get(5);

                    // year, model, warrantyStartDate, engine, color
                    String year = d.get(6);
                    String model = d.get(7);
                    String warrantyStartDate = d.get(8);
                    String engine = d.get(9);
                    String color = d.get(10);
                    String fuelTypeData = d.get(11);
                    if (fuelTypeData != null) {
                        if (fuelTypeData.contains("E")) {
                            fuelTypeData = "Electric";
                        } else if (fuelTypeData.contains("G") || fuelTypeData.contains("F") || fuelTypeData.contains("H")) {
                            fuelTypeData = "Gasoline";
                        } else if (fuelTypeData.contains("D")) {
                            fuelTypeData = "Diesel";
                        } else {
                            fuelTypeData = null;
                        }
                    }
                    String fuelType = fuelTypeData;

//                    String colorDesc = vinDecoder.getVehicleColor(vin);

                    Map<String, String> map = new HashMap<>();
                    map.put("vin", vin);
                    map.put("program", program);
                    map.put("team", team);
                    map.put("mfal", mfal);
                    map.put("plantcd", plantCd);
                    map.put("plant", plantName);
                    map.put("buildChar", buildChar);
                    map.put("buildDate", buildDate);
                    map.put("year", year);
                    map.put("model", model);
                    map.put("warrantyStartDate", warrantyStartDate);
                    map.put("engine", engine);
                    map.put("color", color);
                    map.put("fuelType", fuelType);
//                    map.put("colorDesc", colorDesc);
                    data.add(map);
                });
        return data;
    }


}
