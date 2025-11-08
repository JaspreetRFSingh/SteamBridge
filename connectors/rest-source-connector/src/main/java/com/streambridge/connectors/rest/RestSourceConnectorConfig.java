package com.streambridge.connectors.rest;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import java.util.Map;

public class RestSourceConnectorConfig extends AbstractConfig {
    public static final String ENDPOINT_URL = "rest.endpoint.url";
    public static final String POLL_INTERVAL_MS = "rest.poll.interval.ms";
    public static final String TOPIC_NAME = "rest.topic.name";

    public static ConfigDef conf() {
        return new ConfigDef()
            .define(ENDPOINT_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "REST API endpoint URL")
            .define(POLL_INTERVAL_MS, ConfigDef.Type.INT, 60000, ConfigDef.Importance.MEDIUM, "Poll interval in ms")
            .define(TOPIC_NAME, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Kafka topic name");
    }

    public RestSourceConnectorConfig(Map<String, String> props) { super(conf(), props); }
}
