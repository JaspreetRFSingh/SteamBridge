package com.streambridge.connectors.rest;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestSourceConnector extends SourceConnector {
    private static final Logger log = LoggerFactory.getLogger(RestSourceConnector.class);
    private Map<String, String> configProperties;

    @Override
    public String version() { return "1.0.0"; }

    @Override
    public void start(Map<String, String> props) {
        this.configProperties = props;
        log.info("Starting REST Source Connector");
    }

    @Override
    public Class<? extends Task> taskClass() { return RestSourceTask.class; }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> taskConfigs = new ArrayList<>();
        taskConfigs.add(new HashMap<>(configProperties));
        return taskConfigs;
    }

    @Override
    public void stop() { log.info("Stopping REST Source Connector"); }

    @Override
    public ConfigDef config() { return RestSourceConnectorConfig.conf(); }
}
