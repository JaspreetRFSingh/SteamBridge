package com.streambridge.connectors.elasticsearch;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.sink.SinkConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ElasticsearchSinkConnector extends SinkConnector {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSinkConnector.class);
    private Map<String, String> configProperties;

    @Override
    public String version() { return "1.0.0"; }

    @Override
    public void start(Map<String, String> props) { this.configProperties = props; }

    @Override
    public Class<? extends Task> taskClass() { return ElasticsearchSinkTask.class; }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        List<Map<String, String>> configs = new ArrayList<>();
        for (int i = 0; i < maxTasks; i++) configs.add(new HashMap<>(configProperties));
        return configs;
    }

    @Override
    public void stop() { log.info("Stopping Elasticsearch Sink Connector"); }

    @Override
    public ConfigDef config() { return ElasticsearchSinkConnectorConfig.conf(); }
}
