package com.ford.vdcc.poc.batch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Utility {

    private static Logger logger = LoggerFactory.getLogger(Utility.class);

    public static String getDetroitDate(String utcDate, String format){
        String tempDt = utcDate;
        if (format.contains("ss")){
            tempDt = tempDt.replace(" ", "T") + "Z";
        } else {
            tempDt = tempDt + "T00:00:00Z";
        }
        Instant timestamp = Instant.parse(tempDt);
        ZonedDateTime detroitTime = timestamp.atZone(ZoneId.of("America/Detroit"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return detroitTime.format(formatter);
    }

    public static void updateServiceStatus(Connection con, String name, String msg, String status, String startTime) {
        String sql = String.format(QueryConstants.SQL_INSERT_SERVICE_STATUS, name, startTime, msg, status);
        logger.info("Insert service status: " + sql);
        try{
            Statement stmt = con.createStatement();
            stmt.executeUpdate(sql);
        }catch(SQLException exception){
            throw new RuntimeException("Something went wrong while updating status", exception);
        }

    }

    public static String prepareQuery(int type,int numberOfDays) {
        String startDate = getDetroitDate(LocalDate.now().minusDays(numberOfDays).toString(), "yyyy-MM-dd");
        String endDate = Instant.now().atZone(ZoneOffset.UTC).plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String query = "";
        List<String> tempList = new ArrayList<>();

        // type=1: date; type=2: month
        String subQuery = QueryConstants.HADOOP_SUB_QUERY
                .replaceAll("partitionType", (type == 1) ? "partition_date" : "partition_month");

        if (type == 1) {
            QueryConstants.HADOOP_PARTITION_DATE_TABLES.forEach(s -> {
                tempList.add(String.format(subQuery, s));
            });
        } else {
            QueryConstants.HADOOP_PARTITION_MONTH_TABLES.forEach(s -> {
                tempList.add(String.format(subQuery, s));
            });
        }

        query = QueryConstants.HADOOP_QUERY
                .replaceAll("subUnionQueries", String.join("UNION ", tempList))
                .replaceAll("partitionCondition",
                        (type == 1) ? QueryConstants.HADOOP_PARTITION_DATE_CONDITION :
                                QueryConstants.HADOOP_PARTITION_MONTH_CONDITION
                                        .replaceAll("startMonth", getMonth(startDate))
                                        .replaceAll("endMonth", getMonth(endDate)));

        query = query.replaceAll("startDate", startDate)
                .replaceAll("endDate", endDate)
                .replaceAll("vdccVins", QueryConstants.HADOOP_SELECT_VDCC_VINS)
                .replaceAll("countriesString", QueryConstants.HADOOP_COUNTRIES_STRING);

        return query;
    }

    public static String updateYears(String sql) {
        int currentYear = LocalDate.now().getYear();
        return sql.replaceAll("currentYear", String.valueOf(currentYear)).replaceAll("previousYear", String.valueOf((currentYear - 2)));
    }

    private static String getMonth(String date) {
        return date.substring(0,7);
    }
}
