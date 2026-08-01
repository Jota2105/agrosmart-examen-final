package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ProductoTest {

    @Test
    void getters_alConstruirProducto_debenDevolverLosValoresRecibidos() {

        // 1. Arrange
        List<String> correos = List.of("ventas@agrosmart.ec", "exportacion@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Banano organico Cavendish",
                "Banano",
                new BigDecimal("28.50"),
                correos
        );


        // 2. Act
        Long id = producto.getId();
        String nombre = producto.getNombre();
        String categoria = producto.getCategoria();
        BigDecimal precio = producto.getPrecioUsd();
        List<String> correosObtenidos =
                producto.getCorreosNotificacion();

        // 3. Assert
        assertEquals(1L, id);
        assertEquals("Banano organico Cavendish", nombre);
        assertEquals("Banano", categoria);
        assertEquals(new BigDecimal("28.50"), precio);
        assertEquals(correos, correosObtenidos);
    }

    @Test
    void constructor_alMutarLaListaOriginal_noDebeModificarElProducto() {
        // Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Banano organico Cavendish",
                "Banano",
                new BigDecimal("28.50"),
                correos
        );

        // Act
        correos.add("intruso@mail.com");

        // Assert
        assertEquals(
                1,
                producto.getCorreosNotificacion().size()
        );

        assertEquals(List.of("ventas@agrosmart.ec"), producto.getCorreosNotificacion());
    }

    @Test
    void getCorreosNotificacion_alObtenerLaLista_debeDevolverCopiaInmodificable() {

        // El Arrange
        List<String> correos = new ArrayList<>();
        correos.add("ventas@agrosmart.ec");

        Producto producto = new Producto(1L, "Banano organico Cavendish", "Banano", new BigDecimal("28.50"),  correos );

        // Act
        List<String> correosObtenidos =
                producto.getCorreosNotificacion();

        // Assert
        assertNotSame(correos, correosObtenidos);

        assertThrows(
                UnsupportedOperationException.class,
                () -> correosObtenidos.add("intruso@mail.com")
        );

        assertEquals(
                List.of("ventas@agrosmart.ec"),
                producto.getCorreosNotificacion()
        );
    }

}
