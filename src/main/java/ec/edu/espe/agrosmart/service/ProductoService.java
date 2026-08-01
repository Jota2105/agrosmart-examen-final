package ec.edu.espe.agrosmart.service;


import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.domain.ProductoFilters;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.mapper.ProductoMapper;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private static final Producto PRODUCTO_GENERICO = new Producto(
            0L, "SIN PRODUCTOS COMERCIALIZABLES", "Banano", BigDecimal.ZERO, List.of());

    private final ProductoRepository repository;

    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public Flux<Producto> obtenerProductosComercializables() {

        /* fromCallable difiere repository.findAll(): la consulta no se ejecuta al construir el Flux,
          sino ya cuando un consumidor se suscribe. */
        return Mono.fromCallable(repository::findAll)

                /*
                  JPA/Hibernate utiliza JDBC y bloquea el hilo mientras espera
                  una respuesta de PostgreSQL. boundedElastic mueve esa operación fuera del event loop de Netty.
                 */
                .subscribeOn(Schedulers.boundedElastic())

                /*
                  repository.findAll() devuelve List<ProductoEntity>.
                  flatMapMany convierte el Mono de la lista en un Flux que emite cada entidad individualmente.
                 */
                .flatMapMany(Flux::fromIterable)

                /*  El Primer map: transforma cada ProductoEntity mutable en un Producto de dominio inmutable.
                 */
                .map(ProductoMapper::toDominio)

                /* El segundo map: crea una nueva instancia de Producto
                  con el nombre en MAYUSCULAS, sin mutar el original.
                 */
                .map(ProductoFilters.A_MAYUSCULAS)

                /*
                  Con filter se aplica la regla de comercialización:
                  precio mayor que cero y lista de correos no vacía.
                 */
                .filter(ProductoFilters.IS_VALID)

                /* doOnNext ejecuta el Consumer de trazabilidad.
                  No modifica ni tampoco reemplaza el producto emitido. */
                .doOnNext(ProductoFilters.LOG_PRODUCTO)

                /*
                  Si todos los registros fueron descartados por el filtro,
                  defaultIfEmpty emite exactamente UN producto genérico.
                 */
                .defaultIfEmpty(PRODUCTO_GENERICO);
    }

    public Mono<Producto> buscarPorId(Long id) {

        /*
          fromCallable difiere la consulta bloqueante findById hasta que haya una suscripción al Mono.
         */
        return Mono.fromCallable(() -> repository.findById(id))

                /* La consulta JPA se ejecuta en boundedElastic
                  y no en un hilo reactor-http-nio de Netty.
                 */
                .subscribeOn(Schedulers.boundedElastic())

                /* findById devuelve Optional<ProductoEntity>.
                  Si contiene un valor, Mono lo emite; si está vacío, continúa como Mono vacío.
                 */
                .flatMap(optional -> Mono.justOrEmpty(optional))


                  // Convierte la entidad encontrada al modelo de dominio inmutable.

                .map(ProductoMapper::toDominio)

                /*  Si el Optional estaba vacío, sustituye el Mono vacío
                 por un Mono que termina con ProductoNoEncontradoException. */


                .switchIfEmpty(
                        Mono.error(new ProductoNoEncontradoException(id))
                );
    }



}
