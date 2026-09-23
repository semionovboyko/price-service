package com.bcnc.priceservice.domain.model;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitario puro (sin Spring, sin base de datos) de {@link Price}. Ejercita
 * {@link Price#appliesAt(LocalDateTime)}, la regla de negocio de dominio que determina si una
 * tarifa está vigente en una fecha dada.
 *
 * <p>En el flujo real de la aplicación, el filtrado por rango de fechas lo hace la consulta JPQL
 * de {@link com.bcnc.priceservice.infrastructure.adapter.out.persistence.PriceJpaRepository}
 * directamente en base de datos (con {@code BETWEEN}), por eficiencia de extracción: así se evita
 * traer candidatos a memoria para filtrarlos con Java. Por eso {@code appliesAt} no aparece
 * invocado desde ningún otro punto del código de producción.
 *
 * <p>Aun así, el método se mantiene en {@link Price} porque expresa una regla de negocio propia
 * del dominio ("¿esta tarifa está vigente en este instante?"), independiente de cómo se resuelva
 * la consulta en la capa de persistencia. Este test es lo que la mantiene viva y verificada de
 * forma aislada, sin depender de Spring ni de H2.
 */
class PriceTest {

    private Price priceWithRange(LocalDateTime startDate, LocalDateTime endDate) {
        return Price.builder()
                .brandId(1L)
                .startDate(startDate)
                .endDate(endDate)
                .priceList(1)
                .productId(35455L)
                .priority(0)
                .amount(BigDecimal.valueOf(35.50))
                .currency(Currency.getInstance("EUR"))
                .build();
    }

    @Test
    @DisplayName("appliesAt devuelve true cuando la fecha está estrictamente dentro del rango")
    void appliesAt_returnsTrue_whenDateIsStrictlyInsideRange() {
        Price price = priceWithRange(
                LocalDateTime.of(2020, 6, 14, 0, 0, 0),
                LocalDateTime.of(2020, 6, 14, 23, 59, 59));

        assertThat(price.appliesAt(LocalDateTime.of(2020, 6, 14, 12, 0, 0))).isTrue();
    }

    @Test
    @DisplayName("appliesAt devuelve true cuando la fecha coincide exactamente con startDate (límite inclusivo)")
    void appliesAt_returnsTrue_whenDateEqualsStartDate() {
        LocalDateTime startDate = LocalDateTime.of(2020, 6, 14, 15, 0, 0);
        Price price = priceWithRange(startDate, LocalDateTime.of(2020, 6, 14, 18, 30, 0));

        assertThat(price.appliesAt(startDate)).isTrue();
    }

    @Test
    @DisplayName("appliesAt devuelve true cuando la fecha coincide exactamente con endDate (límite inclusivo)")
    void appliesAt_returnsTrue_whenDateEqualsEndDate() {
        LocalDateTime endDate = LocalDateTime.of(2020, 6, 14, 18, 30, 0);
        Price price = priceWithRange(LocalDateTime.of(2020, 6, 14, 15, 0, 0), endDate);

        assertThat(price.appliesAt(endDate)).isTrue();
    }

    @Test
    @DisplayName("appliesAt devuelve false cuando la fecha es anterior a startDate")
    void appliesAt_returnsFalse_whenDateIsBeforeStartDate() {
        Price price = priceWithRange(
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0));

        assertThat(price.appliesAt(LocalDateTime.of(2020, 6, 14, 14, 59, 59))).isFalse();
    }

    @Test
    @DisplayName("appliesAt devuelve false cuando la fecha es posterior a endDate")
    void appliesAt_returnsFalse_whenDateIsAfterEndDate() {
        Price price = priceWithRange(
                LocalDateTime.of(2020, 6, 14, 15, 0, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30, 0));

        assertThat(price.appliesAt(LocalDateTime.of(2020, 6, 14, 18, 30, 1))).isFalse();
    }
}