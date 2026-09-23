package com.bcnc.priceservice.infrastructure.adapter.in.web.exception;

import java.time.LocalDateTime;

/**
 * Cuerpo JSON estándar para cualquier respuesta de error de la API, construido por
 * {@link GlobalExceptionHandler}. Mantener un único formato de error en toda la API (en vez de
 * dejar que cada excepción serialice su propio {@code getMessage()} sin estructura) es lo que
 * hace que los errores sean predecibles para quien consuma el endpoint.
 *
 * @param status código de estado HTTP, duplicado aquí (además de en la cabecera) para que el
 *               cliente no tenga que inspeccionar la respuesta HTTP para saberlo
 * @param message descripción del error, en castellano, pensada para depuración
 * @param timestamp instante en que se generó el error, en UTC (ver {@link GlobalExceptionHandler})
 */
public record ErrorResponse(int status, String message, LocalDateTime timestamp) {
}
