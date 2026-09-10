package ni.edu.uam.facturacion.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Empleado {
    private Integer id;
    private String nombre;
    private String apellido;
    private String cargo;
    private LocalDate fecha;
}