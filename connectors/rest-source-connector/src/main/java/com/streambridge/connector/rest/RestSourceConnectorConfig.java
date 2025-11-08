package com.streambridge.connector.rest;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigDef.Width;

import java.util.Map;

/**
 * Configuration definition for the REST Source Connector.
 */
public class RestSourceConnectorConfig extends AbstractConfig {

    public static final String ENDPOINT_CONFIG = "rest.endpoint";
    private static final String ENDPOINT_DOC = "The REST API endpoint to poll";
    private static final String ENDPOINT_DISPLAY = "REST Endpoint";
    private static final String ENDPOINT_DEFAULT = "http://localhost:8080/api/data";

    public static final String POLL_INTERVAL_MS_CONFIG = "rest.poll.interval.ms";
    private static final String POLL_INTERVAL_MS_DOC = "The interval in milliseconds between polls";
    private static final String POLL_INTERVAL_MS_DISPLAY = "Poll Interval (ms)";
    private static final int POLL_INTERVAL_MS_DEFAULT = 60000;

    public static final String BATCH_SIZE_CONFIG = "rest.batch.size";
    private static final String BATCH_SIZE_DOC = "Maximum number of records to process per poll";
    private static final String BATCH_SIZE_DISPLAY = "Batch Size";
    private static final int BATCH_SIZE_DEFAULT = 100;

    public static final String TOPIC_CONFIG = "topic";
    private static final String TOPIC_DOC = "The Kafka topic to publish records to";
    private static final String TOPIC_DISPLAY = "Kafka Topic";
    private static final String TOPIC_DEFAULT = "rest-source-data";

    public static final String AUTH_TOKEN_CONFIG = "rest.auth.token";
    private static final String AUTH_TOKEN_DOC = "Authentication token for the REST API (optional)";
    private static final String AUTH_TOKEN_DISPLAY = "Auth Token";
    private static final String AUTH_TOKEN_DEFAULT = "";

    public static final ConfigDef CONFIG_DEF = new ConfigDef()
        .define(ENDPOINT_CONFIG, Type.STRING, ENDPOINT_DEFAULT, Importance.HIGH, ENDPOINT_DOC,
                "Connection", 1, Width.LONG, ENDPOINT_DISPLAY)
        .define(POLL_INTERVAL_MS_CONFIG, Type.INT, POLL_INTERVAL_MS_DEFAULT, Importance.MEDIUM,
                POLL_INTERVAL_MS_DOC, "Connection", 2, Width.SHORT, POLL_INTERVAL_MS_DISPLAY)
        .define(BATCH_SIZE_CONFIG, Type.INT, BATCH_SIZE_DEFAULT, Importance.MEDIUM, BATCH_SIZE_DOC,
                "Connection", 3, Width.SHORT, BATCH_SIZE_DISPLAY)
        .define(TOPIC_CONFIG, Type.STRING, TOPIC_DEFAULT, Importance.HIGH, TOPIC_DOC,
                "General", 1, Width.LONG, TOPIC_DISPLAY)
        .define(AUTH_TOKEN_CONFIG, Type.PASSWORD, AUTH_TOKEN_DEFAULT, Importance.MEDIUM,
                AUTH_TOKEN_DOC, "Connection", 4, Width.LONG, AUTH_TOKEN_DISPLAY);

    public RestSourceConnectorConfig(Map<String, String> props) {
        super(CONFIG_DEF, props);
    }
}
