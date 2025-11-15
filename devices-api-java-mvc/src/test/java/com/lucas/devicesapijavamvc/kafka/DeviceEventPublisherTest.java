package com.lucas.devicesapijavamvc.kafka;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class DeviceEventPublisherTest {

    private static final String TOPIC = "devices.events";

    @Mock
    KafkaTemplate<String, DeviceEvent> kafkaTemplate;

    @Test
    @DisplayName("publishCreated() should build DeviceEvent and send to Kafka with correct key")
    void publishCreatedShouldSendEvent() {

        Device device = Device.createNew(
                "iPhone 15",
                "Apple",
                DeviceState.AVAILABLE
        );
        OffsetDateTime creationTimeBefore = device.getCreationTime();

        DeviceEventPublisher publisher = new DeviceEventPublisher(kafkaTemplate, TOPIC);
        String origin = "JAVA";

        publisher.publishCreated(device, origin);


        ArgumentCaptor<DeviceEvent> eventCaptor = ArgumentCaptor.forClass(DeviceEvent.class);

        verify(kafkaTemplate).send(
                eq(TOPIC),
                eq(device.getExternalId().toString()),
                eventCaptor.capture()
        );
        verifyNoMoreInteractions(kafkaTemplate);

        DeviceEvent sent = eventCaptor.getValue();
        assertThat(sent).isNotNull();
        assertThat(sent.externalId()).isEqualTo(device.getExternalId());
        assertThat(sent.name()).isEqualTo("iPhone 15");
        assertThat(sent.brand()).isEqualTo("Apple");
        assertThat(sent.state()).isEqualTo(DeviceState.AVAILABLE.name());
        assertThat(sent.origin()).isEqualTo("JAVA");
        assertThat(sent.creationTime()).isEqualTo(creationTimeBefore);
    }
}
