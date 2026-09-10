package ni.edu.uam.facturacion.model;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Producto {
    private Integer id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Categoria categoria;
    private BigDecimal precioVenta;
    private int existencia;
    private String rutaImagen;
    private boolean activo;
}
