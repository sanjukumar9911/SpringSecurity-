package com.ford.vdcc.poc.batch.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@Service
public class BatteryDataService {

    @Autowired
    @Qualifier("jdbcSqlTemplate")
    JdbcTemplate template;

    public int runQuery(String sqlDropTempBatteryFnv) {
        return template.update(sqlDropTempBatteryFnv);
    }

    public void runBatchQuery(List<String> list, String query) {

        template.batchUpdate(query, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                try {
                    String[] col = new String[4];
                    System.out.println("list Data :: " + list.get(i));
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    col = list.get(i).split(",");
                    ps.setString(1, col[0]);
                    ps.setInt(2, Integer.valueOf(col[1]));

                    ps.setDate(3, new Date(sdf.parse(col[2].replace("'","")).getDate()));
                    ps.setString(4, "SCA-V");
                }catch(Exception ex){
                    System.out.println("Run Batch Query :: "+ex.getMessage());
                }
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        return;
    }

    public Date runSelectQuery(String sqlSelectMaxDatetimeScav) {
        return template.queryForObject(sqlSelectMaxDatetimeScav, Date.class);
    }
}
