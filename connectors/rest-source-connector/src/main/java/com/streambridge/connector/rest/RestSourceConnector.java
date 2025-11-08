package com.streambridge.connector.rest;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom Kafka Connect Source Connector that polls data from REST APIs.
 * 
 * This connector demonstrates understanding of:
 * - Connector/Task/Worker lifecycle
 * - Configuration management
 * - Task distribution
 */
public class RestSourceConnector extends SourceConnector {
    
    private static final Logger log = LoggerFactory.getLogger(RestSourceConnector.class);
    
    private Map<String, String> configProperties;
    private RestSourceConnectorConfig config;

    @Override
    public String version() {
        return "1.0.0";
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting REST Source Connector");
        this.configProperties = new HashMap<>(props);
        this.config = new RestSourceConnectorConfig(props);
        log.info("REST Source Connector started with endpoint: {}", config.getString(RestSourceConnectorConfig.ENDPOINT_CONFIG));
    }

    @Override
    public Class<? extends Task> taskClass() {
        return RestSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        log.info("Generating task configurations for {} tasks", maxTasks);
        
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        
        // For a REST API connector, we typically run a single task
        // to avoid duplicate polling. Multiple tasks could be used
        // for partitioning by endpoint or query parameters.
        Map<String, String> taskProps = new HashMap<>(configProperties);
        taskConfigs.add(taskProps);
        
        log.info("Generated {} task configuration(s)", taskConfigs.size());
        return taskConfigs;
    }

    @Override
    public void stop() {
        log.info("Stopping REST Source Connector");
        configProperties = null;
        config = null;
        log.info("REST Source Connector stopped");
    }

    @Override
    public ConfigDef config() {
        return RestSourceConnectorConfig.CONFIG_DEF;
    }
}
