package com.lucas.devicesapijavamvc.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucas.devicesapijavamvc.api.dto.DeviceRequest;
import com.lucas.devicesapijavamvc.domain.Device;
import com.lucas.devicesapijavamvc.domain.DeviceState;
import com.lucas.devicesapijavamvc.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DeviceController.class)
class DeviceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeviceService deviceService;

    private Device device(Long id,
                          UUID externalId,
                          String name,
                          String brand,
                          DeviceState state,
                          OffsetDateTime creationTime) {

        Device d = Device.fromEvent(
                externalId,
                name,
                brand,
                state,
                creationTime
        );
        ReflectionTestUtils.setField(d, "id", id);
        return d;
    }

    private Device device(Long id, String name, String brand, DeviceState state) {
        UUID externalId = UUID.randomUUID();
        OffsetDateTime creationTime = OffsetDateTime.now();
        return device(id, externalId, name, brand, state, creationTime);
    }

    @Test
    @DisplayName("POST /api/devices - creates device and returns 201 with body")
    void createDevice() throws Exception {
        DeviceRequest request = new DeviceRequest("Phone", "Apple", DeviceState.AVAILABLE);

        Device saved = device(
                1L,
                UUID.randomUUID(),
                request.name(),
                request.brand(),
                request.state(),
                OffsetDateTime.now()
        );

        when(deviceService.create(any(Device.class)))
                .thenReturn(saved);

        mockMvc.perform(post("/api/devices")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(saved.getId().intValue())))
                .andExpect(jsonPath("$.name", is(saved.getName())))
                .andExpect(jsonPath("$.brand", is(saved.getBrand())))
                .andExpect(jsonPath("$.state", is(saved.getState().name())));

        verify(deviceService).create(any(Device.class));
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/devices/{id} - returns device when found")
    void getDevice() throws Exception {
        Device device = device(1L, "Router", "Cisco", DeviceState.IN_USE);

        when(deviceService.get(1L)).thenReturn(device);

        mockMvc.perform(get("/api/devices/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Router")))
                .andExpect(jsonPath("$.brand", is("Cisco")))
                .andExpect(jsonPath("$.state", is("IN_USE")));

        verify(deviceService).get(1L);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/devices/{id} - returns 404 when not found")
    void getDeviceNotFound() throws Exception {
        when(deviceService.get(999L))
                .thenThrow(new DeviceService.NotFoundException("Device 999 not found"));

        mockMvc.perform(get("/api/devices/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", is("Device 999 not found")));

        verify(deviceService).get(999L);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/devices - returns all devices when no filters")
    void listAllDevices() throws Exception {
        List<Device> devices = List.of(
                device(1L, "Phone", "Apple", DeviceState.AVAILABLE),
                device(2L, "Tablet", "Samsung", DeviceState.RETIRED)
        );

        when(deviceService.all()).thenReturn(devices);

        mockMvc.perform(get("/api/devices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Phone")))
                .andExpect(jsonPath("$[1].name", is("Tablet")));

        verify(deviceService).all();
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/devices?brand= - filters by brand")
    void listByBrand() throws Exception {
        List<Device> devices = List.of(
                device(1L, "Phone", "Apple", DeviceState.AVAILABLE)
        );

        when(deviceService.byBrand("Apple")).thenReturn(devices);

        mockMvc.perform(get("/api/devices")
                        .param("brand", "Apple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand", is("Apple")));

        verify(deviceService).byBrand("Apple");
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("GET /api/devices?state= - filters by state")
    void listByState() throws Exception {
        List<Device> devices = List.of(
                device(1L, "Phone", "Apple", DeviceState.AVAILABLE)
        );

        when(deviceService.byState(DeviceState.AVAILABLE)).thenReturn(devices);

        mockMvc.perform(get("/api/devices")
                        .param("state", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].state", is("AVAILABLE")));

        verify(deviceService).byState(DeviceState.AVAILABLE);
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("PUT /api/devices/{id} - full update")
    void fullUpdate() throws Exception {
        DeviceRequest request = new DeviceRequest("New Phone", "Apple", DeviceState.IN_USE);

        Device updated = device(
                1L,
                UUID.randomUUID(),
                request.name(),
                request.brand(),
                request.state(),
                OffsetDateTime.now()
        );

        when(deviceService.updateFull(eq(1L), any(Device.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/devices/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Phone")))
                .andExpect(jsonPath("$.state", is("IN_USE")));

        verify(deviceService).updateFull(eq(1L), any(Device.class));
        verifyNoMoreInteractions(deviceService);
    }

    @Test
    @DisplayName("DELETE /api/devices/{id} - deletes device")
    void deleteDevice() throws Exception {
        doNothing().when(deviceService).delete(1L);

        mockMvc.perform(delete("/api/devices/1"))
                .andExpect(status().isNoContent());

        verify(deviceService).delete(1L);
        verifyNoMoreInteractions(deviceService);
    }
}
