package com.streambridge.connectors.rest;
import org.apache.kafka.common.config.ConfigException;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RestSourceConnectorConfigTest {
    @Test
    void shouldCreateValidConfig() {
        Map<String, String> props = new HashMap<>();
        props.put(RestSourceConnectorConfig.ENDPOINT_URL, "http://api.example.com/data");
        props.put(RestSourceConnectorConfig.TOPIC_NAME, "rest-topic");
        RestSourceConnectorConfig config = new RestSourceConnectorConfig(props);
        assertEquals("http://api.example.com/data", config.getString(RestSourceConnectorConfig.ENDPOINT_URL));
    }

    @Test
    void shouldRejectMissingEndpointUrl() {
        Map<String, String> props = new HashMap<>();
        props.put(RestSourceConnectorConfig.TOPIC_NAME, "rest-topic");
        assertThrows(ConfigException.class, () -> new RestSourceConnectorConfig(props));
    }
}
