package com.ford.vdcc.poc.batch.dao;

import com.ford.vdcc.poc.batch.util.Utility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BatteryRowMapper implements RowMapper<String> {
    @Override
    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
        {
            //System.out.println("Map Row :: ");

            StringBuffer buf = new StringBuffer();
            try {
                String vin = rs.getString(1);
                int battery_soc = Integer.parseInt(rs.getString(2));
                String signal_time = Utility.getDetroitDate(rs.getString(3), "yyyy-MM-dd HH:mm:ss");
                buf.append(String.format("('%s',%d,'%s','%s')", vin, battery_soc, signal_time, "SCA-V"));
            }catch (Exception e){
                System.out.println("Error in Mapper :: "+e.getMessage());
                e.printStackTrace();
            }
            //System.out.println("Map Row :: "+buf.toString());

            return buf.toString();
        }
    }
}
