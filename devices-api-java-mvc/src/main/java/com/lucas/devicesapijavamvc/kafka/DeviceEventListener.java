package com.lucas.devicesapijavamvc.kafka;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.repository.DeviceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "kafka.enabled", havingValue = "true")
public class DeviceEventListener {

    private final DeviceRepository repo;

    public DeviceEventListener(DeviceRepository repo) {
        this.repo = repo;
    }

    @KafkaListener(
            topics = "${kafka.topic.devices}",
            groupId = "java-devices",
            containerFactory = "deviceEventKafkaListenerFactory"
    )
    public void onDeviceEvent(DeviceEvent event) {
        try {
            if ("JAVA".equals(event.origin())) {
                return; // ignora eventos próprios
            }

            var existingOpt = repo.findByExternalId(event.externalId());
            if (existingOpt.isPresent()) {
                var existing = existingOpt.get();
                existing.setName(event.name());
                existing.setBrand(event.brand());
                existing.setState(DeviceState.valueOf(event.state()));
                repo.save(existing);
            } else {
                Device mirrored = Device.fromEvent(
                        event.externalId(),
                        event.name(),
                        event.brand(),
                        DeviceState.valueOf(event.state()),
                        event.creationTime()
                );
                repo.save(mirrored);
            }
        } catch (Exception ex) {
            // log simples para challenge
            System.err.println("Failed to process DeviceEvent in Java: " + event);
            ex.printStackTrace();
        }
    }
}
