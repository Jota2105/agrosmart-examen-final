package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductoServiceTest {

    private ProductoRepository repository;
    private ProductoService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ProductoRepository.class);
        service = new ProductoService(repository);
    }

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTresProductos() {
        // 1. Arrange
        when(repository.findAll()).thenReturn(datosDePrueba());
        // 2. Act
        var flujo = service.obtenerProductosComercializables();
        // 3. Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        List<ProductoEntity> productosInvalidos = List.of(
                crearEntidad(
                        4L,
                        "Banano con precio cero",
                        "0.00",
                        "ventas@agrosmart.ec"
                ),
                crearEntidad(
                        5L,
                        "Banano sin correos",
                        "20.00",
                        ""
                )
        );

        when(repository.findAll())
                .thenReturn(productosInvalidos);

        // Act
        var flujo = service.obtenerProductosComercializables();
        // Assert
        StepVerifier.create(flujo)
                .assertNext(producto -> {
                    assertEquals(0L, producto.getId());

                    assertEquals(
                            "SIN PRODUCTOS COMERCIALIZABLES",
                            producto.getNombre()
                    );
                })
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void buscarPorId_conIdInexistente_debeEmitirProductoNoEncontradoException() {
        // Arrange
        Long idInexistente = 9999L;

        when(repository.findById(idInexistente))
                .thenReturn(Optional.empty());
        // Act
        var resultado = service.buscarPorId(idInexistente);
        // Assert
        StepVerifier.create(resultado)
                .expectError(
                        ProductoNoEncontradoException.class
                )
                .verify();

        verify(repository).findById(idInexistente);
    }

    private List<ProductoEntity> datosDePrueba() {
        return List.of(
                // Los que son Validos
                crearEntidad(
                        1L,
                        "Banano organico Cavendish",
                        "28.50",
                        "ventas@agrosmart.ec"
                ),
                crearEntidad(
                        2L,
                        "Banano premium de exportacion",
                        "34.90",
                        "mayoristas@agrosmart.ec"
                ),
                crearEntidad(
                        3L,
                        "Banano ecologico seleccionado",
                        "31.75",
                        "comercial@agrosmart.ec"
                ),

                // IVALIDOS -> Inválido: precio cero
                crearEntidad(
                        4L,
                        "Banano de muestra promocional",
                        "0.00",
                        "promociones@agrosmart.ec"
                ),

                // Inválido: correos vacíos
                crearEntidad(
                        5L,
                        "Banano verde para distribucion local",
                        "19.80",
                        ""
                )
        );
    }

    private ProductoEntity crearEntidad(
            Long id,
            String nombre,
            String precio,
            String correos
    ) {
        ProductoEntity entity = new ProductoEntity(
                nombre,
                new BigDecimal(precio),
                100,
                "Banano",
                correos
        );

        entity.setIdProducto(id);

        return entity;
    }
}