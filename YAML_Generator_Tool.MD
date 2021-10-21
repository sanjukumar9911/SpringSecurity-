package com.ford.vdcc.scheduler.tool.apic.tools;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ford.vdcc.scheduler.tool.apic.tools.repository.ApiDocsConfigPropertyRepository;
import com.ford.vdcc.scheduler.tool.apic.tools.repository.ApiDocsRecordRepository;
import com.ford.vdcc.scheduler.tool.apic.tools.repository.ApiMetaDataRecordRepository;
import com.ford.vdcc.scheduler.tool.apic.tools.util.ZipUtility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ApicServiceController {

    @Autowired
    ApiDocsConfigPropertyRepository repo;

    @Value("${loadMetaData}")
    boolean loadMetaDataFlag;

    @Value("${loadConfigData}")
    boolean loadConfigDataFlag;

    @Autowired
    ApiDocsRecordRepository recordRepo;

    @Autowired
    @Qualifier("restTemplate")
    RestTemplate restTemplate;

    @Autowired
    ApiMetaDataRecordRepository metaDataRecordRepo;

    @PostConstruct
    public void init() {
        log.info("ENTER :: INIT");
        try {
            Files.createDirectories(Paths.get("./src/main/resources/APIC/GENERATED_FILES"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public  void createPropertyConfig() throws IOException {

        log.info("ENTER :: createPropertyConfig()");
        if(loadConfigDataFlag) {
            repo.deleteAll();
            List<ApiDocsConfigProperty> output = processInputPropConfigFile("prop1.csv");
            for (ApiDocsConfigProperty prop : output) {
                repo.save(prop);
            }
        }
        if(loadMetaDataFlag) {
            metaDataRecordRepo.deleteAll();
            List<ApiMetaDataRecord> apiRecList = processInputMetaDataRecordFile("docsRecord.csv");
            for(ApiMetaDataRecord record : apiRecList){
                metaDataRecordRepo.save(record);
            }
        }

        log.info("EXIT :: createPropertyConfig()");
    }

    public List<ApiMetaDataRecord> processInputMetaDataRecordFile(String inputFilePath) {

        List<ApiMetaDataRecord> inputList = new ArrayList<ApiMetaDataRecord>();
        try{

            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            AtomicInteger i = new AtomicInteger(0);
            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapToMetaDataRecord).map(l-> {
                l.setId(i.incrementAndGet());
                return  l;
            }).collect(Collectors.toList());
            br.close();
        } catch (Exception e) {
        }
        log.info("inputList :: "+inputList);
        return inputList ;
    }


    public List<ApiDocsRecord> processInputdocRecordFile(String inputFilePath) {

        List<ApiDocsRecord> inputList = new ArrayList<ApiDocsRecord>();
        try{

            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            AtomicInteger i = new AtomicInteger(0);
            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapToDocRecord).map(l-> {
                l.setId(i.incrementAndGet());
                return  l;
            }).collect(Collectors.toList());
            br.close();
        } catch (Exception e) {
        }
        log.info("inputList :: "+inputList);
        return inputList ;
    }

    private Function<String, ApiDocsRecord> mapToDocRecord = (line) -> {
        String[] p = line.split("[|]");// a CSV has comma separated lines
        ApiDocsRecord item = new ApiDocsRecord();
        item.setId(Integer.valueOf(p[0]));
        item.setVersion(p[1]);
        item.setDocType(p[2]);
        item.setDocApiName(p[3]);
        item.setDocStatus(p[4]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        item.setAuditDatetime(timestamp);
        item.setDocContent(p[6]);
        return item;
    };


    private Function<String, ApiMetaDataRecord> mapToMetaDataRecord = (line) -> {
        String[] p = line.split("[|]");// a CSV has comma separated lines
        ApiMetaDataRecord item = new ApiMetaDataRecord();
        item.setId(Integer.valueOf(p[0]));
        item.setVersion(p[1]);
        item.setDocType(p[2]);
        item.setDocApiName(p[3]);
        item.setDocStatus(p[4]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        item.setAuditDatetime(timestamp);
        item.setDocContent(p[6]);
        return item;
    };




    public List<ApiDocsConfigProperty> processInputPropConfigFile(String inputFilePath) {

        List<ApiDocsConfigProperty> inputList = new ArrayList<ApiDocsConfigProperty>();
        try{

            File inputF = new File(inputFilePath);
            InputStream inputFS = new FileInputStream(inputF);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));

            AtomicInteger i = new AtomicInteger(0);
            // skip the header of the csv
            inputList = br.lines().skip(1).map(mapToItem).map(l-> {
                l.setId(i.incrementAndGet());
                return  l;
            }).collect(Collectors.toList());
            br.close();
        } catch (Exception e) {
        }

        return inputList ;
    }


    private Function<String, ApiDocsConfigProperty> mapToItem = (line) -> {
        String[] p = line.split("[,]");// a CSV has comma separated lines
        ApiDocsConfigProperty item = new ApiDocsConfigProperty();
        item.setEnvironment(p[0]);
        item.setApiKey(Integer.valueOf(p[1]));
        item.setConfigKey(p[2]);
        item.setConfigValue(p[3]);
        item.setConfigStatus(p[4]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        item.setAuditDatetime(timestamp);
        return item;
    };

    public String performWork(String env, String apiName, HttpServletResponse response) throws IOException {
        log.info("ENTER :: performWork() ");
        log.info("ENTER :: apiName :: "+apiName);
        log.info("ENTER :: env :: "+env);


        if(apiName==null || apiName.isEmpty() ){
            return "Select API Name ::";
        }else if(env==null || env.isEmpty() ){
            return "Select Environment Value ::";
        }
        ZipUtility.clearDirectory();

        Map<String, String> apiDocMap = null;
        Map<String, ApiDocsConfigProperty> apiConfigMap = null;
        List<String> apis = null;

        List<ApiMetaDataRecord> apiDocList = metaDataRecordRepo.findByDocApiName("METADATA");

        if(apiDocList!=null && !apiDocList.isEmpty()){
            apiDocMap = apiDocList.stream().collect(Collectors.toMap(ApiMetaDataRecord::getDocType,ApiMetaDataRecord::getDocContent));
            log.info("apiDocMap ::"+apiDocMap);
        }

        List<ApiDocsConfigProperty> apiProp = repo.fetchRecordsByEnvironment(env);
        if(apiProp!=null && !apiProp.isEmpty()) {
            apiConfigMap = apiProp.stream().collect(Collectors.toMap(ApiDocsConfigProperty::getConfigKey, Function.identity()));
            log.info("apiConfigMap ::"+apiConfigMap);

            apis = apiProp.stream().filter(api -> api.getConfigKey().contains("stagging")).map(api -> api.getConfigKey()).collect(Collectors.toList());
            log.info("apis :: "+apis);
        }

        if(apiName!=null && !apiName.isEmpty() && apiName.contains("ALL")){
            log.info("GENERATE FOR ALL API");
        }else{
            log.info("GENERATE FOR API"+apiName);
            apis.clear();
            apis.add(apiName);
        }

        List<String> maxList = recordRepo.fetchMaxVersion();
        String version = getVersion(maxList);

        for (String api : apis)
        {
            log.info("API **** "+api);
            String temp = api.replace("_stagging","");
            log.info("URL **** "+temp );

            try {
                String url = apiConfigMap.get(temp + "_url").getConfigValue();
                log.info("URL :: " + url);

                String host = apiConfigMap.get(api).getConfigValue();
                log.info("host :: " + host);

                ResponseEntity<String> result = restTemplate.getForEntity(new URI(url), String.class);

                JsonParser parser = new JsonParser();
                JsonElement jsonElement = parser.parse(result.getBody());

                /*String version = jsonElement.getAsJsonObject().get("info").getAsJsonObject().get("version").getAsString();
                log.info("VERSION :: " + version);*/

                if (version != null) {
                    jsonElement.getAsJsonObject().get("info").getAsJsonObject().addProperty("version", version);
                    log.info("Version updated");
                }else{
                    version="2.0.0.0";
                }

                jsonElement.getAsJsonObject().get("info").getAsJsonObject().addProperty("x-ibm-name", api);
                log.info("X-ibm-Name-Added :: ");

                if(jsonElement.getAsJsonObject().get("info").getAsJsonObject()!=null
                        && jsonElement.getAsJsonObject().get("info").getAsJsonObject().get("contact")!=null){
                    jsonElement.getAsJsonObject().get("info").getAsJsonObject().get("contact").getAsJsonObject().addProperty("name","hdoan3");
                }

                jsonElement.getAsJsonObject().get("info").getAsJsonObject().addProperty("x-ibm-name", api);
                log.info("X-ibm-Name-Added :: ");

                jsonElement.getAsJsonObject().addProperty("host", host);
                log.info("HOST Updated :: ");

                jsonElement.getAsJsonObject().addProperty("basePath", "/api");
                log.info("Base Path Updated :: ");

/*                for (Map.Entry<String, JsonElement> defElement : jsonElement.getAsJsonObject().get("definitions").getAsJsonObject().entrySet()) {

                    if (defElement != null && defElement.getValue() != null
                            && defElement.getValue().getAsJsonObject() != null
                            && defElement.getValue().getAsJsonObject().get("properties") != null){
                        for (Map.Entry<String, JsonElement> propElement : defElement.getValue().getAsJsonObject().get("properties").getAsJsonObject().entrySet()) {
                            if (propElement != null && propElement.getValue() != null && propElement.getValue().getAsJsonObject() != null &&
                                    propElement.getValue().getAsJsonObject().get("type") != null && propElement.getValue().getAsJsonObject().get("type").getAsString() != null
                                    && propElement.getValue().getAsJsonObject().get("type").getAsString().equalsIgnoreCase("array")) {
                                propElement.getValue().getAsJsonObject().addProperty("maxItems", 100);
                                log.info("Array Element Added :: ");
                            }
                        }
                    }
                }*/

                if(jsonElement.getAsJsonObject().get("paths")!=null && jsonElement.getAsJsonObject().get("paths").getAsJsonObject()!=null) {
                    for (Map.Entry<String, JsonElement> defElement : jsonElement.getAsJsonObject().get("paths").getAsJsonObject().entrySet()) {

                        if (defElement!=null && defElement.getValue() != null && defElement.getValue().getAsJsonObject() != null){
                            for (Map.Entry<String, JsonElement> propElement : defElement.getValue().getAsJsonObject().entrySet()) {

                                if(propElement.getValue()!=null && propElement.getValue().getAsJsonObject()!=null) {
                                    if(propElement.getValue().getAsJsonObject().get("parameters")!=null) {
                                        JsonArray parametersElement = propElement.getValue().getAsJsonObject().get("parameters").getAsJsonArray();
                                        JsonElement securityRef = parser.parse("{   \"$ref\": \"#/parameters/Authorization\"     }");
                                        parametersElement.add(securityRef);
                                    }else{
                                        JsonElement securityRef = parser.parse("[  { \"$ref\": \"#/parameters/Authorization\"   } ]");
                                        propElement.getValue().getAsJsonObject().add("parameters",securityRef);
                                    }
                                }

           /*                     if(propElement.getValue()!=null && propElement.getValue().getAsJsonObject()!=null) {

                                    if(propElement.getValue().getAsJsonObject().get("responses")!=null
                                    && propElement.getValue().getAsJsonObject().get("responses").getAsJsonObject()!=null
                                    && propElement.getValue().getAsJsonObject().get("responses").getAsJsonObject().get("200")!=null ) {

                                        JsonElement responsesCodes = propElement.getValue().getAsJsonObject().get("responses").getAsJsonObject().get("200").getAsJsonObject();
                                        JsonElement resp = responsesCodes;

                                        if(resp!=null && resp.getAsJsonObject()!=null && resp.getAsJsonObject().get("schema")!=null
                                                && resp.getAsJsonObject().get("schema").getAsJsonObject()!=null
                                                && resp.getAsJsonObject().get("schema").getAsJsonObject().get("type")!=null
                                                && resp.getAsJsonObject().get("schema").getAsJsonObject().get("type").getAsString()!=null
                                                && resp.getAsJsonObject().get("schema").getAsJsonObject().get("type").getAsString().equalsIgnoreCase("array")){
                                            log.info("ARR :: "+propElement.getValue().getAsJsonObject().get("responses").
                                                    getAsJsonObject().get("200").getAsJsonObject().getAsJsonObject().
                                                    get("schema").getAsJsonObject().get("type"));

                                            propElement.getValue().getAsJsonObject().get("responses").
                                                    getAsJsonObject().get("200").getAsJsonObject().getAsJsonObject().
                                                    get("schema").getAsJsonObject().addProperty("maxItems", 100);
                                            log.info("responses.schema.type-100 added :: ");
                                        }
                                    }
                                }*/
                            }
                        }
                    }
                }


                //maxItems: 100
                log.info("SECURITY :: " + apiDocMap.get("security"));
                JsonElement securityElement = parser.parse(apiDocMap.get("security"));
                jsonElement.getAsJsonObject().add("security", securityElement);
                log.info("Security Added :: ");

                JsonElement securityDefinitionsElement = parser.parse(apiDocMap.get("securityDefinitions").
                        replace("apicorpkey",apiConfigMap.get("apicorpkey").getConfigValue()));
                jsonElement.getAsJsonObject().add("securityDefinitions", securityDefinitionsElement);
                log.info("securityDefinitionsElement Added :: ");

                JsonElement xIbmConfigurationElement = parser.parse(apiDocMap.get("x-ibm-configuration").
                        replace("targetvaluekey","https://"+apiConfigMap.get(api).getConfigValue()));
                jsonElement.getAsJsonObject().add("x-ibm-configuration", xIbmConfigurationElement);
                log.info("x-ibm-configuration Added :: ");

                JsonElement xIbmEndPoints = parser.parse(apiDocMap.get("x-ibm-endpoints").
                        replace("apigtwkeyvalue",apiConfigMap.get("apigtwkeyvalue").getConfigValue()));
                jsonElement.getAsJsonObject().add("x-ibm-endpoints", xIbmEndPoints);
                log.info("x-ibm-endpoints Added :: ");

                JsonElement parametersElement = parser.parse(apiDocMap.get("parameters"));
                jsonElement.getAsJsonObject().add("parameters", parametersElement);
                log.info("parameters Added :: ");

                jsonElement.getAsJsonObject().get("parameters").
                        getAsJsonObject().get("Authorization").getAsJsonObject().
                        addProperty("pattern","^[a-zA-Z0-9:.\\s<>-_$^&*?#;\"\\[\\]{}]*");

                String yaml = createYmlFile(api, jsonElement,version);

                loadFilesToDataBase(yaml,version,api,env);

            }catch (Exception ex) {
                log.info("Exception :: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        ZipUtility.zip(response);
        log.info("EXIT :: performWork() ");
        return "OK";
    }

    private String getVersion(List<String> maxList) {
        String version;
        if(maxList !=null && !maxList.isEmpty()){
            version = maxList.get(0);
            int verNumner = Integer.parseInt(version.replaceAll("[.]",""));
            if(verNumner==0){
                version = "2.0.0.0";
            }else {
                String tempVersion = String.valueOf(verNumner + 1);
                char[] ch = tempVersion.toCharArray();
                tempVersion = ch[0] + "." + ch[1] + "." + ch[2] + "." + ch[3];
                version = tempVersion;
                log.info("Version :: " + tempVersion);
            }
        }else{
            version = "2.0.0.0";
        }
        return version;
    }

    private String createYmlFile(String api, JsonElement jsonElement, String version) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> obj = mapper.readValue(jsonElement.toString().replaceAll("\"array\",","\"array\",\"maxItems\":100,"), new TypeReference<Map<String, Object>>() {
        });
        System.out.println("**********Convert Java objects to YAML**********");
        DumperOptions opts = new DumperOptions();
        opts.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        String yaml = new Yaml(opts).dump(obj);
        log.info("YML**************************************");

        //ResourceUtils.getFile("classpath:application.properties")
        Path path = Paths.get("src/main/resources/APIC/GENERATED_FILES/"+api +".yml");

        //Check and create a directory -----
        File pathAsFile = new File(String.valueOf(path.toAbsolutePath().toString().replaceAll(api +".yml","")));
        if (!Files.exists(Paths.get(String.valueOf(path.toAbsolutePath())))) {
            pathAsFile.mkdir();
        }

        log.info("PATH ::"+path.toAbsolutePath());
        Files.write(path, Collections.singleton(yaml));


        return yaml;
    }


    void loadFilesToDataBase(String fileData, String version,String apiName,String envv) throws IOException {

        ApiDocsRecord record = new ApiDocsRecord();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        record.setVersion(version);
        record.setDocContent(fileData);
        record.setDocStatus("ACTIVE");
        record.setDocApiName(apiName);
        record.setDocType(envv);
        int id = 1;

        try {
            if(recordRepo!=null) {
                if(recordRepo.max()!=null && recordRepo.max().size() > 0)
                    id = recordRepo.max().size() + 1;
                else
                    id = 1;
            }else{
                log.info("RECORD REPO IS NULL");
            }
        }catch (Exception ex){
            ex.printStackTrace();
            log.info(ex.getMessage());
        }
        record.setId( id);
        record.setAuditDatetime(timestamp);

        recordRepo.save(record);

    }

}


--implementation 'com.google.code.gson:gson:2.8.6'


package com.ford.vdcc.scheduler.tool.apic.tools.util;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
@Slf4j
public class ZipUtility {

    public static void zip( HttpServletResponse response) throws IOException {
        Path path = Paths.get("src/main/resources/APIC/GENERATED_FILES/");
        log.info("PATH ::"+path.toAbsolutePath());

        List<File> files = Files.list(Paths.get(path.toAbsolutePath().toString()))
                .map(Path::toFile)
                .collect(Collectors.toList());

        //setting headers
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader("Content-Disposition", "attachment; filename=\"apic.zip\"");

        ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

        for (File file : files) {
            //new zip entry and copying inputstream with file to zipOutputStream, after all closing streams
            zipOutputStream.putNextEntry(new ZipEntry(file.getName()));
            FileInputStream fileInputStream = new FileInputStream(file);
            IOUtils.copy(fileInputStream, zipOutputStream);
            fileInputStream.close();
            zipOutputStream.closeEntry();
        }

        zipOutputStream.close();
    }

    public static void clearDirectory() throws IOException{
        Path path = Paths.get("src/main/resources/APIC/GENERATED_FILES/");
        log.info("PATH ::"+path.toAbsolutePath());

        List<File> files = Files.list(Paths.get(path.toAbsolutePath().toString()))
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : files) {
            Files.delete(file.toPath());
        }

    }
}


package com.ford.vdcc.scheduler.tool.apic.tools.repository;

import com.ford.vdcc.scheduler.tool.apic.tools.ApiDocsConfigProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiDocsConfigPropertyRepository extends JpaRepository<ApiDocsConfigProperty, Integer> {

    @Query("SELECT api from ApiDocsConfigProperty api where api.environment = ?1 or api.environment = 'ALL'")
    List<ApiDocsConfigProperty> fetchRecordsByEnvironment(String env);
}


package com.ford.vdcc.scheduler.tool.apic.tools.form;

import lombok.Data;

@Data
public class ApicFormData {
    private String environment;
    private String apiName;

}



package com.ford.vdcc.scheduler.tool.apic.tools.repository;

import com.ford.vdcc.scheduler.tool.apic.tools.ApiDocsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiDocsRecordRepository extends JpaRepository<ApiDocsRecord, String> {

    @Query(value = "SELECT api.id FROM ApiDocsRecord api")
    public List<Integer> max();

    @Query(value = "SELECT api.version FROM ApiDocsRecord api where api.version = ?1 ORDER BY api.version DESC ")
    public List<String> maxVersion(String api);

    @Query(value = "SELECT api FROM ApiDocsRecord api where api.docApiName = ?1")
    public List<ApiDocsRecord> findByDocApiName(String docApiName);

    @Query("SELECT api.version from ApiDocsRecord api ORDER BY api.version DESC ")
    List<String> fetchMaxVersion();

}



package com.ford.vdcc.scheduler.tool.apic.tools.repository;

import com.ford.vdcc.scheduler.tool.apic.tools.ApiMetaDataRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiMetaDataRecordRepository extends JpaRepository<ApiMetaDataRecord, String> {

    @Query(value = "SELECT max(api.id) FROM ApiMetaDataRecord api")
    public int max();

    @Query(value = "SELECT api.version FROM ApiMetaDataRecord api where api.version = ?1 ORDER BY api.version DESC ")
    public List<String> maxVersion(String api);

    @Query(value = "SELECT api FROM ApiMetaDataRecord api where api.docApiName = ?1")
    public List<ApiMetaDataRecord> findByDocApiName(String docApiName);

    @Query("SELECT api.version from ApiMetaDataRecord api ORDER BY api.version DESC ")
    List<String> fetchMaxVersion();

}



package com.ford.vdcc.scheduler.tool.apic.tools;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Table(name = "api_docs_config_properties")
@Entity
@Data
public class ApiDocsConfigProperty  {
    public ApiDocsConfigProperty() {
    }

    @Column(name = "environment", length = 20)
    private String environment;

    @Id
    private int id;

    @Column(name = "config_key", length = 250)
    private String configKey;

    @Lob
    @Column(name = "config_value")
    private String configValue;

    @Column(name = "api_key")
    private int apiKey;

    @Column(name = "config_status", length = 10)
    private String configStatus;

    @Column(name = "audit_datetime")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date auditDatetime;

}



package com.ford.vdcc.scheduler.tool.apic.tools;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Table(name = "api_docs_records")
@Entity
@Data
public class ApiDocsRecord {

    @Column(name = "iteration", length = 20)
    private String version;

    @Column(name = "id")
    @Id
    private int id;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "doc_api_name", length = 50)
    private String docApiName;

    @Lob
    @Column(name = "doc_content")
    private String docContent;

    @Column(name = "doc_status", length = 10)
    private String docStatus;

    @Column(name = "audit_datetime", nullable = false)
    private Timestamp auditDatetime;

}



package com.ford.vdcc.scheduler.tool.apic.tools;

import lombok.Data;

import javax.persistence.*;
import java.sql.Timestamp;

@Table(name = "api_metadata_records")
@Entity
@Data
public class ApiMetaDataRecord {

    @Column(name = "iteration", length = 20)
    private String version;

    @Column(name = "id")
    @Id
    private int id;

    @Column(name = "doc_type", length = 50)
    private String docType;

    @Column(name = "doc_api_name", length = 50)
    private String docApiName;

    @Lob
    @Column(name = "doc_content")
    private String docContent;

    @Column(name = "doc_status", length = 10)
    private String docStatus;

    @Column(name = "audit_datetime", nullable = false)
    private Timestamp auditDatetime;

}



package com.ford.vdcc.scheduler.tool.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
public class SqlConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String userdrivername;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.hikari.connection-timeout}")
    private int connTimeOut;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minIdle;

    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;

    @Value("${spring.datasource.hikari.idle-timeout}")
    private int idleTimeOut;

    @Value("${spring.datasource.hikari.max-lifetime}")
    private int maxLifeTime;

    @Bean
    public DataSource getSqlDataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(userdrivername);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaxLifetime(maxLifeTime);
        config.setIdleTimeout(idleTimeOut);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minIdle);
        config.setConnectionTimeout(connTimeOut);

        HikariDataSource ds = new HikariDataSource(config);
        return ds;
    }

    @Bean(name = "jdbcSqlTemplate")
    public JdbcTemplate getSqlJDBCTemplate() throws IOException {
        return new JdbcTemplate(getSqlDataSource());
    }
}



-- Inside Resources/template Folder

apic.html

<!DOCTYPE HTML>
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://thymeleaf.org">

<head>
    <title>APIC generator</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <style>
        table, th, td {
            border-collapse: collapse;
        }
        h1{
            font-size: 24px;
            color: blue;
        }

        table.center {
            margin-left: auto;
            margin-right: auto;
        }

        table{
            font-family: Verdana;
            font-size: small;
        }
        ul.a {
            list-style-type: circle;
            font-family: Verdana;
            font-size: small;
        }
        html{
            display: flex;
            flex-flow: row nowrap;
            justify-content: center;
            align-content: center;
            align-items: center;
            height:100%;
            margin: 0;
            padding: 0;
            background:#eeeeee;
        }
        div.center{
            margin-left: 50px;
            margin-right: auto;
            text-align: left;
            color: #606975;
            font-family: Verdana;
        }
        body {
            margin: 0;
            flex: 0 1 auto;
            align-self: auto;
            width: 100%;
            max-width: 800px;
            height: 100%;
            max-height: 800px;
            background:#f1edfa;

            -webkit-box-shadow: 0px 0px 96px 0px rgba(0,0,0,0.75);
            -moz-box-shadow: 0px 0px 96px 0px rgba(0,0,0,0.75);
            box-shadow: 0px 0px 96px 0px rgba(0,0,0,0.75);
        }
        input[type=button], input[type=submit], input[type=reset] {
            background-color: #458bed;
            border: none;
            color: white;
            padding: 16px 32px;
            text-decoration: none;
            margin: 2px 1px;
            cursor: pointer;

        }
    </style>
</head>
<body style="alignment: center">
<center>
    <h1>APIC Tool to Generate Swagger document</h1>

    <br>APIC DOCUMENT GENERATOR
    <br/>
    <div class="center">
        <ul class="a">
            <li>APIC Tool to Generate APIC Standard Swagger document depending on the Environment </li>
        </ul>
    </div>
    <br/>

    <form action="#" th:action="@{/loadApic}" th:object="${apicFormData}" method="post">
        <table>
            <tr>
                <th> Environment ?:</th>
            </tr>
            <tr>
                <td><input type="radio" th:field="*{environment}" value="DEV"/>DEV</td> </tr>
            <tr> <td><input type="radio" th:field="*{environment}" value="QA"/>QA</td> </tr>
            <tr><td><input type="radio" th:field="*{environment}" value="PROD"/>PROD</td>   </tr>
        </table>
        <br/>

        <table>
            <tr>
                <th> Select API ?:</th>
                <td>
                    <select id="apiName" name="apiName" th:field="*{apiName}">
                        <option th:field="*{apiName}" value="">SELECT VALUE</option>
                        <option th:field="*{apiName}" value="ALL">ALL</option>
                        <option th:field="*{apiName}" value="vdccapplicationmonitor_stagging">vdccapplicationmonitor</option>
                        <option th:field="*{apiName}" value="adminapi_stagging">adminapi</option>
                        <option th:field="*{apiName}" value="enrollmentdashapi_stagging">enrollmentdashapi</option>
                        <option th:field="*{apiName}" value="internalvaluedashapi_stagging">internalvaluedashapi</option>
                        <option th:field="*{apiName}" value="launchapi_stagging">launchapi</option>
                        <option th:field="*{apiName}" value="qualityscorecardapi_stagging">qualityscorecardapi</option>
                        <option th:field="*{apiName}" value="sitesearchapi_stagging">sitesearchapi</option>
                    </select>
                </td>
            </tr>
        </table>

        <p>
            <input  style="background-color:#4e9af1" type="submit" value="DOWNLOAD"/> <input style="background-color:#4e9af1" type="reset" value="Reset"/></p>
    </form>
</center>
</body>
</html>



--application.properties

server.port = 9124
loadMetaData=false
loadConfigData=false



id	environment	api_key	config_key	config_value	config_status	audit_datetime
1	DEV	6	vdccapplicationmonitor_stagging	monitoring-scheduler.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:49:58.950
2	DEV	5	adminapi_stagging	vdcc-adminapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:49:59.237
3	DEV	4	enrollmentdashapi_stagging	vdcc-enrollmentdashapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:49:59.470
4	DEV	3	internalvaluedashapi_stagging	vdcc-internalvaluedashapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:49:59.710
5	DEV	1	launchapi_stagging	vdccapis.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:49:59.947
6	DEV	2	qualityscorecardapi_stagging	vdccqualtyscorecardapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:00.233
7	DEV	7	sitesearchapi_stagging	vdcc-sitesearchapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:00.457
8	QA	6	vdccapplicationmonitor_stagging	monitoring-scheduler-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:00.677
9	QA	5	adminapi_stagging	vdcc-adminapi-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:00.917
10	QA	4	enrollmentdashapi_stagging	vdcc-enrollmentdashapi-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:01.153
11	QA	3	internalvaluedashapi_stagging	vdcc-internalvaluedashapi-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:01.393
12	QA	1	launchapi_stagging	vdccapis-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:01.627
13	QA	2	qualityscorecardapi_stagging	vdccqualtyscorecardapi-qa.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:01.877
14	QA	7	sitesearchapi_stagging	vdcc-sitesearchapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:02.107
15	PROD	6	vdccapplicationmonitor_stagging	monitoring-scheduler-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:02.360
16	PROD	5	adminapi_stagging	vdcc-adminapi-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:02.600
17	PROD	4	enrollmentdashapi_stagging	vdcc-enrollmentdashapi-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:02.833
18	PROD	3	internalvaluedashapi_stagging	vdcc-internalvaluedashapi-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:03.073
19	PROD	1	launchapi_stagging	vdccapis-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:03.307
20	PROD	2	qualityscorecardapi_stagging	vdccqualtyscorecardapi-prod.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:03.547
21	PROD	7	sitesearchapi_stagging	vdcc-sitesearchapi.apps.pp01i.edc1.cf.ford.com	Active	2021-10-19 09:50:03.783
22	DEV	0	endpointUrl	https://apigtwdev.ford.com/vdcc-52291/sb	Active	2021-10-19 09:50:04.013
23	QA	0	endpointUrl	https://apigtwqa.ford.com/oneford/api	Active	2021-10-19 09:50:04.247
24	PROD	0	endpointUrl	https://apigtw.ford.com/oneford/api	Active	2021-10-19 09:50:04.477
25	ALL	6	vdccapplicationmonitor_url	https://monitoring-scheduler.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:04.710
26	ALL	5	adminapi_url	https://vdcc-adminapi.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:04.950
27	ALL	4	enrollmentdashapi_url	https://vdcc-enrollmentdashapi.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:05.190
28	ALL	3	internalvaluedashapi_url	https://vdcc-internalvaluedashapi.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:05.433
29	ALL	1	launchapi_url	https://vdccapis.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:05.690
30	ALL	2	qualityscorecardapi_url	https://vdccqualtyscorecardapi.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:05.927
31	ALL	7	sitesearchapi_url	https://vdcc-sitesearchapi.apps.pp01i.edc1.cf.ford.com/v2/api-docs	Active	2021-10-19 09:50:06.450
32	DEV	0	apigtwkeyvalue	https://apigtwqa.ford.com/oneford/api	Active	2021-10-19 09:50:06.717
33	QA	0	apigtwkeyvalue	https://apigtwqa.ford.com/oneford/api	Active	2021-10-19 09:50:06.947
34	PROD	0	apigtwkeyvalue	https://apigtw.ford.com/oneford/api	Active	2021-10-19 09:50:07.170
35	DEV	0	apicorpkey	https://corpqa.sts.ford.com/adfs/oauth2/token	Active	2021-10-19 09:50:07.390
36	QA	0	apicorpkey	https://corpqa.sts.ford.com/adfs/oauth2/token	Active	2021-10-19 09:50:07.617
37	PROD	0	apicorpkey	https://corp.sts.ford.com/adfs/oauth2/token	Active	2021-10-19 09:50:07.853


id	iteration	doc_type	doc_api_name	doc_content	doc_status	audit_datetime
1	2.0.0.0	DEV	vdccapplicationmonitor_stagging	swagger: '2.0'
info:
  description: API Resources & Documentation
  version: 2.0.0.0
  title: vdccapplicationmonitor
  contact:
    name: hdoan3
  license:
    name: Ford license
  x-ibm-name: vdccapplicationmonitor_stagging
host: monitoring-scheduler.apps.pp01i.edc1.cf.ford.com
basePath: /api
tags:
- name: application-admin-event-controller
  description: Application Admin Event Controller
schemes:
- https
paths:
  /monitoring-api/app-status:
    post:
      tags:
      - application-admin-event-controller
      summary: Application App Status
      description: Application App Status
      operationId: notifyAPiUsingPOST
      consumes:
      - application/json
      produces:
      - '*/*'
      parameters:
      - in: body
        name: applicationEventRequestDTO
        description: applicationEventRequestDTO
        required: true
        schema:
          $ref: '#/definitions/ApplicationEventRequestDTO'
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/ApplicationEventResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
          schema:
            $ref: '#/definitions/GenericResponse'
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '405':
          description: Method not allowed
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
securityDefinitions:
  api-key-1:
    type: apiKey
    description: ''
    in: header
    name: X-IBM-Client-Id
  OAuth2:
    type: oauth2
    tokenUrl: https://corpqa.sts.ford.com/adfs/oauth2/token
    flow: application
    scopes: {}
    x-tokenIntrospect:
      url: http://127.0.0.1:9032/introspect?issuer=adfs
definitions:
  ApplicationErrorEventDTO:
    type: object
    required:
    - eventDetails
    - eventType
    properties:
      eventCode:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      eventDetails:
        type: string
        minLength: 0
        maxLength: 1000
      eventTime:
        type: string
      eventType:
        type: string
        enum:
        - COMPLETED
        - ERROR
        - IN_PROGRESS
        - MISSED_SCHEDULE
        - SUCCESS
        - TERMINATED_DUE_TO_ERROR
        - UNKNOWN_STATUS
    title: ApplicationErrorEventDTO
  ApplicationEventRequestDTO:
    type: object
    required:
    - applicationType
    properties:
      applicationCode:
        type: string
        minLength: 1
        maxLength: 20
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      applicationType:
        type: string
        enum:
        - API
        - SCHEDULER
        - UI
      eventDetails:
        $ref: '#/definitions/ApplicationErrorEventDTO'
    title: ApplicationEventRequestDTO
  ApplicationEventResponse:
    type: object
    properties:
      requestingApplication:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}/]*
      responseCode:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}/]*
      responseMessage:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}/]*
    title: ApplicationEventResponse
  GenericResponse:
    type: object
    properties:
      msg:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}/]*
      statusCode:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}/]*
    title: GenericResponse
security:
- OAuth2: []
  api-key-1: []
x-ibm-configuration:
  phase: realized
  enforced: true
  testable: true
  cors:
    enabled: true
  assembly:
    execute:
    - invoke:
        title: invoke
        timeout: 60
        verb: keep
        cache-response: protocol
        cache-ttl: 900
        stop-on-error:
        - null
        version: 1.0.0
        target-url: $(target-url)$(request.path)
    catch: []
  properties:
    target-url:
      value: https://monitoring-scheduler.apps.pp01i.edc1.cf.ford.com
      description: ''
      encoded: false
x-ibm-endpoints:
- endpointUrl: https://apigtwqa.ford.com/oneford/api
  type:
  - production
parameters:
  Authorization:
    name: Authorization
    type: string
    required: true
    in: header
    maxLength: 1024
	ACTIVE	2021-10-20 14:48:39.367
2	2.0.0.0	DEV	adminapi_stagging	swagger: '2.0'
info:
  description: API Resources & Documentation
  version: 2.0.0.0
  title: VDCCAdminApis
  contact:
    name: hdoan3
  license:
    name: Ford license
  x-ibm-name: adminapi_stagging
host: vdcc-adminapi.apps.pp01i.edc1.cf.ford.com
basePath: /api
tags:
- name: aps-controller
  description: Aps Controller
- name: dynamic-tiles-controller
  description: Dynamic Tiles Controller
- name: generic-controller
  description: Generic Controller
schemes:
- https
paths:
  /v1/aps/resources/{userid}:
    get:
      tags:
      - aps-controller
      summary: Get the resources
      description: Get the resources
      operationId: getApsResourcesUsingGET
      produces:
      - application/json
      parameters:
      - name: userid
        in: path
        description: userid
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/UserRolesDTO'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
          schema:
            $ref: '#/definitions/GenericResponse'
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '405':
          description: Method Not Allowed
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /v1/aps/roles/{userid}:
    get:
      tags:
      - aps-controller
      summary: Get the resources
      description: Get the resources
      operationId: getApsResponseUsingGET
      produces:
      - application/json
      parameters:
      - name: userid
        in: path
        description: userid
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/UserRolesDTO'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
          schema:
            $ref: '#/definitions/GenericResponse'
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '405':
          description: Method Not Allowed
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /v1/monitoringapi/appstatus:
    post:
      tags:
      - generic-controller
      summary: Application App Status
      description: Application App Status
      operationId: notifyUserUsingPOST
      consumes:
      - application/json
      produces:
      - application/json
      parameters:
      - in: body
        name: statusRequest
        description: statusRequest
        required: true
        schema:
          $ref: '#/definitions/StatusRequest'
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/GenericResponse'
  /v1/tiles/details/{screen}/{kpiName}/{program}/{fleet}/{team}:
    get:
      tags:
      - dynamic-tiles-controller
      summary: Get Tiles summary with program, fleet,team, screen
      description: Get Tiles data
      operationId: isDetailsAllowedUsingGET
      produces:
      - application/json
      parameters:
      - name: fleet
        in: path
        description: fleet
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: kpiName
        in: path
        description: kpiName
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: program
        in: path
        description: program
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: screen
        in: path
        description: screen
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: team
        in: path
        description: team
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/DynamicDetailsTileModel'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
          schema:
            $ref: '#/definitions/GenericResponse'
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '405':
          description: Method Not Allowed
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /v1/tiles/{screen}/{program}/{fleet}/{team}:
    get:
      tags:
      - dynamic-tiles-controller
      summary: Get Tiles summary with program, fleet,team, screen
      description: Get Tiles data
      operationId: getDyanamicTileModelUsingGET
      produces:
      - application/json
      parameters:
      - name: fleet
        in: path
        description: fleet
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: program
        in: path
        description: program
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: screen
        in: path
        description: screen
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - name: team
        in: path
        description: team
        required: true
        type: string
        maxLength: 50
        minLength: 1
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/DynamicTileModel'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
          schema:
            $ref: '#/definitions/GenericResponse'
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '405':
          description: Method Not Allowed
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
securityDefinitions:
  api-key-1:
    type: apiKey
    description: ''
    in: header
    name: X-IBM-Client-Id
  OAuth2:
    type: oauth2
    tokenUrl: https://corpqa.sts.ford.com/adfs/oauth2/token
    flow: application
    scopes: {}
    x-tokenIntrospect:
      url: http://127.0.0.1:9032/introspect?issuer=adfs
definitions:
  DynamicDetailsTileModel:
    type: object
    properties:
      allowed:
        type: boolean
    title: DynamicDetailsTileModel
  DynamicTileModel:
    type: object
    properties:
      status:
        type: boolean
      tilesResponse:
        type: array
        items:
          $ref: '#/definitions/TilesResponse'
        maxItems: 100
    title: DynamicTileModel
  EventDetails:
    type: object
    properties:
      eventCode:
        type: string
        minLength: 1
        maxLength: 20
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      eventDetails:
        type: string
        minLength: 1
        maxLength: 8000
        pattern: ^[0-9a-zA-Z-,:.'"\[\]\(\)&\/\s_&+äöüÄÖÜß/$^&*?#;{}"]*
      eventTime:
        type: string
        minLength: 23
        maxLength: 23
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      eventType:
        type: string
        minLength: 1
        maxLength: 30
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
    title: EventDetails
  GenericResponse:
    type: object
    properties:
      msg:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      statusCode:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: GenericResponse
  StatusRequest:
    type: object
    properties:
      applicationCode:
        type: string
        minLength: 1
        maxLength: 20
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      applicationType:
        type: string
        minLength: 1
        maxLength: 20
        pattern: ^[0-9a-zA-Z-,:\s_+&]+
      eventDetails:
        $ref: '#/definitions/EventDetails'
    title: StatusRequest
  TilesResponse:
    type: object
    properties:
      kpiName:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      tileId:
        type: integer
        format: int32
        minimum: 0
        maximum: 1024
        exclusiveMinimum: false
        exclusiveMaximum: false
      tileOrder:
        type: integer
        format: int32
        minimum: 0
        maximum: 1024
        exclusiveMinimum: false
        exclusiveMaximum: false
    title: TilesResponse
  UserRolesDTO:
    type: object
    properties:
      resources:
        type: array
        items:
          type: string
        maxItems: 100
      roles:
        type: array
        items:
          type: string
        maxItems: 100
    title: UserRolesDTO
security:
- OAuth2: []
  api-key-1: []
x-ibm-configuration:
  phase: realized
  enforced: true
  testable: true
  cors:
    enabled: true
  assembly:
    execute:
    - invoke:
        title: invoke
        timeout: 60
        verb: keep
        cache-response: protocol
        cache-ttl: 900
        stop-on-error:
        - null
        version: 1.0.0
        target-url: $(target-url)$(request.path)
    catch: []
  properties:
    target-url:
      value: https://vdcc-adminapi.apps.pp01i.edc1.cf.ford.com
      description: ''
      encoded: false
x-ibm-endpoints:
- endpointUrl: https://apigtwqa.ford.com/oneford/api
  type:
  - production
parameters:
  Authorization:
    name: Authorization
    type: string
    required: true
    in: header
    maxLength: 1024
	ACTIVE	2021-10-20 14:48:39.987
3	2.0.0.0	DEV	enrollmentdashapi_stagging	swagger: '2.0'
info:
  description: API Resources & Documentation
  version: 2.0.0.0
  title: VdccEnrollmentDashBoardApi
  contact:
    name: hdoan3
  license:
    name: Ford license
  x-ibm-name: enrollmentdashapi_stagging
host: vdcc-enrollmentdashapi.apps.pp01i.edc1.cf.ford.com
basePath: /api
tags:
- name: details-enrollment-data-controller
  description: Details Enrollment Data Controller
- name: eco-system-controller
  description: Eco System Controller
- name: eu-commercial-controller
  description: EU Commercial Controller
- name: fleet-enrollment-and-unrollment-controller
  description: Fleet Enrollment And Unrollment Controller
- name: incident-and-problem-mgmt-controller
  description: Incident And Problem Mgmt Controller
- name: power-metrics-controller
  description: Power Metrics Controller
- name: retail-enrollment-controller
  description: Retail Enrollment Controller
schemes:
- https
paths:
  /enroll-unenroll-details/download/{build}/{customerType}/{customerName}/{requestType}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: GET DOWNLOAD Data
      description: GET DOWNLOAD Data
      operationId: getDownloadDataUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: customerName
        in: path
        description: customerName
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: customerType
        in: path
        description: customerType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: requestType
        in: path
        description: requestType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            type: array
            items:
              type: object
              additionalProperties:
                type: object
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-details/filters/{customerType}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: Get Customer Names Filter
      description: Get Customer Names Filter
      operationId: getAllFiltersUsingGET_1
      produces:
      - application/json
      parameters:
      - name: customerType
        in: path
        description: customerType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            type: array
            items:
              type: string
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-details/{build}/{customerType}/{customerName}/{requestType}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: GET Enrollment And Unenrollment Detail Page
      description: GET Enrollment And Unenrollment Detail Page
      operationId: getDetailTilesUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: customerName
        in: path
        description: customerName
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: customerType
        in: path
        description: customerType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: requestType
        in: path
        description: requestType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentAndUnenrollmentDetailPageResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-statestics/{build}/{requestType}/{customerType}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: Get Enrollment UnEnrollment Response
      description: Get Enrollment UnEnrollment Response
      operationId: getEnrollmentAndUnEnrollmentStatasticsBasedOnCustomerTypeUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: customerType
        in: path
        description: customerType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: requestType
        in: path
        description: requestType
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentUnEnrollmentResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-statestics/{build}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: Get Enrollment UnEnrollment
      description: Get Enrollment UnEnrollment
      operationId: getEnrollmentAndUnEnrollmentStatasticsUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentUnEnrollmentResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-summary/failure/{requestType}/{build}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: Download failure records
      operationId: getAllFailureDataUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: requestType
        in: path
        description: requestType
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            type: array
            items:
              $ref: '#/definitions/EnrollmentAndUnenrollmentDownloadFailureResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enroll-unenroll-summary/filters:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: Get Filters
      description: Get filters
      operationId: getAllFiltersUsingGET_2
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            type: object
            additionalProperties:
              type: array
              items:
                $ref: '#/definitions/LabelIdModel'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /enroll-unenroll-summary/{build}/{startDate}/{endDate}:
    get:
      tags:
      - fleet-enrollment-and-unrollment-controller
      summary: GET Enrollment And Unenrollment Summary Response
      description: GET Enrollment And Unenrollment Summary Response
      operationId: getEnrollmentAndUnenrollmentSummaryUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentAndUnenrollmentSummaryResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enrolldash-mock/{startDate}/{endDate}:
    get:
      tags:
      - retail-enrollment-controller
      summary: Get Retail Enrollment Details Mock Data
      description: Get Retail Enrollment Details Mock Data
      operationId: getRetailEnrollmentDetailsMockDataUsingGET
      produces:
      - application/json
      parameters:
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/RetailEnrollmentDTO'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enrolldash/filters:
    get:
      tags:
      - retail-enrollment-controller
      summary: Get Filters
      description: Get filters
      operationId: getAllFiltersUsingGET_3
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            type: object
            additionalProperties:
              type: array
              items:
                $ref: '#/definitions/LabelIdModel'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /enrolldash/retail/download:
    get:
      tags:
      - retail-enrollment-controller
      summary: getFile
      operationId: getFileUsingGET
      produces:
      - '*/*'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/Resource'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /enrolldash/{build}/{requestType}/{tileType}/{startDate}/{endDate}:
    get:
      tags:
      - retail-enrollment-controller
      summary: Get Retail Enrollment Details With Tile Type
      description: Get Retail Enrollment Details With Tile Type
      operationId: getRetailEnrollmentDetailsWithTileTypeUsingGET
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: requestType
        in: path
        description: requestType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: tileType
        in: path
        description: tileType
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enrolldash/{build}/{startDate}/{endDate}:
    get:
      tags:
      - retail-enrollment-controller
      summary: Get Retail Enrollment Details With Build
      description: Get Retail Enrollment Details With Build
      operationId: getRetailEnrollmentDetailsWithBuildUsingGET_1
      produces:
      - application/json
      parameters:
      - name: build
        in: path
        description: build
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: endDate
        in: path
        description: endDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: startDate
        in: path
        description: startDate
        required: true
        type: string
        maxLength: 10
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EnrollmentResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enrollment/file/download:
    get:
      tags:
      - details-enrollment-data-controller
      summary: downloadFile
      operationId: downloadFileUsingGET
      produces:
      - '*/*'
      parameters:
      - name: dataKey
        in: query
        description: dataKey
        required: false
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            type: string
            format: byte
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /enrollment/file/download/{dataKey}:
    get:
      tags:
      - details-enrollment-data-controller
      summary: downloadFileWithPathVariable
      operationId: downloadFileWithPathVariableUsingGET
      produces:
      - '*/*'
      parameters:
      - name: dataKey
        in: path
        description: dataKey
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            type: string
            format: byte
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /env:
    get:
      tags:
      - retail-enrollment-controller
      summary: verifyEnv
      operationId: verifyEnvUsingGET
      produces:
      - '*/*'
      responses:
        '200':
          description: OK
          schema:
            type: string
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /eucommercial/filters:
    get:
      tags:
      - eu-commercial-controller
      summary: Get Filters
      description: Get filters
      operationId: getAllFiltersUsingGET
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            type: object
            additionalProperties:
              type: array
              items:
                $ref: '#/definitions/LabelIdModel'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /eucommercial/{vehicleprogram}/{country}/{auth}/{snapshotDate}:
    get:
      tags:
      - eu-commercial-controller
      summary: EU Commercial Response
      description: EU Commercial Response
      operationId: getRetailEnrollmentDetailsWithBuildUsingGET
      produces:
      - '*/*'
      parameters:
      - name: auth
        in: path
        description: auth
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: country
        in: path
        description: country
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: snapshotDate
        in: path
        description: snapshotDate
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^(null)||(\d{4}\-(0[1-9]|1[012])\-(0[1-9]|[12][0-9]|3[01])$)
      - name: vehicleprogram
        in: path
        description: vehicleprogram
        required: true
        type: string
        maxLength: 1000
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/EUCommercialResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /ipm/default:
    get:
      tags:
      - incident-and-problem-mgmt-controller
      summary: Get IPM Data for Default Group
      description: Get Incident and Problem Management Data for Ticket, Incident,
        Service Request and Problem tiles
      operationId: getIPMTileDataWithDefaultGroupUsingGET
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/IPMDTO'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /ipm/filters:
    get:
      tags:
      - incident-and-problem-mgmt-controller
      summary: Get Product Group Filters
      description: Get Product Group filters
      operationId: getProductGroupFiltersUsingGET
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/IPMFilters'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /ipm/workflow/{severity}:
    get:
      tags:
      - incident-and-problem-mgmt-controller
      summary: Get IPM Flow data
      description: Get IPM Flow data based on the severity
      operationId: getIPMFlowUsingGET
      produces:
      - application/json
      parameters:
      - name: severity
        in: path
        description: severity
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/IPMFlow'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /ipm/{tileId}/{productGroup}/{severity}:
    get:
      tags:
      - incident-and-problem-mgmt-controller
      summary: Get IPM Tile Data Based on Tile Id , Product Group and Severity
      description: Get IPM Tile Data Based on Tile Id , Product Group and Severity
      operationId: getIPMTileDataByTileAndGroupUsingGET
      produces:
      - application/json
      parameters:
      - name: productGroup
        in: path
        description: productGroup
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: severity
        in: path
        description: severity
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - name: tileId
        in: path
        description: tileId
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/IPMData'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /powerMetrics/details/{projectName}:
    get:
      tags:
      - power-metrics-controller
      summary: Get Power Metrics Details Page Data
      description: Get Power Metrics Details Page Data for a specific project
      operationId: getPowerMetricsDetailsPageUsingGET
      produces:
      - application/json
      parameters:
      - name: projectName
        in: path
        description: projectName
        required: true
        type: string
        maxLength: 100
        minLength: 0
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      - $ref: '#/parameters/Authorization'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/PowerMetricsDetailsPageResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
  /powerMetrics/summary:
    get:
      tags:
      - power-metrics-controller
      summary: Get Power Metrics Summary Data
      description: Get Power Metrics Summary Data
      operationId: getPowerMetricsDashboardUsingGET
      produces:
      - application/json
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/PowerMetricsDashboardResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
  /software-operation-health-dashboard:
    get:
      tags:
      - eco-system-controller
      summary: Get Software Operation Health DashBoard
      description: Get Software Operation Health DashBoard
      operationId: getSoftwareOperationHealthDashBoardUsingGET
      produces:
      - '*/*'
      responses:
        '200':
          description: OK
          schema:
            $ref: '#/definitions/SoftwareOperationDashboardResponse'
        '400':
          description: Bad request
          schema:
            $ref: '#/definitions/GenericResponse'
        '401':
          description: Unauthorized request
        '403':
          description: Forbidden access
          schema:
            $ref: '#/definitions/GenericResponse'
        '404':
          description: Not found resource
          schema:
            $ref: '#/definitions/GenericResponse'
        '429':
          description: Too many request
          schema:
            $ref: '#/definitions/GenericResponse'
        default:
          description: Default response
          schema:
            $ref: '#/definitions/GenericResponse'
      parameters:
      - $ref: '#/parameters/Authorization'
securityDefinitions:
  api-key-1:
    type: apiKey
    description: ''
    in: header
    name: X-IBM-Client-Id
  OAuth2:
    type: oauth2
    tokenUrl: https://corpqa.sts.ford.com/adfs/oauth2/token
    flow: application
    scopes: {}
    x-tokenIntrospect:
      url: http://127.0.0.1:9032/introspect?issuer=adfs
definitions:
  AverageTime:
    type: object
    properties:
      averageTime:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      changeInValue:
        type: boolean
      color:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      isGain:
        type: boolean
      productName:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      trend:
        type: number
        format: double
        minimum: -1000
        maximum: 1000
        exclusiveMinimum: false
        exclusiveMaximum: false
    title: AverageTime
  Categories:
    type: object
    properties:
      name:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      tiles:
        type: array
        items:
          $ref: '#/definitions/Tile'
        maxItems: 100
    title: Categories
  DetailTile:
    type: object
    properties:
      content:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      data:
        $ref: '#/definitions/TileData'
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      footer:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      header:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: DetailTile
  DonutChart:
    type: object
    properties:
      goal:
        type: integer
        format: int32
        minimum: -100
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      isGain:
        type: boolean
      percent:
        type: number
        format: double
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      title:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: DonutChart
  EUCommercialResponse:
    type: object
    properties:
      lastRun:
        type: string
      refreshPeriod:
        type: string
      status:
        $ref: '#/definitions/GenericResponse'
      tiles:
        type: array
        items:
          $ref: '#/definitions/EUCommercialTileInfo'
        maxItems: 100
    title: EUCommercialResponse
  EUCommercialTileDetails:
    type: object
    properties:
      labelValues:
        type: array
        items:
          $ref: '#/definitions/FleetActivationData'
        maxItems: 100
      logoIconClass:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      logoIconColor:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      milesDriven:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      milesDrivenLabel:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      tileTypeId:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      vehicleCount:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      vehicleLabel:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: EUCommercialTileDetails
  EUCommercialTileInfo:
    type: object
    properties:
      content:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      data:
        $ref: '#/definitions/EUCommercialTileDetails'
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      footer:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      header:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      tileId:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
    title: EUCommercialTileInfo
  EnrollmentAndUnEnrollment:
    type: object
    properties:
      detailTiles:
        type: array
        items:
          $ref: '#/definitions/DetailTile'
        maxItems: 100
    title: EnrollmentAndUnEnrollment
  EnrollmentAndUnenrollmentDetailPageResponse:
    type: object
    properties:
      averageTime:
        type: array
        items:
          $ref: '#/definitions/AverageTime'
        maxItems: 100
      enrollmentsSubmitted:
        type: array
        items:
          $ref: '#/definitions/EnrollmentsSubmitted'
        maxItems: 100
      status:
        $ref: '#/definitions/GenericResponse'
      successRate:
        type: array
        items:
          $ref: '#/definitions/SuccessRate'
        maxItems: 100
    title: EnrollmentAndUnenrollmentDetailPageResponse
  EnrollmentAndUnenrollmentDownloadFailureResponse:
    type: object
    properties:
      VIN:
        type: string
        minLength: 17
        maxLength: 17
        pattern: ^[0-9A-Za-z]*
      created_at:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      error_code:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      fulfillment_id:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      fulfillment_request_id:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      fulfillment_type:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      manager_name:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      message:
        type: string
        minLength: 0
        maxLength: 500
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      overall_state:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      state:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      updated_at:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: EnrollmentAndUnenrollmentDownloadFailureResponse
  EnrollmentAndUnenrollmentNode:
    type: object
    properties:
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      header:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      summaryTiles:
        type: array
        items:
          $ref: '#/definitions/SummaryTile'
        maxItems: 100
    title: EnrollmentAndUnenrollmentNode
  EnrollmentAndUnenrollmentSummaryResponse:
    type: object
    properties:
      enrollments:
        $ref: '#/definitions/EnrollmentAndUnenrollmentNode'
      status:
        $ref: '#/definitions/GenericResponse'
      unenrollments:
        $ref: '#/definitions/EnrollmentAndUnenrollmentNode'
    title: EnrollmentAndUnenrollmentSummaryResponse
  EnrollmentResponse:
    type: object
    properties:
      retailEnrollments:
        $ref: '#/definitions/RetailEnrollments'
      status:
        $ref: '#/definitions/GenericResponse'
    title: EnrollmentResponse
  EnrollmentUnEnrollmentResponse:
    type: object
    properties:
      enrollmentAndUnEnrollment:
        $ref: '#/definitions/EnrollmentAndUnEnrollment'
    title: EnrollmentUnEnrollmentResponse
  EnrollmentsSubmitted:
    type: object
    properties:
      enrollments:
        type: integer
        format: int64
        minimum: 0
        maximum: 100000
        exclusiveMinimum: false
        exclusiveMaximum: false
      productName:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: EnrollmentsSubmitted
  FleetActivationData:
    type: object
    properties:
      label:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      labelColor:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      percentile:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      value:
        type: integer
        format: int32
        minimum: 1
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
    title: FleetActivationData
  GenericResponse:
    type: object
    properties:
      msg:
        type: string
        minLength: 0
        maxLength: 10000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      statusCode:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: GenericResponse
  GraphData:
    type: object
    properties:
      data:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      date:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: GraphData
  IPMDTO:
    type: object
    properties:
      data:
        type: array
        items:
          $ref: '#/definitions/IPMData'
        maxItems: 100
      header:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      response:
        $ref: '#/definitions/GenericResponse'
      timestamp:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: IPMDTO
  IPMData:
    type: object
    properties:
      action:
        $ref: '#/definitions/LabelValueLink'
      control:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      managementDescription:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      managementType:
        $ref: '#/definitions/LabelIdModel'
      response:
        $ref: '#/definitions/GenericResponse'
      status:
        type: array
        items:
          $ref: '#/definitions/LabelValueLink'
        maxItems: 100
      tileId:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      trendDetails:
        $ref: '#/definitions/IPMTrend'
    title: IPMData
  IPMFilters:
    type: object
    properties:
      productGroup:
        type: array
        items:
          $ref: '#/definitions/LabelIdModel'
        maxItems: 100
      severity:
        type: array
        items:
          $ref: '#/definitions/LabelIdModel'
        maxItems: 100
    title: IPMFilters
  IPMFlow:
    type: object
    properties:
      data:
        $ref: '#/definitions/IPMFlowData'
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      header:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      response:
        $ref: '#/definitions/GenericResponse'
    title: IPMFlow
  IPMFlowData:
    type: object
    properties:
      details:
        $ref: '#/definitions/IPMFlowDetails'
      tiles:
        type: array
        items:
          $ref: '#/definitions/IPMFlowTile'
        maxItems: 100
    title: IPMFlowData
  IPMFlowDetails:
    type: object
    properties:
      incidentCompletedCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      incidentCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      linkedIssuesCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      mttaIndicatorValue:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      mttrIndicatorValue:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      permanentCorrectiveClosedCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      problemCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      problemMttrIndicatorvalue:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      serviceRequestCompletdCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      serviceRequestCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      ticketsCompletedCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      ticketsCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      totalIssuesCancelledCount:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: IPMFlowDetails
  IPMFlowTile:
    type: object
    properties:
      data:
        $ref: '#/definitions/IPMFlowTileData'
      header:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: IPMFlowTile
  IPMFlowTileData:
    type: object
    properties:
      apiQualityFlag:
        type: string
        minLength: 0
        maxLength: 5
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      count:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      days:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      slisloFlag:
        type: string
        minLength: 0
        maxLength: 5
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: IPMFlowTileData
  IPMTrend:
    type: object
    properties:
      trendArrow:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      trendColor:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      trendLabel:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      trendValue:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: IPMTrend
  InputStream:
    type: object
    title: InputStream
  LabelIdModel:
    type: object
    properties:
      id:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      label:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: LabelIdModel
  LabelValueLink:
    type: object
    properties:
      label:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      link:
        type: string
        minLength: 0
        maxLength: 200
        pattern: ^[0-9a-zA-Z-,:.'\(\)&\/\s_&+äöüÄÖÜß]*
      value:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: LabelValueLink
  PowerMetricDetailsPageData:
    type: object
    properties:
      breadth:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      dataSource:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      depth:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      engagementLabel:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      engagementTrend:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      engagementTrendColor:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      engagementValue:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      frequency:
        type: integer
        format: int32
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
      refreshRate:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: PowerMetricDetailsPageData
  PowerMetricsDashboardResponse:
    type: object
    properties:
      categoriesData:
        type: array
        items:
          $ref: '#/definitions/Categories'
        maxItems: 100
      header:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      response:
        $ref: '#/definitions/GenericResponse'
    title: PowerMetricsDashboardResponse
  PowerMetricsDetailsPageResponse:
    type: object
    properties:
      data:
        $ref: '#/definitions/PowerMetricDetailsPageData'
      header:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      headerTag:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      response:
        $ref: '#/definitions/GenericResponse'
    title: PowerMetricsDetailsPageResponse
  Resource:
    type: object
    properties:
      description:
        type: string
      file:
        type: file
      filename:
        type: string
      inputStream:
        $ref: '#/definitions/InputStream'
      open:
        type: boolean
      readable:
        type: boolean
      uri:
        type: string
        format: uri
      url:
        type: string
        format: url
    title: Resource
  RetailEnrollmentDTO:
    type: object
    properties:
      retailEnrollments:
        $ref: '#/definitions/RetailEnrollments'
    title: RetailEnrollmentDTO
  RetailEnrollments:
    type: object
    properties:
      detailTiles:
        type: array
        items:
          $ref: '#/definitions/DetailTile'
        maxItems: 100
      header:
        type: string
        minLength: 0
        maxLength: 500
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      summaryTiles:
        $ref: '#/definitions/RetailEnrollmentsSummaryTiles'
    title: RetailEnrollments
  RetailEnrollmentsSummaryTiles:
    type: object
    properties:
      enrollmentHeader:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      enrollmentTileDescription:
        $ref: '#/definitions/TileDescriptionDTO'
      enrollments:
        type: array
        items:
          $ref: '#/definitions/SummaryTile'
        maxItems: 100
      unenrollmentHeader:
        type: string
        minLength: 0
        maxLength: 50
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      unenrollmentTileDescription:
        $ref: '#/definitions/TileDescriptionDTO'
      unenrollments:
        type: array
        items:
          $ref: '#/definitions/SummaryTile'
        maxItems: 100
    title: RetailEnrollmentsSummaryTiles
  SoftwareOperationDashboardResponse:
    type: object
    properties:
      lastRun:
        type: string
      refreshPeriod:
        type: integer
        format: int32
      tiles:
        type: array
        items:
          $ref: '#/definitions/Tile'
        maxItems: 100
    title: SoftwareOperationDashboardResponse
  SuccessRate:
    type: object
    properties:
      color:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      productName:
        type: string
        minLength: 0
        maxLength: 100
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      successRate:
        type: number
        format: double
        minimum: 0
        maximum: 100
        exclusiveMinimum: false
        exclusiveMaximum: false
    title: SuccessRate
  SummaryTile:
    type: object
    properties:
      changeInValue:
        type: boolean
      color:
        type: string
        minLength: 0
        maxLength: 10
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      content:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      currentProgress:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      display_value_type:
        type: string
        minLength: 0
        maxLength: 20
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      failed:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      iconUri:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      isGain:
        type: boolean
      lastMonthProgress:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      submitted:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      success:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      title:
        type: string
        minLength: 0
        maxLength: 30
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
    title: SummaryTile
  Tile:
    type: object
    properties:
      content:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-zA-Z0-9:.\s<>-_$^&*?#;"\[\]{}]*
      data:
        $ref: '#/definitions/TileData'
      description:
        $ref: '#/definitions/TileDescriptionDTO'
      footer:
        type: string
        minLength: 0
        maxLength: 1000
        pattern: ^.{0}$|^[a-	ACTIVE	2021-10-20 14:48:40.790
        
        
id	iteration	doc_type	doc_api_name	doc_content	doc_status	audit_datetime
1	0	parameters	METADATA	{ "Authorization": { "name": "Authorization", "type": "string", "required": true, "in": "header", "maxLength": 1024 } }	ACTIVE	2021-10-20 14:23:01.017
2	0	x-ibm-endpoints	METADATA	[ { "endpointUrl": "apigtwkeyvalue", "type": [ "production" ] } ]	ACTIVE	2021-10-20 14:23:01.017
3	0	x-ibm-configuration	METADATA	{ "phase": "realized", "enforced": true, "testable": true, "cors": { "enabled": true }, "assembly": { "execute": [ { "invoke": { "title": "invoke", "timeout": 60, "verb": "keep", "cache-response": "protocol", "cache-ttl": 900, "stop-on-error": [ null ], "version": "1.0.0", "target-url": "$(target-url)$(request.path)" } } ], "catch": [] }, "properties": { "target-url": { "value": "targetvaluekey", "description": "", "encoded": false } } }	ACTIVE	2021-10-20 14:23:01.017
4	0	security	METADATA	[ { "OAuth2": [], "api-key-1": [] } ]	ACTIVE	2021-10-20 14:23:01.017
5	0	securityDefinitions	METADATA	{ "api-key-1": { "type": "apiKey", "description": "", "in": "header", "name": "X-IBM-Client-Id" }, "OAuth2": { "type": "oauth2", "tokenUrl": "apicorpkey", "flow": "application", "scopes": {}, "x-tokenIntrospect": { "url": "http://127.0.0.1:9032/introspect?issuer=adfs" } } }	ACTIVE	2021-10-20 14:23:01.017        
