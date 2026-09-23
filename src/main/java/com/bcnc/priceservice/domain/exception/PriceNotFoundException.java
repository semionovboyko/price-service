package com.bcnc.priceservice.domain.exception;

import java.time.LocalDateTime;

/**
 * Excepción de dominio: se lanza cuando no existe ninguna tarifa aplicable para los parámetros
 * dados. Es <i>unchecked</i> (extiende {@link RuntimeException}) a propósito, para no obligar a
 * {@link com.bcnc.priceservice.domain.port.out.PriceRepositoryPort} ni a
 * {@link com.bcnc.priceservice.application.service.PriceService} a declarar {@code throws} solo
 * para dejarla pasar; la captura quien realmente la necesita:
 * {@link com.bcnc.priceservice.infrastructure.adapter.in.web.exception.GlobalExceptionHandler},
 * que la traduce a un 404 HTTP.
 */
public class PriceNotFoundException extends RuntimeException {

    /**
     * @param brandId identificador de la cadena consultada
     * @param productId identificador del producto consultado
     * @param applicationDate fecha/hora para la que no se encontró tarifa aplicable
     */
    public PriceNotFoundException(Long brandId, Long productId, LocalDateTime applicationDate) {
        super("No se ha encontrado tarifa aplicable para brandId=%d, productId=%d, fecha=%s"
                .formatted(brandId, productId, applicationDate));
    }
}
