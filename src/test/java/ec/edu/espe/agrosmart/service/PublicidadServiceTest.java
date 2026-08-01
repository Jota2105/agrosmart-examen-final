package ec.edu.espe.agrosmart.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublicidadServiceTest {

    private AgroSmartAIService aiService;
    private PublicidadService service;

    @BeforeEach
    void setUp() {
        aiService = Mockito.mock(AgroSmartAIService.class);
        service = new PublicidadService(aiService);
    }

    @Test
    void generarPublicidad_cuandoElProveedorResponde_debeEmitirElTextoGenerado() {
        // 1. Arrange
        String producto = "Banano organico Cavendish";
        String audiencia = "supermercados mayoristas";

        String publicidadEsperada =
                "Banano organico de calidad para tu supermercado.";

        when(
                aiService.generarPublicidad(
                        producto,
                        audiencia
                )
        ).thenReturn(publicidadEsperada);
        // 2. Act
        var resultado = service.generarPublicidad(
                producto,
                audiencia
        );
        // 3. Assert
        StepVerifier.create(resultado)
                .expectNext(publicidadEsperada)
                .verifyComplete();

        verify(aiService).generarPublicidad(
                producto,
                audiencia
        );
    }

    @Test
    void generarPublicidad_cuandoElProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        String producto = "Banano organico Cavendish";
        String audiencia = "supermercados mayoristas";

        when(
                aiService.generarPublicidad(producto, audiencia)).thenThrow(
                new RuntimeException("429 Too Many Requests")
        );
        // Act
        var resultado = service.generarPublicidad(
                producto,
                audiencia
        );
        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(texto ->
                        texto.contains(
                                "Publicidad no disponible"
                        )
                                && texto.contains(
                                "RuntimeException"
                        )
                )
                .verifyComplete();

        verify(aiService).generarPublicidad(
                producto,
                audiencia
        );
    }
}