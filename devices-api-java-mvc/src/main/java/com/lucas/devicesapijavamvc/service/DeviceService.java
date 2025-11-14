package com.lucas.devicesapijavamvc.service;

import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DeviceService {

    private final DeviceRepository repo;

    public DeviceService(DeviceRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Device create(Device newDevice) {
        return repo.save(newDevice);
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

        existing.setName(incoming.getName());
        existing.setBrand(incoming.getBrand());
        existing.setState(incoming.getState());

        return repo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        var existing = get(id);
        repo.delete(existing);
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
