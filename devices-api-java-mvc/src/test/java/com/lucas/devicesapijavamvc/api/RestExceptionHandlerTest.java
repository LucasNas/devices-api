package com.lucas.devicesapijavamvc.api;

import com.lucas.devicesapijavamvc.service.DeviceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RestExceptionHandlerTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("handleNotFound() should return 404 with NOT_FOUND body")
    void handleNotFoundShouldReturn404() {
        long id = 42L;
        String msg = "Device %d not found".formatted(id);
        DeviceService.NotFoundException ex = new DeviceService.NotFoundException(msg);

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> body = response.getBody();

        assertThat(body.get("error")).isEqualTo("NOT_FOUND");
        assertThat(body.get("message")).isEqualTo(msg);
    }

    @Test
    @DisplayName("handleValidation() should return 400 with VALIDATION_ERROR and details list")
    void handleValidationShouldReturn400() throws NoSuchMethodException {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "deviceRequest");

        bindingResult.addError(new FieldError(
                "deviceRequest", "name", "must not be blank"));
        bindingResult.addError(new FieldError(
                "deviceRequest", "brand", "must not be blank"));

        Method dummyMethod = Dummy.class.getDeclaredMethod("dummy", String.class);
        MethodParameter methodParameter = new MethodParameter(dummyMethod, 0);

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        Map<String, Object> body = response.getBody();

        assertThat(body.get("error")).isEqualTo("VALIDATION_ERROR");
        assertThat(body.get("message")).isEqualTo("Request validation failed");

        @SuppressWarnings("unchecked")
        List<String> details = (List<String>) body.get("details");
        assertThat(details).hasSize(2);
        assertThat(details.get(0)).contains("name");
        assertThat(details.get(1)).contains("brand");
    }

    static class Dummy {
        @SuppressWarnings("unused")
        public void dummy(String arg) {}
    }
}
