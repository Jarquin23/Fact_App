package ni.edu.uam.facturacion.util;

import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;

import java.sql.SQLException;
import java.util.List;

public class TestCategoriaDAO {
    public static void main(String[] args) {
        CategoriaDAO dao = new CategoriaDAO();

        try {
            // 1. LISTAR
            System.out.println("--- listar() ---");
            List<Categoria> lista = dao.listar();
            lista.forEach(c -> System.out.println("  " + c.getId() + " | " + c.getNombre()));

            // 2. GUARDAR
            System.out.println("\n--- guardar() ---");
            Categoria nueva = new Categoria(null, "Prueba Test", true);
            dao.guardar(nueva);
            System.out.println("Categoría guardada");

            // 3. BUSCAR
            System.out.println("\n--- buscar() ---");
            Categoria encontrada = dao.buscar(1);   // busca el id=1
            if (encontrada != null) {
                System.out.println("  Encontrada: " + encontrada.getNombre());
            }

            // 4. ACTUALIZAR
            System.out.println("\n--- actualizar() ---");
            if (encontrada != null) {
                encontrada.setNombre("Alimentos y más");
                dao.actualizar(encontrada);
                System.out.println("Categoría actualizada");
            }

            // 5. ELIMINAR (solo la de prueba)
            System.out.println("\n--- eliminar() ---");
            List<Categoria> despues = dao.listar();
            Categoria ultima = despues.get(despues.size() - 1);
            dao.eliminar(ultima.getId());
            System.out.println("Categoría eliminada: " + ultima.getNombre());

        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}