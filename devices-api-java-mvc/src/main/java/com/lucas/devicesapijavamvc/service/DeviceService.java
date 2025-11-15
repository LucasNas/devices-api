package com.lucas.devicesapijavamvc.service;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.kafka.DeviceEventPublisher;
import com.lucas.devicesapijavamvc.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {

    private final DeviceRepository repo;
    private final Optional<DeviceEventPublisher> publisher;

    public DeviceService(DeviceRepository repo,
                         Optional<DeviceEventPublisher> publisher) {
        this.repo = repo;
        this.publisher = publisher;
    }

    @Transactional
    public Device create(Device newDevice) {
        Device saved = repo.save(newDevice);
        publisher.ifPresent(p -> p.publishCreated(saved, "JAVA"));
        return saved;
    }

    @Transactional(readOnly = true)
    public Device get(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Device %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Device> all() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public List<Device> byBrand(String brand) {
        return repo.findByBrand(brand);
    }

    @Transactional(readOnly = true)
    public List<Device> byState(DeviceState state) {
        return repo.findByState(state);
    }

    @Transactional
    public Device updateFull(Long id, Device incoming) {
        var existing = get(id);

        // Domain rule: name/brand cannot be changed if IN_USE
        if (existing.getState() == DeviceState.IN_USE) {
            boolean nameChanged = !existing.getName().equals(incoming.getName());
            boolean brandChanged = !existing.getBrand().equals(incoming.getBrand());
            if (nameChanged || brandChanged) {
                throw new IllegalStateException("Name and brand cannot be updated when device is in use");
            }
        }

        existing.setName(incoming.getName());
        existing.setBrand(incoming.getBrand());
        existing.setState(incoming.getState());

        // creationTime and externalId remain unchanged
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        var existing = get(id);

        // Domain rule: IN_USE devices cannot be deleted
        if (existing.getState() == DeviceState.IN_USE) {
            throw new IllegalStateException("Cannot delete device that is in use");
        }

        repo.delete(existing);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
