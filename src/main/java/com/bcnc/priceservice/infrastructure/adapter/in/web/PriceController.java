package com.bcnc.priceservice.infrastructure.adapter.in.web;


import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.domain.port.in.FindApplicablePriceQuery;
import com.bcnc.priceservice.domain.port.in.FindApplicablePriceUseCase;
import com.bcnc.priceservice.infrastructure.adapter.in.web.dto.PriceResponse;
import com.bcnc.priceservice.infrastructure.adapter.in.web.mapper.PriceWebMapper;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Adaptador de entrada (<i>driving adapter</i>): traduce una petición HTTP a una llamada al
 * caso de uso. Depende de la interfaz {@link FindApplicablePriceUseCase}, nunca de
 * {@link com.bcnc.priceservice.application.service.PriceService} directamente — así el
 * controller se podría testear con un mock del puerto sin levantar nada de persistencia.
 *
 * <p>{@code @Validated} a nivel de clase habilita la validación de los parámetros anotados con
 * {@code jakarta.validation} directamente en los parámetros del método (aquí, {@code @NotNull}
 * en cada {@code @RequestParam}); sin esta anotación, esas restricciones se ignorarían
 * silenciosamente porque Spring solo valida cuerpos de petición ({@code @RequestBody}) por
 * defecto.
 */
@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
@Validated
public class PriceController {

    private final FindApplicablePriceUseCase findApplicablePriceUseCase;
    private final PriceWebMapper priceWebMapper;

    /**
     * {@code GET /api/v1/prices?brandId=&productId=&applicationDate=}
     *
     * @param brandId identificador de la cadena (p. ej. 1 = ZARA)
     * @param productId identificador del producto
     * @param applicationDate fecha/hora de aplicación en formato ISO-8601 ({@code yyyy-MM-dd'T'HH:mm:ss})
     * @return 200 con la tarifa aplicable; ver {@link com.bcnc.priceservice.infrastructure.adapter.in.web.exception.GlobalExceptionHandler}
     *         para los códigos de error (404 si no hay tarifa, 400 si los parámetros son inválidos)
     */
    @GetMapping
    public ResponseEntity<PriceResponse> getApplicablePrice(
            @RequestParam("brandId") @NotNull Long brandId,
            @RequestParam("productId") @NotNull Long productId,
            @RequestParam("applicationDate")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime applicationDate) {

        FindApplicablePriceQuery query = new FindApplicablePriceQuery(brandId, productId, applicationDate);
        Price price = findApplicablePriceUseCase.findApplicablePrice(query);
        return ResponseEntity.ok(priceWebMapper.toResponse(price));
    }
}
