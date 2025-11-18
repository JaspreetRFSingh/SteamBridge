package com.streambridge.connectors.elasticsearch;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.kafka.connect.sink.SinkTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

public class ElasticsearchSinkTask extends SinkTask {
    private static final Logger log = LoggerFactory.getLogger(ElasticsearchSinkTask.class);
    private ElasticsearchSinkConnectorConfig config;
    private int recordCount;

    @Override
    public String version() { return "1.0.0"; }

    @Override
    public void start(Map<String, String> props) {
        this.config = new ElasticsearchSinkConnectorConfig(props);
        this.recordCount = 0;
    }

    @Override
    public void put(Collection<SinkRecord> records) {
        for (SinkRecord record : records) {
            log.debug("Processing record from topic: {} offset: {}", record.topic(), record.kafkaOffset());
            recordCount++;
        }
        if (recordCount >= config.getInt(ElasticsearchSinkConnectorConfig.BATCH_SIZE)) {
            log.info("Flushing batch of {} records", recordCount);
            recordCount = 0;
        }
    }

    @Override
    public void flush(Map<TopicPartition, OffsetAndMetadata> offsets) {}

    @Override
    public void stop() { log.info("Stopping ES Sink Task. Processed: {}", recordCount); }
}
