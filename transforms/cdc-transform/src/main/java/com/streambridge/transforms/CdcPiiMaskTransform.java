package com.streambridge.transforms;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.ConnectRecord;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.transforms.Transformation;
import org.apache.kafka.connect.transforms.util.Requirements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.regex.Pattern;

public class CdcPiiMaskTransform<R extends ConnectRecord<R>> implements Transformation<R> {
    private static final Logger log = LoggerFactory.getLogger(CdcPiiMaskTransform.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.]+@[\\w.]+");
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\+?\\d{10,}");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\d{3}-\\d{2}-\\d{4}");
    private static final String MASKED = "***MASKED***";

    @Override
    public R apply(R record) {
        if (record.value() == null || !(record.value() instanceof Struct)) return record;
        Struct value = Requirements.requireStruct(record.value(), "PII masking");
        Struct masked = maskPiiFields(value);
        return record.newRecord(record.topic(), record.kafkaPartition(), record.keySchema(), record.key(), value.schema(), masked, record.timestamp());
    }

    private Struct maskPiiFields(Struct input) {
        Struct output = new Struct(input.schema());
        for (org.apache.kafka.connect.data.Field field : input.schema().fields()) {
            Object val = input.get(field);
            if (val instanceof String) {
                String s = (String) val;
                if (EMAIL_PATTERN.matcher(s).matches() || PHONE_PATTERN.matcher(s).matches() || SSN_PATTERN.matcher(s).matches()) {
                    output.put(field.name(), MASKED);
                } else { output.put(field.name(), val); }
            } else { output.put(field.name(), val); }
        }
        return output;
    }

    @Override public ConfigDef config() { return new ConfigDef(); }
    @Override public void close() {}
    @Override public void configure(Map<String, ?> configs) {}
}
