package com.lucas.devicesapijavamvc.kafka;

import com.lucas.devicesapijavamvc.domain.Device;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class DeviceEventPublisher {

    private final KafkaTemplate<String, DeviceEvent> template;
    private final String topic;

    public DeviceEventPublisher(
            KafkaTemplate<String, DeviceEvent> template,
            @Value("${kafka.topic.devices}") String topic
    ) {
        this.template = template;
        this.topic = topic;
    }

    public void publishCreated(Device device, String origin) {
        DeviceEvent event = new DeviceEvent(
                device.getExternalId(),
                device.getName(),
                device.getBrand(),
                device.getState().name(),
                device.getCreationTime(),
                origin
        );
        template.send(topic, device.getExternalId().toString(), event);
    }
}
