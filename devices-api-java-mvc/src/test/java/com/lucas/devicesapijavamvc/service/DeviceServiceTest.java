package com.lucas.devicesapijavamvc.service;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.kafka.DeviceEventPublisher;
import com.lucas.devicesapijavamvc.repository.DeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock
    private DeviceRepository repo;

    @Mock
    private DeviceEventPublisher eventPublisher;

    private DeviceService service;

    @BeforeEach
    void setUp() {
        service = new DeviceService(repo, Optional.of(eventPublisher));
    }

    @Test
    @DisplayName("create() should persist the device and publish the event")
    void createShouldSaveAndPublishEvent() {

        Device newDevice = Device.createNew("iPhone 15", "Apple", DeviceState.AVAILABLE);

        when(repo.save(any(Device.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Device.class));

        Device result = service.create(newDevice);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("iPhone 15");
        assertThat(result.getBrand()).isEqualTo("Apple");
        assertThat(result.getState()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(result.getExternalId()).isNotNull();
        assertThat(result.getCreationTime()).isNotNull();

        verify(repo).save(any(Device.class));
        verify(eventPublisher).publishCreated(result, "JAVA");
        verifyNoMoreInteractions(repo, eventPublisher);
    }

    @Test
    @DisplayName("get() should return the device when it exists")
    void getShouldReturnDeviceWhenFound() {

        Long id = 1L;
        Device device = Device.createNew("Galaxy S24", "Samsung", DeviceState.IN_USE);
        when(repo.findById(id)).thenReturn(Optional.of(device));

        Device result = service.get(id);

        assertThat(result).isSameAs(device);
        verify(repo).findById(id);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("get() should throw NotFoundException when device does not exist")
    void getShouldThrowWhenNotFound() {

        Long id = 42L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(DeviceService.NotFoundException.class, () -> service.get(id));

        verify(repo).findById(id);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("all() should delegate to the repository and return all devices")
    void allShouldReturnAllDevices() {

        List<Device> devices = List.of(
                Device.createNew("Pixel 9", "Google", DeviceState.AVAILABLE),
                Device.createNew("Moto Edge", "Motorola", DeviceState.RETIRED)
        );
        when(repo.findAll()).thenReturn(devices);

        List<Device> result = service.all();

        assertThat(result).hasSize(2).isEqualTo(devices);
        verify(repo).findAll();
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("byBrand() should return devices filtered by brand")
    void byBrandShouldFilterByBrand() {

        String brand = "Apple";
        List<Device> devices = List.of(
                Device.createNew("iPhone 14", "Apple", DeviceState.AVAILABLE),
                Device.createNew("iPhone 15", "Apple", DeviceState.IN_USE)
        );

        when(repo.findByBrand(brand)).thenReturn(devices);

        List<Device> result = service.byBrand(brand);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getBrand().equals("Apple"));

        verify(repo).findByBrand(brand);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("byState() should return devices filtered by state")
    void byStateShouldFilterByState() {

        DeviceState state = DeviceState.IN_USE;
        List<Device> devices = List.of(
                Device.createNew("iPhone 13", "Apple", state),
                Device.createNew("Galaxy S23", "Samsung", state)
        );

        when(repo.findByState(state)).thenReturn(devices);

        List<Device> result = service.byState(state);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(d -> d.getState() == state);

        verify(repo).findByState(state);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("updateFull() should update all fields and persist the device")
    void updateFullShouldUpdateAndSave() {

        Long id = 10L;
        Device existing = Device.createNew("Old Phone", "Nokia", DeviceState.RETIRED);

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(Device.class)))
                .thenAnswer(invocation -> invocation.getArgument(0, Device.class));

        String newName = "iPhone 16";
        String newBrand = "Apple";
        DeviceState newState = DeviceState.AVAILABLE;
        Device incoming = Device.createNew(newName, newBrand, newState);

        Device result = service.updateFull(id, incoming);

        assertThat(result.getName()).isEqualTo(newName);
        assertThat(result.getBrand()).isEqualTo(newBrand);
        assertThat(result.getState()).isEqualTo(newState);

        assertThat(result.getExternalId()).isEqualTo(existing.getExternalId());
        assertThat(result.getCreationTime()).isEqualTo(existing.getCreationTime());

        verify(repo).findById(id);
        verify(repo).save(any(Device.class));
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("delete() should remove the device when it exists and is not IN_USE")
    void deleteShouldRemoveDevice() {

        Long id = 99L;
        Device existing = Device.createNew("Moto G", "Motorola", DeviceState.AVAILABLE);
        when(repo.findById(id)).thenReturn(Optional.of(existing));

        service.delete(id);

        verify(repo).findById(id);
        verify(repo).delete(existing);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("delete() should throw NotFoundException when the device does not exist")
    void deleteShouldThrowWhenNotFound() {

        Long id = 100L;
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(DeviceService.NotFoundException.class, () -> service.delete(id));

        verify(repo).findById(id);
        verifyNoMoreInteractions(repo);
        verifyNoInteractions(eventPublisher);
    }
}
