package com.ford.vdcc.poc.batch.config;

import com.ford.vdcc.poc.batch.util.QueryConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SQLProcessorService {

    private Logger logger = LoggerFactory.getLogger(SQLProcessorService.class);

    @Value("${dbUrl}")
    private String dbUrl;

    @Value("${scheduler.serviceName}")
    private String serviceName;

    @Autowired
    @Qualifier("jdbcSqlTemplate")
    public JdbcTemplate template;

    private Connection conn = null;

    public  Connection getConnection() {
        if (conn == null) {
            try {
                conn = template.getDataSource().getConnection();
                logger.info("Connection Established.");
            } catch ( SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }

    public void closeConnection(Connection conn, ResultSet rs, Statement stmt) throws SQLException {
        if(rs!=null){
            rs.close();
        }
        if(stmt!=null){
            stmt.close();
        }
        if(conn!=null){
            conn.close();
        }
    }

    public int insertToVehicleTable(List<String> list) throws SQLException {
        int insertCount = 0;
        Statement stmt = null;
        conn = getConnection();
        for (String s : list) {
            stmt = conn.createStatement();
            try {
                insertCount += stmt.executeUpdate(String.format(QueryConstants.SQL_INSERT_VEHICLE, s));
                stmt.executeUpdate(QueryConstants.SQL_UPDATE_VEHICLE);
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
        closeConnection(conn,null,stmt);
        return insertCount;
    }

    public List<Map<String, String>> getActivePrograms1() {

        return template.query(QueryConstants.SQL_SELECT_PLANT, new RowMapper<Map<String, String>>() {

            @Override
            public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                List<Map<String, String>> programs = new ArrayList<>();
                Map<String, String> map = new HashMap<>();
                map.put("program", rs.getString(1));
                map.put("model", rs.getString(2));
                map.put("productCode", rs.getString(3));
                map.put("year", rs.getString(4));
                return map;
            }
        });
    }
    public Map<String, String> getPlantmappings1() {

        return (Map<String, String>) template.query(QueryConstants.SQL_SELECT_PLANT, new RowMapper<Map<String, String>>() {

            @Override
            public Map<String, String> mapRow(ResultSet rs, int rowNum) throws SQLException {
                Map<String, String> map = new HashMap<>();
                map.put(rs.getString(1), rs.getString(2) + "," + rs.getString(3));
                return map;
            }
        });
    }

    public List<Map<String, String>> getActivePrograms() throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        List<Map<String, String>> programs = new ArrayList<>();

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(QueryConstants.SQL_SELECT_ACTIVE_PROGRAM);
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("program", rs.getString(1));
                map.put("model", rs.getString(2));
                map.put("productCode", rs.getString(3));
                map.put("year", rs.getString(4));
                programs.add(map);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                closeConnection(conn,rs,stmt);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return programs;
    }

    public Map<String, String> getPlantmappings() {
        Statement stmt = null;
        ResultSet rs = null;
        Map<String, String> map = new HashMap<>();
        try {
            stmt = template.getDataSource().getConnection().createStatement();
            rs = stmt.executeQuery(QueryConstants.SQL_SELECT_PLANT);
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2) + "," + rs.getString(3));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            try {
                closeConnection(conn,rs,stmt);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return map;
    }

    public List<Map<String, String>> getKnownBuildData(List<Map<String, String>> dataList) throws SQLException {
        Map<String, Map<String, Pair<String, String>>> programToBuildChar = getProgramToBuildChars();  //P702 -> (D -> PP)
        List<Map<String, String>> knownBuildChars = new ArrayList<>();
        for (Map<String, String> data : dataList) {
            Map<String, Pair<String, String>> charToBuild = programToBuildChar.get(data.get("program"));  //D -> PP
            if (charToBuild != null && charToBuild.containsKey(data.get("buildChar"))) {
                Pair<String, String> buildChar = charToBuild.get(data.get("buildChar"));
                data.put("build", buildChar.getLeft());
                data.put("program", buildChar.getRight());
                knownBuildChars.add(data);
            }
        }
        return knownBuildChars;
    }

    private Map<String, Map<String, Pair<String, String>>> getProgramToBuildChars() throws SQLException {
        Map<String, Map<String, Pair<String, String>>> programToBuildChar = new HashMap<>();
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet resultSet = stmt.executeQuery(QueryConstants.SQL_SELECT_BUILD_PROGRAM);
        while (resultSet.next()) {
            String program = resultSet.getString(1);
            Map<String, Pair<String, String>> existingBuildChars = programToBuildChar.get(program);
            if (programToBuildChar.get(program) == null) {
                existingBuildChars = new HashMap<>();
                programToBuildChar.put(program, existingBuildChars);
            }
            existingBuildChars.put(resultSet.getString(2), Pair.of(resultSet.getString(3), resultSet.getString(4)));
        }
        closeConnection(conn,resultSet,stmt);
        return programToBuildChar;
    }

    public List<String> createInsertStringForVehicle(List<Map<String, String>> data) {
        List<String> list = new ArrayList<>();
        int totalCount = 0;
        List<String> subList = new ArrayList<>();
        for (Map<String, String> map : data) {
            ++totalCount;
            subList.add(String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s', '%s', '%s')", map.get("vin"), map.get("program"),
                    map.get("team"), map.get("mfal"), map.get("plantcd"), map.get("plant"), map.get("build"), map.get("buildDate"), map.get("year"),
                    map.get("model"), map.get("warrantyStartDate"), map.get("engine"), map.get("color"), map.get("fuelType"), map.get("colorDesc"), "Launch", "Pre-Production"));
            if (subList.size() == 1000) {
                list.add(String.join(",", subList));
                subList.clear();
            }
        }
        if (subList.size() > 0) {
            list.add(String.join(",", subList));
            subList.clear();
        }
        logger.info("Vehicle insert Records Count: " + totalCount);
        return list;
    }

    public void updateServiceStatus(String name, String msg, String status, String startTime) {
        String SQL = String.format(QueryConstants.SQL_INSERT_SERVICE_STATUS, name, startTime, msg, status);
        Statement stmt = null;
        Connection conn = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            logger.info("Calling to insert into service status: " + startTime);
            stmt.executeUpdate(SQL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                closeConnection(conn,null,stmt);
            } catch (SQLException exception) {
                throw new RuntimeException("An error occurred while closing connection", exception);
            }
        }
    }

}
