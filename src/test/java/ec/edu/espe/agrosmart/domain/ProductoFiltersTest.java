package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreos_debeRetornarTrue() {
        // 1. Arrange
        Producto producto = new Producto(1L,"Banano organico Cavendish","Banano", new BigDecimal("28.50"), List.of("ventas@agrosmart.ec"));

        // 2. Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // 3. Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Banano de muestra promocional",
                "Banano",
                BigDecimal.ZERO,
                List.of("promociones@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_conCorreosVacios_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Banano verde para distribucion local",
                "Banano",
                new BigDecimal("19.80"),
                List.of()
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);
        // Assert
        assertFalse(resultado);
    }

    @Test
    void aMayusculas_conProducto_debeCrearNuevaInstanciaSinMutarElOriginal() {
        // Arrange
        Producto original = new Producto(
                1L,
                "Banano organico Cavendish",
                "Banano",
                new BigDecimal("28.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        Producto transformado = ProductoFilters.A_MAYUSCULAS.apply(original);
        // Assert
        assertNotSame(original, transformado);

        assertEquals(
                "BANANO ORGANICO CAVENDISH",
                transformado.getNombre()
        );
        assertEquals(
                "Banano organico Cavendish",
                original.getNombre()
        );
    }
}
