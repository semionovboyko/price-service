package com.bcnc.priceservice.infrastructure.adapter.in.web.exception;

import com.bcnc.priceservice.domain.exception.PriceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Traduce excepciones de dominio y de validación a respuestas HTTP. Vive en el adaptador web
 * ({@code infrastructure}): el dominio no sabe qué es un 404 ni un 400, eso es un concepto de
 * HTTP. Centralizar el manejo de errores aquí (en vez de {@code try/catch} repetido en cada
 * método del controller) evita duplicación y garantiza un formato de error consistente
 * ({@link ErrorResponse}) en toda la API.
 *
 * <p>{@code @RestControllerAdvice} combina {@code @ControllerAdvice} (intercepta excepciones
 * lanzadas por cualquier {@code @RestController} de la aplicación, no solo uno) con
 * {@code @ResponseBody} (serializa el valor de retorno directamente a JSON, igual que
 * {@code @RestController} hace en los controllers normales).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Traduce {@link PriceNotFoundException} (lanzada por
     * {@link com.bcnc.priceservice.application.service.PriceService} cuando no hay tarifa
     * aplicable) a un 404.
     *
     * @param ex la excepción de dominio capturada
     * @return 404 con el mensaje de la excepción
     */
    @ExceptionHandler(PriceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePriceNotFound(PriceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Agrupa las tres excepciones que Spring lanza cuando los parámetros de entrada no son
     * válidos: {@link ConstraintViolationException} (falla una anotación de {@code jakarta.validation},
     * p. ej. {@code @NotNull}), {@link MissingServletRequestParameterException} (falta un
     * {@code @RequestParam} obligatorio) y {@link MethodArgumentTypeMismatchException} (el valor
     * recibido no se puede convertir al tipo esperado, p. ej. una fecha mal formada).
     *
     * @param ex la excepción de validación capturada
     * @return 400 con un mensaje descriptivo
     */
    @ExceptionHandler({
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Parámetros de entrada inválidos: " + ex.getMessage());
    }

    /**
     * @param status código HTTP a devolver
     * @param message mensaje descriptivo del error
     * @return la respuesta ya construida, lista para devolver desde el {@code @ExceptionHandler}
     */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        // Zona explícita (UTC) evita que el timestamp del error dependa de la zona horaria por defecto de la JVM donde se despliegue.
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), message, LocalDateTime.now(ZoneOffset.UTC)));
    }
}