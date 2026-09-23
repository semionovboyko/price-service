package com.bcnc.priceservice.application.service;

import com.bcnc.priceservice.domain.exception.PriceNotFoundException;
import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.domain.port.in.FindApplicablePriceQuery;
import com.bcnc.priceservice.domain.port.out.PriceRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario de {@link PriceService}, con {@link PriceRepositoryPort} mockeado con Mockito.
 *
 * <p>A diferencia de {@link com.bcnc.priceservice.infrastructure.adapter.in.web.PriceControllerIntegrationTest},
 * este test no levanta contexto de Spring ni base de datos ({@code @ExtendWith(MockitoExtension.class)}
 * en vez de {@code @SpringBootTest}): verifica en aislamiento total que el caso de uso orquesta
 * correctamente el puerto de salida, sin depender de infraestructura. Es deliberadamente rápido
 * y no necesita las anotaciones {@code @MockitoBean}/{@code @MockitoSpyBean} introducidas en
 * Spring Boot 4 — esas son solo necesarias cuando el mock tiene que sustituir un bean dentro de un
 * contexto de Spring ya levantado, que no es el caso aquí.
 */
@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    private static final Long BRAND_ID = 1L;
    private static final Long PRODUCT_ID = 35455L;
    private static final LocalDateTime APPLICATION_DATE = LocalDateTime.of(2020, 6, 14, 10, 0, 0);

    @Mock
    private PriceRepositoryPort priceRepositoryPort;

    @InjectMocks
    private PriceService priceService;

    @Test
    @DisplayName("Devuelve la tarifa cuando el repositorio la encuentra")
    void findApplicablePrice_returnsPrice_whenRepositoryFindsIt() {
        Price expectedPrice = Price.builder()
                .brandId(BRAND_ID)
                .productId(PRODUCT_ID)
                .startDate(LocalDateTime.of(2020, 6, 14, 0, 0, 0))
                .endDate(LocalDateTime.of(2020, 12, 31, 23, 59, 59))
                .priceList(1)
                .priority(0)
                .amount(BigDecimal.valueOf(35.50))
                .currency(Currency.getInstance("EUR"))
                .build();

        when(priceRepositoryPort.findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(Optional.of(expectedPrice));

        FindApplicablePriceQuery query = new FindApplicablePriceQuery(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);
        Price result = priceService.findApplicablePrice(query);

        assertThat(result).isEqualTo(expectedPrice);
        verify(priceRepositoryPort).findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);
    }

    @Test
    @DisplayName("Lanza PriceNotFoundException cuando el repositorio no encuentra ninguna tarifa")
    void findApplicablePrice_throwsPriceNotFoundException_whenRepositoryFindsNothing() {
        when(priceRepositoryPort.findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE))
                .thenReturn(Optional.empty());

        FindApplicablePriceQuery query = new FindApplicablePriceQuery(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);

        assertThatThrownBy(() -> priceService.findApplicablePrice(query))
                .isInstanceOf(PriceNotFoundException.class);

        verify(priceRepositoryPort).findApplicablePrice(BRAND_ID, PRODUCT_ID, APPLICATION_DATE);
    }
}