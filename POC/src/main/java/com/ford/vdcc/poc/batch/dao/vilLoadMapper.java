package com.ford.vdcc.poc.batch.dao;

import com.ford.vdcc.poc.batch.util.Utility;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class vilLoadMapper implements RowMapper<List<String>> {
    @Override
    public List<String> mapRow(ResultSet rs, int rowNum) throws SQLException {
        List<String> rowData = new ArrayList<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            rowData.add(rs.getString(i));
        }

        return rowData;
    }
}
