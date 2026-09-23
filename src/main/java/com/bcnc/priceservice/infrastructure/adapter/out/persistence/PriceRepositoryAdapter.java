package com.bcnc.priceservice.infrastructure.adapter.out.persistence;

import com.bcnc.priceservice.domain.model.Price;
import com.bcnc.priceservice.domain.port.out.PriceRepositoryPort;
import com.bcnc.priceservice.infrastructure.adapter.out.persistence.mapper.PriceEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Adaptador de salida: implementa {@link PriceRepositoryPort}, el puerto que el dominio define,
 * apoyándose en Spring Data JPA ({@link PriceJpaRepository}). Es la única clase que conoce a la
 * vez el mundo del dominio ({@link Price}) y el de persistencia ({@link PriceEntity}) — por eso
 * delega en {@link PriceEntityMapper} en vez de dejar que ese acoplamiento se filtre hacia el
 * resto de la aplicación.
 *
 * <p>{@code @Repository} además de darla de alta como bean, habilita en Spring la traducción de
 * excepciones nativas del proveedor JPA (p. ej. {@code org.hibernate.exception.*}) a la
 * jerarquía {@code DataAccessException} de Spring — una capa de abstracción adicional sobre los
 * errores de persistencia que esta clase hereda gratis por llevar la anotación.
 */
@Repository
@RequiredArgsConstructor
public class PriceRepositoryAdapter implements PriceRepositoryPort {

    private final PriceJpaRepository priceJpaRepository;
    private final PriceEntityMapper priceEntityMapper;

    /**
     * {@inheritDoc}
     *
     * <p>Pide explícitamente una página de tamaño 1 ({@link PageRequest#of(int, int)}) para que
     * la limitación a un único resultado ocurra en la base de datos, no en este método.
     */
    @Override
    public Optional<Price> findApplicablePrice(Long brandId, Long productId, LocalDateTime applicationDate) {
        return priceJpaRepository
                .findApplicablePricesByPriorityDesc(brandId, productId, applicationDate, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(priceEntityMapper::toDomain);
    }
}
