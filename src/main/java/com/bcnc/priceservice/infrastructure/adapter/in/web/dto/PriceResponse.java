package com.bcnc.priceservice.infrastructure.adapter.in.web.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de salida del endpoint. Es un {@code record} propio, no el
 * {@link com.bcnc.priceservice.domain.model.Price} de dominio: el contrato REST no tiene por qué
 * coincidir siempre con el modelo interno, así un cambio en el dominio no rompe automáticamente
 * la API pública (y viceversa) — es el mismo principio de desacople que ya aplica
 * {@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.PriceEntity} en el lado
 * de persistencia.
 *
 * @param productId identificador del producto
 * @param brandId identificador de la cadena
 * @param priceList identificador de la tarifa aplicada
 * @param startDate inicio de vigencia de la tarifa aplicada
 * @param endDate fin de vigencia de la tarifa aplicada
 * @param price precio final de venta
 * @param currency código ISO 4217 de la moneda (p. ej. {@code "EUR"})
 */
public record PriceResponse(
        Long productId,
        Long brandId,
        Integer priceList,
        LocalDateTime startDate,
        LocalDateTime endDate,
        BigDecimal price,
        String currency) {
}
