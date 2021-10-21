package com.ford.vdcc.poc.batch.util;

import java.util.Arrays;
import java.util.List;

public class QueryConstants {
    public static String[] SETTING_QUERIES = {
            "SET hive.execution.engine=tez",
            "SET hive.ignore.mapjoin.hint=false",
            "SET hive.exec.orc.split.strategy=BI",
            "SET hive.exec.parallel=true",
            "SET hive.exec.parallel.thread.number=64",
            "SET hive.exec.compress.intermediate=true",
            "SET hive.exec.compress.output=true",
            "SET hive.auto.convert.join=true",
            "SET hive.auto.convert.join.noconditionaltask=true",
            "SET hive.auto.convert.join.noconditionaltask.size=405306368", // 405MB
            "SET hive.auto.convert.join.use.nonstaged=true",
            "SET hive.vectorized.execution.enabled=true",
            "SET hive.vectorized.execution.reduce.enabled =true",
            "SET hive.cbo.enable=true",
            "SET hive.compute.query.using.stats=true",
            "SET hive.stats.fetch.column.stats=true",
            "SET hive.merge.mapfiles=true",
            "SET hive.merge.mapredfiles=true",
            "SET hive.merge.size.per.task=134217728",
            "SET hive.merge.smallfiles.avgsize=44739242",
            "SET mapreduce.job.reduce.slowstart.completedmaps=0.8",
            "SET hive.orc.cache.use.soft.references=true",
            "SET hive.tez.container.size=32768",
            "SET tez.am.resource.memory.mb=32768"
    };

    public static String FNV_MODEL_QUERY =
            "SELECT vin,modelled_soc,event_creation_time,geid " +
            "FROM  [ito086747.hosts.cloud.ford.com].[diagnostics_denorm].[integrations].[low_battery_soc_view] " +
            "WHERE YEAR(event_creation_time) > previousYear AND year(event_creation_time) <= currentYear " +
            "  AND LEN(vin) = 17";

    public static String HADOOP_COUNTRIES_STRING = "'CAN','CHN','MEX','USA'";

    public static String HADOOP_SELECT_VDCC_VINS = "SELECT vin FROM prxcvhcl.vdcc_vins";

    public static List<String> HADOOP_PARTITION_DATE_TABLES = Arrays.asList(new String[]{
            "cvdp.NCVDC62_TCUFTCP_MSG_SEC_HTE",
            "cvdp.NCVDC62_TCU4G_MSG_PJ1_SEC_HTE",
            "cvdp.NCVDC62_FNV2_MSG_SEC_HTE"});

    public static List<String> HADOOP_PARTITION_MONTH_TABLES = Arrays.asList(new String[]{
            "cvdp.NCVDC62_FNV2_PRS_SEC_HTE",
            "cvdp.NCVDC62_FNV2_PSA_SEC_HTE",
            "cvdp.NCVDC62_FNV2_PSU_SEC_HTE"});

    public static String HADOOP_SUB_QUERY =
            "SELECT cvdc62_vin_d_3, cvdc62_bsbattsoc_r, cvdc62_der_event_s_3, cvdc62_partition_country_x, cvdc62_partitionType_x FROM %s ";

    public static String HADOOP_QUERY =
            "SELECT combined.cvdc62_vin_d_3 AS vin, combined.cvdc62_bsbattsoc_r AS battery_soc, combined.cvdc62_der_event_s_3 AS signal_time " +
            "FROM ( " +
            "subUnionQueries " +
            ") combined " +
            "WHERE combined.cvdc62_vin_d_3 IN (vdccVins) " +
            "AND combined.cvdc62_partition_country_x IN (countriesString) " +
            "AND combined.cvdc62_bsbattsoc_r <= 30 AND combined.cvdc62_bsbattsoc_r > 0 " +
            "partitionCondition ";

    public static String HADOOP_PARTITION_DATE_CONDITION = "AND combined.cvdc62_partition_date_x BETWEEN 'startDate 00:00:00' AND 'endDate 00:00:00'";

    public static String HADOOP_PARTITION_MONTH_CONDITION = "AND combined.cvdc62_partition_month_x BETWEEN 'startMonth' AND 'endMonth' " +
            "AND combined.cvdc62_der_event_s_3 BETWEEN 'startDate 00:00:00' AND 'endDate 00:00:00'";

    public static String SQL_DROP_TEMP_BATTERY_FNV = "DROP TABLE sched_detail_battery_fnv";

    public static String SQL_DUPLICATE_TO_TEMP_BATTERY_FNV = "SELECT TOP 1 * INTO sched_detail_battery_fnv FROM detail_battery";

    public static String SQL_DELETE_TEMP_BATTERY_FNV = "delete from sched_detail_battery_fnv";

    public static String SQL_INSERT_TEMP_BATTERY_FNV = "INSERT INTO sched_detail_battery_fnv(vin,battery_soc,value_datetime,datasource,geid,geid_link) VALUES %s ";

    public static String SQL_INSERT_TEMP_BATTERY_FNV1 = "INSERT INTO sched_detail_battery_fnv(vin,battery_soc,value_datetime,datasource,geid,geid_link) VALUES (?,?,?,?,?,?)) ";

    public static String SQL_REMOVE_FROM_TEMP_BATTERY_FNV = "DELETE FROM sched_detail_battery_fnv WHERE vin NOT IN (SELECT vin FROM vehicle)";

    public static String SQL_MERGE_INTO_BATTERY_FNV = "MERGE detail_battery v " +
            "USING sched_detail_battery_fnv t " +
            "ON (t.vin = v.vin " +
            "AND t.battery_soc = v.battery_soc " +
            "AND t.value_datetime = v.value_datetime " +
            "AND t.datasource = v.datasource " +
            "AND t.geid = v.geid) " +
            "WHEN NOT MATCHED BY TARGET THEN " +
            "INSERT (vin,battery_soc,value_datetime,geid,geid_link,datasource,record_datetime) " +
            "VALUES (t.vin,t.battery_soc,t.value_datetime,t.geid,t.geid_link,t.datasource,'currentDate');";

    public static String SQL_SELECT_MAX_DATETIME_FNV = "SELECT MAX(value_datetime) AS batsocDate FROM detail_battery WHERE datasource='FNV Analytics'";

    public static String SQL_DELETE_OLD_VALUES_FNV = "DELETE FROM detail_battery WHERE datasource='FNV Analytics' AND value_datetime < '%s'";

    public static String SQL_DELETE_DUPLICATE_FNV = "DELETE FROM detail_battery WHERE datasource='FNV Analytics' AND id NOT IN (SELECT MAX(id) FROM detail_battery GROUP BY vin,datasource,battery_soc,geid,value_datetime)";

    public static String SQL_DROP_TEMP_BATTERY = "DROP TABLE sched_detail_battery";

    public static String SQL_DUPLICATE_TO_TEMP_BATTERY = "SELECT TOP 1 * INTO sched_detail_battery FROM detail_battery";

    public static String SQL_DELETE_TEMP_BATTERY = "DELETE FROM sched_detail_battery";

    public static String SQL_INSERT_TEMP_BATTERY = "INSERT INTO sched_detail_battery(vin,battery_soc,value_datetime,datasource) VALUES %s ";

    public static String SQL_INSERT_TEMP_BATTERY1 = "INSERT INTO sched_detail_battery(vin,battery_soc,value_datetime,datasource) VALUES (?,?,?,?) ";

    public static String SQL_DELETE_DUPLICATE_TEMP_SCAV = "DELETE FROM sched_detail_battery WHERE datasource='SCA-V' AND id NOT IN (SELECT MAX(id) FROM sched_detail_battery GROUP BY vin,datasource,battery_soc,value_datetime)";

    public static String SQL_MERGE_INTO_BATTERY = "MERGE detail_battery v " +
            "USING sched_detail_battery t " +
            "ON (t.vin = v.vin " +
            "AND t.battery_soc = v.battery_soc " +
            "AND v.datasource = v.datasource " +
            "AND t.value_datetime = v.value_datetime) " +
            "WHEN NOT MATCHED BY TARGET THEN " +
            "INSERT (vin,battery_soc,value_datetime,datasource,record_datetime) " +
            "VALUES (t.vin,t.battery_soc,t.value_datetime,t.datasource,'currentDate');";

    public static String SQL_SELECT_MAX_DATETIME_SCAV = "SELECT MAX(value_datetime) AS batsocDate FROM detail_battery WHERE datasource='SCA-V'";

    public static String SQL_DELETE_OLD_VALUES_SCAV = "DELETE FROM detail_battery WHERE datasource='SCA-V' AND value_datetime < '%s'";

    public static String SQL_DELETE_DUPLICATE_SCAV = "DELETE FROM detail_battery WHERE datasource='SCA-V' AND id NOT IN (SELECT MAX(id) FROM detail_battery GROUP BY vin,datasource,battery_soc,value_datetime)";

    public static String SQL_INSERT_SERVICE_STATUS = "INSERT INTO service_status(name,run_datetime,message,status) VALUES ('%s','%s','%s','%s')";



    //VIN Load

        public static String HADOOP_CREATE_TEMP_TABLE =
                "CREATE TEMPORARY TABLE prxcvhcl.load_save_temp AS SELECT v.vin_2 AS vin, mfg_wers_vl_desc795 AS program, v.mvewersfamdmvewersftrd38 AS mfal, " +
                        "v.mvewersasmplantfmlymvewersasmplantftr757 AS plantcd, SUBSTRING(mvenavisvoc31,110,1) AS buildchar, " +
                        "v.build_date11 AS builddate, v.mvederivedmodyr895 AS year, v.vehicle_description888 AS model, " +
                        "v.mvewarrstrtdate737 AS warrantystartdate, v.mfg_wers_en_desc801 AS engine, v.mveslseurpaintcd823 AS color, " +
                        "v.engine_fuel_type885 AS fueltype " +
                        "FROM cvdp.NCVDPJC_SAVE_VEH_PII_VW v " +
                        "WHERE SUBSTRING(mvenavisvoc31,110,1) != ' ' AND (%s) ";

        public static String HADOOP_VINLOAD_WITH_TEMP_TABLE =
                "SELECT vin, program, mfal, plantcd, buildchar, builddate, year, model, warrantystartdate, engine, color, fueltype FROM prxcvhcl.load_save_temp " +
                        "WHERE vin NOT IN (SELECT vin FROM prxcvhcl.hadoopTableName) ";

    /* Backup
    public static String HADOOP_VINLOAD_QUERY_WITHOUT_PRE_LOAD =
            "SELECT v.vin_2 AS vin, mfg_wers_vl_desc795 AS program, v.mvewersfamdmvewersftrd38 AS mfal, " +
            "v.mvewersasmplantfmlymvewersasmplantftr757 AS plantcd, SUBSTRING(mvenavisvoc31,110,1) AS buildchar, " +
            "v.build_date11 AS builddate, v.mvederivedmodyr895 AS year, v.vehicle_description888 AS model, " +
            "v.mvewarrstrtdate737 AS warrantystartdate, v.mfg_wers_en_desc801 AS engine, v.mveslseurpaintcd823 AS color, " +
            "v.engine_fuel_type885 AS fueltype " +
            "FROM cvdp.NCVDPJC_SAVE_VEH_PII_VW v " +
            "WHERE v.VIN_2 NOT IN (SELECT vin FROM prxcvhcl.hadoopTableName) " +
            "AND SUBSTRING(mvenavisvoc31,110,1) != ' ' AND (%s) ";
    * */

        public static String HADOOP_INSERT_VDCC_VINS = "INSERT INTO prxcvhcl.hadoopTableName(vin, program, mfal, build) VALUES %s";

        public static String SQL_SELECT_BUILD_PROGRAM = "SELECT program, char_at_110, build, program2 FROM build_mapping";

        public static String SQL_SELECT_PLANT = "SELECT hadoop_plant, plantcd, plant from dbo.detail_plant";

        public static String SQL_SELECT_ACTIVE_PROGRAM = "SELECT program, model, product_code, year FROM program WHERE active='Y'";

        //    public static String SQL_INSERT_VEHICLE = "INSERT INTO vehicle(vin, program, team, mfal, plantcd, plant, build, build_dt, " +
//            "year, model, warranty_dt, engine, color, fuel_type, color_description, scorecard, fleet) VALUES %s";
        // Exclude color_description
        public static String SQL_INSERT_VEHICLE = "INSERT INTO vehicle(vin, program, team, mfal, plantcd, plant, build, build_dt, " +
                "year, model, warranty_dt, engine, color, fuel_type, scorecard, fleet) VALUES %s";

        public static String SQL_UPDATE_VEHICLE = "UPDATE vehicle SET mfal=null WHERE mfal='null'";

        public static String SQL_GET_CRON_EXPRESSION = "SELECT cron_expression FROM vdcc_schedulers WHERE service_name = '%s'";


}
