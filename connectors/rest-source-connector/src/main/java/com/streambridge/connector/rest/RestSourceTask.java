package com.streambridge.connector.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Source Task that polls data from a REST API and produces Kafka records.
 * 
 * Demonstrates:
 * - Offset management for tracking progress
 * - Schema handling for Kafka records
 * - HTTP client integration
 */
public class RestSourceTask extends SourceTask {
    
    private static final Logger log = LoggerFactory.getLogger(RestSourceTask.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String OFFSET_KEY = "rest_offset";
    private static final String OFFSET_FIELD = "lastPollTimestamp";
    
    private RestSourceConnectorConfig config;
    private long lastPollTimestamp;
    private int recordCount;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting REST Source Task");
        this.config = new RestSourceConnectorConfig(props);
        
        // Recover offset from previous run
        Map<String, Object> offset = context.offsetStorageReader()
                .offset(Collections.singletonMap(OFFSET_KEY, "partition"));
        
        if (offset != null && offset.containsKey(OFFSET_FIELD)) {
            this.lastPollTimestamp = (Long) offset.get(OFFSET_FIELD);
            log.info("Recovered offset - last poll timestamp: {}", lastPollTimestamp);
        } else {
            this.lastPollTimestamp = 0;
            log.info("No offset found - starting fresh");
        }
        
        this.recordCount = 0;
        log.info("REST Source Task started");
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        log.debug("Polling REST endpoint: {}", config.getString(RestSourceConnectorConfig.ENDPOINT_CONFIG));
        
        try {
            List<SourceRecord> records = fetchDataFromApi();
            
            if (records.isEmpty()) {
                log.debug("No new data from API, waiting for next poll");
                Thread.sleep(config.getInt(RestSourceConnectorConfig.POLL_INTERVAL_MS_CONFIG));
                return Collections.emptyList();
            }
            
            // Update offset
            this.lastPollTimestamp = System.currentTimeMillis();
            
            log.info("Polled {} records from REST endpoint", records.size());
            return records;
            
        } catch (Exception e) {
            log.error("Error polling REST endpoint", e);
            // Return empty list on error - connector will retry on next poll
            Thread.sleep(config.getInt(RestSourceConnectorConfig.POLL_INTERVAL_MS_CONFIG));
            return Collections.emptyList();
        }
    }

    private List<SourceRecord> fetchDataFromApi() throws Exception {
        List<SourceRecord> records = new ArrayList<>();
        
        String endpoint = config.getString(RestSourceConnectorConfig.ENDPOINT_CONFIG);
        String topic = config.getString(RestSourceConnectorConfig.TOPIC_CONFIG);
        int batchSize = config.getInt(RestSourceConnectorConfig.BATCH_SIZE_CONFIG);
        
        // Create HTTP connection
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(30000);
        
        // Add auth token if configured
        String authToken = config.getPassword(RestSourceConnectorConfig.AUTH_TOKEN_CONFIG).value();
        if (authToken != null && !authToken.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + authToken);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            log.warn("Received non-OK response: {}", responseCode);
            return records;
        }
        
        // Parse response
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // Parse JSON array
        JsonNode rootNode = objectMapper.readTree(response.toString());
        if (!rootNode.isArray()) {
            log.warn("Expected JSON array but got: {}", rootNode.getNodeType());
            return records;
        }
        
        // Convert to SourceRecords
        int count = 0;
        for (JsonNode item : rootNode) {
            if (count >= batchSize) {
                break;
            }
            
            SourceRecord record = convertToSourceRecord(topic, item);
            records.add(record);
            count++;
        }
        
        return records;
    }

    private SourceRecord convertToSourceRecord(String topic, JsonNode data) {
        // Create source partition (identifies the data source)
        Map<String, String> sourcePartition = Collections.singletonMap(OFFSET_KEY, "partition");
        
        // Create source offset (tracks position in the data source)
        Map<String, Long> sourceOffset = Collections.singletonMap(OFFSET_FIELD, lastPollTimestamp);
        
        // Define schema for the record value
        Schema valueSchema = buildSchemaFromJson(data);
        
        // Create struct from JSON data
        Struct value = buildStructFromJson(valueSchema, data);
        
        // Create record key schema and struct
        Schema keySchema = SchemaBuilder.struct()
                .field("id", Schema.STRING_SCHEMA)
                .build();
        
        Struct key = new Struct(keySchema)
                .put("id", data.has("id") ? data.get("id").asText() : String.valueOf(recordCount++));
        
        return new SourceRecord(
                sourcePartition,
                sourceOffset,
                topic,
                0, // partition
                keySchema,
                key,
                valueSchema,
                value,
                Instant.now().toEpochMilli()
        );
    }

    private Schema buildSchemaFromJson(JsonNode node) {
        SchemaBuilder builder = SchemaBuilder.struct().name("com.streambridge.rest.Record");
        
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fieldNames().forEachRemaining(fieldName -> {
                JsonNode fieldValue = node.get(fieldName);
                if (fieldValue.isTextual()) {
                    builder.field(fieldName, Schema.STRING_SCHEMA);
                } else if (fieldValue.isInt()) {
                    builder.field(fieldName, Schema.INT32_SCHEMA);
                } else if (fieldValue.isLong()) {
                    builder.field(fieldName, Schema.INT64_SCHEMA);
                } else if (fieldValue.isDouble() || fieldValue.isFloat()) {
                    builder.field(fieldName, Schema.FLOAT64_SCHEMA);
                } else if (fieldValue.isBoolean()) {
                    builder.field(fieldName, Schema.BOOLEAN_SCHEMA);
                } else {
                    builder.field(fieldName, Schema.STRING_SCHEMA);
                }
            });
        }
        
        return builder.build();
    }

    private Struct buildStructFromJson(Schema schema, JsonNode node) {
        Struct struct = new Struct(schema);
        
        for (org.apache.kafka.connect.data.Field field : schema.fields()) {
            JsonNode fieldValue = node.get(field.name());
            if (fieldValue != null) {
                if (fieldValue.isTextual()) {
                    struct.put(field.name(), fieldValue.asText());
                } else if (fieldValue.isInt()) {
                    struct.put(field.name(), fieldValue.asInt());
                } else if (fieldValue.isLong()) {
                    struct.put(field.name(), fieldValue.asLong());
                } else if (fieldValue.isDouble() || fieldValue.isFloat()) {
                    struct.put(field.name(), fieldValue.asDouble());
                } else if (fieldValue.isBoolean()) {
                    struct.put(field.name(), fieldValue.asBoolean());
                }
            }
        }
        
        return struct;
    }

    @Override
    public void stop() {
        log.info("Stopping REST Source Task. Total records produced: {}", recordCount);
    }
}
