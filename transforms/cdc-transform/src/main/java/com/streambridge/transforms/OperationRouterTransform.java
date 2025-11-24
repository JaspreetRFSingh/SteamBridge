package com.streambridge.transforms;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class OperationRouterTransform<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger log = LoggerFactory.getLogger(OperationRouterTransform.class);
    private static final String TOPIC_PREFIX_CONFIG = "topic.prefix";
    private String topicPrefix;

    @Override
    public R apply(R record) {
        if (record.value() == null || !(record.value() instanceof Struct)) return record;
        Struct value = (Struct) record.value();
        if (value.schema().field("op") != null) {
            String op = value.getString("op");
            String operation = "c".equals(op) ? "INSERT" : "u".equals(op) ? "UPDATE" : "d".equals(op) ? "DELETE" : "UNKNOWN";
            String newTopic = topicPrefix + "." + operation.toLowerCase();
            return record.newRecord(newTopic, record.kafkaPartition(), record.keySchema(), record.key(), record.valueSchema(), record.value(), record.timestamp());
        }
        return record;
    }

    @Override
    public ConfigDef config() {
        return new ConfigDef().define(TOPIC_PREFIX_CONFIG, ConfigDef.Type.STRING, ConfigDef.Importance.HIGH, "Topic prefix");
    }

    @Override public void close() {}
    @Override public void configure(Map<String, ?> configs) {
        this.topicPrefix = (String) configs.getOrDefault(TOPIC_PREFIX_CONFIG, "cdc");
    }
}
