package com.ford.vdcc.poc.batch.config;

import com.ford.vdcc.poc.batch.util.QueryConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class VinLoadWriterWriter implements ItemWriter {


	@Autowired
	@Qualifier("jdbcSqlTemplate")
	JdbcTemplate template;

	@Value("${hadoop.retry:5}")
	private String hadoopRetry;

	@Value("${hadoop_vdcc_table}")
	private String hadoopTableName;

	@Autowired
	@Qualifier("hiveDataBase")
	private DataSource dataSource1;

	private static final Logger logger = LoggerFactory.getLogger(VinLoadWriterWriter.class);

	@Value("#{stepExecution.jobExecution}")
	private JobExecution jobExecution;

	@Override
	public void write(List items) throws Exception {
		//logger.info("Data :: " + items);
		ExecutionContext jobContext = jobExecution.getExecutionContext();

		List<Map<String, String>> programs = (List<Map<String, String>>) jobContext.get("programs");
		Map<String, String> plants = (Map<String, String>) jobContext.get("plants");

		List<Map<String, String>> dataList = processHadoopData(items, programs, plants);

		List<Map<String, String>> knownBuildData = getKnownBuildData(dataList);
		logger.info("Received knownBuildData " + knownBuildData.size());

		List<String> list = createInsertStringForVehicle(knownBuildData);
		logger.info("Insert list to SQL " + list.size());

		int insertCount = insertToVehicleTable(list);
		logger.info("Inserted Count in Vehicle table " + insertCount);
		logger.info("Saving new Vins to vehicle table done");

		// save new Vins to hadoop
		logger.info("Saving new Vins to hadoop vdcc_vins started");
		List<String> hadoopInsertList = createInsertStringForHadoop(knownBuildData);
		insertToHadoop(hadoopInsertList);
		logger.info("Saving new Vins to hadoop vdcc_vins done");

	}

	public int insertToHadoop(List<String> list) throws SQLException {
		int insertCount = 0;
		Connection conn = null;
		Statement stmt = null;
		logger.info("Data\n" + list.size());
		boolean isSuccess = false;
		int retryCount = 0;
		for (String s : list) {
			while (!isSuccess && retryCount < Integer.parseInt(hadoopRetry)) {
				String sql = String.format(QueryConstants.HADOOP_INSERT_VDCC_VINS.replace("hadoopTableName", hadoopTableName), s);
				try {
					conn = dataSource1.getConnection();
					stmt = conn.createStatement();
					insertCount += stmt.executeUpdate(sql);
					isSuccess = true;
				} catch (SQLException throwables) {
					throwables.printStackTrace();
					retryCount++;
				} finally {
					conn.close();
					stmt.close();
				}
			}
		}
		return insertCount;
	}


	public List<String> createInsertStringForHadoop(List<Map<String, String>> data) {
		List<String> list = new ArrayList<>();
		List<String> subList = new ArrayList<>();
		int totalCount = 0;
		for (Map<String, String> map : data) {
			++totalCount;
			subList.add(String.format("('%s','%s','%s','%s')", map.get("vin"), map.get("program"), map.get("mfal") == null ? "" : map.get("mfal"), map.get("build")));
			if (subList.size() == 1000) {
				list.add(String.join(",", subList));
				subList.clear();
			}
		}
		if (subList.size() > 0) {
			list.add(String.join(",", subList));
			subList.clear();
		}
		logger.info("Inserting Hadoop Records Count: {}", totalCount);
		return list;
	}

	public int insertToVehicleTable(List<String> list) throws SQLException {
		int insertCount = 0;
		Statement stmt = null;
		Connection conn = template.getDataSource().getConnection();
		for (String s : list) {
			stmt = conn.createStatement();
			try {
				insertCount += stmt.executeUpdate(String.format(QueryConstants.SQL_INSERT_VEHICLE, s));
				stmt.executeUpdate(QueryConstants.SQL_UPDATE_VEHICLE);
			} catch (Exception ex) {
				logger.error(ex.getMessage());
			}
			conn.close();
			stmt.close();
		}
		return insertCount;
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


	public List<Map<String, String>> getKnownBuildData(List<Map<String, String>> dataList) throws SQLException {
		Map<String, Map<String, Pair<String, String>>> programToBuildChar = getProgramToBuildChars();  //P702 -> (D -> PP)
		List<Map<String, String>> knownBuildChars = new ArrayList<>();
		for (Map<String, String> data : dataList) {
			Map<String, Pair<String, String>> charToBuild = programToBuildChar.get(data.get("program"));  //D -> PP
			if (charToBuild != null && charToBuild.containsKey(data.get("buildChar"))) {
				Pair<String, String> buildChar = charToBuild.get(data.get("buildChar"));
				data.put("build", buildChar.getLeft());  //PP
				data.put("program", buildChar.getRight());  //PP
				knownBuildChars.add(data);
			}
		}
		return knownBuildChars;
	}

	private Map<String, Map<String, Pair<String, String>>> getProgramToBuildChars() throws SQLException {
		Map<String, Map<String, Pair<String, String>>> programToBuildChar = new HashMap<>();
		Statement stmt = null;
		Connection conn = template.getDataSource().getConnection();
		stmt = conn.createStatement();
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
		conn.close();
		stmt.close();
		return programToBuildChar;
	}


	private List<Map<String, String>> processHadoopData(List<List<String>> dataList, List<Map<String, String>> programs, Map<String, String> plants) {
		logger.info("Processing data received from Hadoop ... " + dataList.size());
		List<Map<String, String>> data = new ArrayList<>();
		dataList.stream().forEach(
				d -> {
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
					data.add(map);
				});
		return data;
	}
}

