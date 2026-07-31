package ec.edu.espe.agrosmart.mapper;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;

import java.util.Arrays;
import java.util.List;

public final class ProductoMapper {

    private ProductoMapper(){}

    public static Producto toDominio(ProductoEntity entity){
        List<String> correos = convertirCorreos(entity.getCorreosNotificacion());

        return new Producto(entity.getIdProducto(), entity.getNombreProducto(), entity.getCategoria(), entity.getPrecioUsd(), correos);
    }
    private static List<String> convertirCorreos(String correosSeparadosPorComa){
        if (correosSeparadosPorComa == null || correosSeparadosPorComa.isBlank()){
            return List.of();
        }
        return Arrays.stream(correosSeparadosPorComa.split(","))
                .map(String::trim)
                .filter(correo -> !correo.isEmpty())
                .toList();
    }

}
