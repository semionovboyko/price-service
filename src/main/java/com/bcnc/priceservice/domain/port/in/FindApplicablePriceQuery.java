package com.bcnc.priceservice.domain.port.in;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Parámetros de entrada del caso de uso, encapsulados como value object inmutable en lugar de
 * pasar primitivos sueltos (evita "primitive obsession" y centraliza la validación en el dominio).
 *
 * @param brandId identificador de la cadena (p. ej. 1 = ZARA en el enunciado)
 * @param productId identificador del producto
 * @param applicationDate fecha/hora para la que se quiere resolver la tarifa aplicable
 */
public record FindApplicablePriceQuery(Long brandId, Long productId, LocalDateTime applicationDate) {

    /**
     * Constructor compacto: los {@code record} de Java lo ejecutan con los argumentos ya
     * asignados a los campos, pero antes de terminar de construir el objeto — es el punto
     * idiomático para validar invariantes sin reescribir el constructor completo a mano.
     * Falla rápido (fail-fast) con un {@link NullPointerException} explícito en vez de dejar
     * que él {@code null} se propague silenciosamente y falle más adelante en el flujo.
     */
    public FindApplicablePriceQuery {
        Objects.requireNonNull(brandId, "brandId no puede ser null");
        Objects.requireNonNull(productId, "productId no puede ser null");
        Objects.requireNonNull(applicationDate, "applicationDate no puede ser null");
    }
}
