package com.bcnc.priceservice.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test de integración end-to-end de {@link PriceController}, contra los 5 casos exigidos por el
 * enunciado (producto 35455, marca 1, fechas del 14 al 16 de junio de 2020).
 *
 * <p>Levanta el contexto completo de Spring ({@code @SpringBootTest}) con la base de datos H2
 * real, sembrada por {@code src/main/resources/data.sql} al arrancar — es el mismo camino que se
 * verificó manualmente con {@code curl.exe} durante el desarrollo, pero aquí queda automatizado y
 * repetible en cualquier máquina o pipeline de CI.
 *
 * <p>{@code @AutoConfigureMockMvc} es imprescindible: a partir de Spring Boot 4 / Spring
 * Framework 7, {@code @SpringBootTest} dejó de autoconfigurar {@link MockMvc} por defecto (antes
 * de esa versión no hacía falta esta anotación).
 */
@SpringBootTest
@AutoConfigureMockMvc
class PriceControllerIntegrationTest {

    private static final String BASE_URL = "/api/v1/prices";
    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Los 5 casos obligatorios del enunciado. La combinación de fechas fuerza tanto tarifas sin
     * solape (tests 1, 3 y 5) como tarifas solapadas donde debe ganar la de mayor
     * {@code PRIORITY} (tests 2 y 4) — estos dos últimos son los que realmente validan el
     * criterio de desempate, no solo el filtrado por fecha.
     *
     * @param applicationDate fecha/hora de aplicación enviada como parámetro de la petición
     * @param expectedPriceList {@code PRICE_LIST} que debe devolver el endpoint
     * @param expectedPrice precio final que debe devolver el endpoint
     */
    @ParameterizedTest(name = "[{index}] {0} → tarifa {1}, precio {2}")
    @DisplayName("Los 5 casos del enunciado devuelven la tarifa y el precio esperados")
    @CsvSource({
            "2020-06-14T10:00:00, 1, 35.50",
            "2020-06-14T16:00:00, 2, 25.45",
            "2020-06-14T21:00:00, 1, 35.50",
            "2020-06-15T10:00:00, 3, 30.50",
            "2020-06-16T21:00:00, 4, 38.95"
    })
    void getApplicablePrice_returnsExpectedTariff(
            String applicationDate, int expectedPriceList, double expectedPrice) throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("brandId", BRAND_ID.toString())
                        .param("productId", PRODUCT_ID.toString())
                        .param("applicationDate", applicationDate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(PRODUCT_ID))
                .andExpect(jsonPath("$.brandId").value(BRAND_ID))
                .andExpect(jsonPath("$.priceList").value(expectedPriceList))
                .andExpect(jsonPath("$.price").value(expectedPrice))
                .andExpect(jsonPath("$.currency").value("EUR"));
    }

    @Test
    @DisplayName("Devuelve 404 cuando no existe ninguna tarifa aplicable a la fecha dada")
    void getApplicablePrice_returns404_whenNoTariffApplies() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("brandId", BRAND_ID.toString())
                        .param("productId", PRODUCT_ID.toString())
                        .param("applicationDate", "2019-01-01T00:00:00"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("Devuelve 400 cuando falta un parámetro obligatorio")
    void getApplicablePrice_returns400_whenRequiredParameterIsMissing() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("productId", PRODUCT_ID.toString())
                        .param("applicationDate", "2020-06-14T10:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Devuelve 400 cuando la fecha de aplicación tiene un formato inválido")
    void getApplicablePrice_returns400_whenDateFormatIsInvalid() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param("brandId", BRAND_ID.toString())
                        .param("productId", PRODUCT_ID.toString())
                        .param("applicationDate", "no-es-una-fecha"))
                .andExpect(status().isBadRequest());
    }
}
