package com.lucas.devicesapikotlinreactive.infrastructure.api

import com.lucas.devicesapikotlinreactive.application.service.DeviceService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(DeviceService.NotFoundException::class)
    fun handleNotFound(ex: DeviceService.NotFoundException): ResponseEntity<Map<String, Any?>> {
        val body = mapOf(
            "error" to "NOT_FOUND",
            "message" to ex.message
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any?>> {
        val details = ex.bindingResult.fieldErrors
            .map { "${it.field}: ${it.defaultMessage}" }

        val body = mapOf(
            "error" to "VALIDATION_ERROR",
            "message" to "Request validation failed",
            "details" to details
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body)
    }
}
