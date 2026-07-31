package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class AgrosmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgrosmartApplication.class, args);
	}

	@Bean
	CommandLineRunner sembrarProductos(ProductoRepository repository){
		return args -> {
			if (repository.count() == 0){ // Siembra idempotente
				// 3 validos y 2 no invalidos
				List<ProductoEntity> productos = List.of( //VALIDO, precio > 0 y minimo un correo
						new ProductoEntity("Banano organico Cavendish", new BigDecimal("28.50"), 150, "Banano", "ventas@agrosmart.ec,exportacion@agrosmart.ec"),
						new ProductoEntity("Banano Premium de exportacion", new BigDecimal("34.90"), 220, "Banano", "mayoristas@agrosmart.ec"),
						new ProductoEntity("Banano ecologico seleccionado", new BigDecimal("31.75"), 180, "Banano", "comercial@agrosmart.ec,compras@agrosmart.ec"),
						// INVALIDO POR PRECIO = 0
						new ProductoEntity("Banano de muestra promocional", new BigDecimal("0.00"), 30, "Banano", "promociones@agrosmart.ec"),
						// INVALIDO POR NO TENER CORREOS
						new ProductoEntity("Banano verde para distribucion local", new BigDecimal("19.80"), 100, "Banano", "")

				); repository.saveAll(productos);
			}
		};
	}


}
