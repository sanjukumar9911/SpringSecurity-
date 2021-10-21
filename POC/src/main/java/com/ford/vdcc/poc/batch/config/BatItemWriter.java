package com.ford.vdcc.poc.batch.config;

import java.sql.Date;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.ford.vdcc.poc.batch.service.BatteryDataService;
import com.ford.vdcc.poc.batch.util.QueryConstants;
import com.ford.vdcc.poc.batch.util.Utility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;

public class BatItemWriter implements ItemWriter {

	@Autowired
	BatteryDataService batteryService;

	private static final Logger logger = LoggerFactory.getLogger(BatItemWriter.class);
	@Override
	public void write(List list) throws Exception {
		Gson gson = new Gson();
			if(!list.isEmpty()){

				logger.info("List is not empty :: "+list.size());

				SCAV_Update(list,60);
			}else{
				logger.info("List is empty :: ");
			}
	}

	private void SCAV_Update(List<String> list, int daysToSubtract) throws Exception {
		String startDate = getStartDate(daysToSubtract);
		logger.info("Start Date: " + startDate);

		DateTimeFormatter formatter;

		if (list == null || list.isEmpty()) {
			logger.info("Missing Hadoop Data");
			return;
		}

		try {

			batteryService.runQuery(QueryConstants.SQL_DROP_TEMP_BATTERY);
			batteryService.runQuery(QueryConstants.SQL_DUPLICATE_TO_TEMP_BATTERY);
			batteryService.runQuery(QueryConstants.SQL_DELETE_TEMP_BATTERY);
			batteryService.runBatchQuery(list, QueryConstants.SQL_INSERT_TEMP_BATTERY1);

			int  updateCount = 0;

			/*for (String s : list) {
				updateCount += batteryService.runQuery(String.format(QueryConstants.SQL_INSERT_TEMP_BATTERY, s));
			}*/

			logger.info("SCA-V Records inserted: " + list.size());
			//delete duplicates from stage table
			batteryService.runQuery(QueryConstants.SQL_DELETE_DUPLICATE_TEMP_SCAV);

			formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String currentDate = LocalDateTime.now().format(formatter);

			updateCount = batteryService.runQuery(QueryConstants.SQL_MERGE_INTO_BATTERY.replaceAll("currentDate", currentDate));
			logger.info("SCA-V Records merged: " + updateCount);

			Date batsocDate = batteryService.runSelectQuery(QueryConstants.SQL_SELECT_MAX_DATETIME_SCAV);
			//clean up older records

			String earliestDate = getEarliestDate(daysToSubtract, batsocDate);

			updateCount = batteryService.runQuery(String.format(QueryConstants.SQL_DELETE_OLD_VALUES_SCAV, earliestDate));
			logger.info("Old SCA-V records deleted: " + updateCount);

			//delete duplicates from main table
			updateCount = batteryService.runQuery(QueryConstants.SQL_DELETE_DUPLICATE_SCAV);
			logger.info("Deleted duplicates form SCAV main: " + updateCount);

		} catch (Exception ex) {
			logger.info("Error SCAV_Update :: " + ex);
		}
	}

	private String getEarliestDate(int daysToSubtract, Date batsocDate) {
		LocalDate pastDate;
		pastDate = LocalDate.parse(batsocDate.toString());
		pastDate = pastDate.minusDays(daysToSubtract);
		String earliestDate = Utility.getDetroitDate(pastDate.toString(), "yyyy-MM-dd");
		logger.info("Earlisest Date: " + earliestDate);
		return earliestDate;
	}

	private String getStartDate(int daysToSubtract) {
		LocalDate pastDate = LocalDate.now();
		pastDate = pastDate.minusDays(daysToSubtract);
		ZonedDateTime futureDateTime = Instant.now().atZone(ZoneOffset.UTC).plusDays(2);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String futureDate = futureDateTime.format(formatter);
		String startDate = Utility.getDetroitDate(pastDate.toString(), "yyyy-MM-dd");
		return startDate;
	}


}
