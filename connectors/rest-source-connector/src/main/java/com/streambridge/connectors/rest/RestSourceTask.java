package com.streambridge.connectors.rest;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

public class RestSourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(RestSourceTask.class);
    private RestSourceConnectorConfig config;
    private HttpClient httpClient;
    private long lastPollTime;

    @Override
    public String version() { return "1.0.0"; }

    @Override
    public void start(Map<String, String> props) {
        this.config = new RestSourceConnectorConfig(props);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
        this.lastPollTime = System.currentTimeMillis();
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        long now = System.currentTimeMillis();
        long elapsed = now - lastPollTime;
        long pollInterval = config.getInt(RestSourceConnectorConfig.POLL_INTERVAL_MS);
        if (elapsed < pollInterval) { Thread.sleep(pollInterval - elapsed); }

        try {
            String endpoint = config.getString(RestSourceConnectorConfig.ENDPOINT_URL);
            String topic = config.getString(RestSourceConnectorConfig.TOPIC_NAME);
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).timeout(Duration.ofSeconds(30)).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Map<String, Object> sourcePartition = Collections.singletonMap("endpoint", endpoint);
                Map<String, Object> sourceOffset = Collections.singletonMap("timestamp", System.currentTimeMillis());
                lastPollTime = System.currentTimeMillis();
                return Collections.singletonList(new SourceRecord(sourcePartition, sourceOffset, topic, null, null, null, response.body()));
            }
        } catch (Exception e) { log.error("Error polling REST endpoint", e); }
        return Collections.emptyList();
    }

    @Override
    public void stop() { log.info("Stopping REST Source Task"); }
}
