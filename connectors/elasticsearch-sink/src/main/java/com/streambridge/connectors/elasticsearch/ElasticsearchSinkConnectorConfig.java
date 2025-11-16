package com.streambridge.connectors.elasticsearch;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import java.util.Map;

public class ElasticsearchSinkConnectorConfig extends AbstractConfig {
    public static final String CONNECTION_URL = "elasticsearch.connection.url";
    public static final String INDEX_NAME = "elasticsearch.index.name";
    public static final String BATCH_SIZE = "elasticsearch.batch.size";

    public static ConfigDef conf() {
        return new ConfigDef()
            .define(CONNECTION_URL, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "ES URL")
            .define(INDEX_NAME, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Index name")
            .define(BATCH_SIZE, ConfigDef.Type.INT, 1000, ConfigDef.Importance.MEDIUM, "Batch size");
    }

    public ElasticsearchSinkConnectorConfig(Map<String, String> props) { super(conf(), props); }
}
