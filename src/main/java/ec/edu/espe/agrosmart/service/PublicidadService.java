package ec.edu.espe.agrosmart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PublicidadService {
    private static final Duration TIEMPO_MAXIMO_RESPUESTA =
            Duration.ofSeconds(30);

    private final AgroSmartAIService aiService;

    public PublicidadService(AgroSmartAIService aiService) {
        this.aiService = aiService;
    }

    public Mono<String> generarPublicidad(
            String producto,
            String audiencia
    ) {
        // fromCallable difiere la llamada al modelo como vimos en ProductoService: no se ejecuta hasta que exista una suscripción.
        return Mono.fromCallable(
                        () -> aiService.generarPublicidad(
                                producto,
                                audiencia
                        )
                )

                /* La llamada HTTP al proveedor de IA es bloqueante.
                   boundedElastic evita ejecutarla en el event loop de Netty.*/
                .subscribeOn(Schedulers.boundedElastic())

                // Si el modelo no responde en 30 seg,Reactor genera una señal de error por timeout.

                .timeout(TIEMPO_MAXIMO_RESPUESTA)

                /* Si el proveedor falla, se agota la cuota o vence el tiempo,
                  el endpoint recibe un texto de respaldo en lugar de un error. */
                .onErrorResume(error -> Mono.just(
                        "Publicidad no disponible en este momento ("
                                + error.getClass().getSimpleName()
                                + ")"
                ));
    }




}
