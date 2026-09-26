package ni.edu.uam.facturacion.util;

import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.dao.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class TestConexion {

    public static void main(String[] args) {

        String url  = "jdbc:postgresql://localhost:5432/facturacion_db";
        String user = "postgres";
        String pass = "1234";

        try (Connection con = DriverManager.getConnection(url, user, pass)) {
            System.out.println("Conexión exitosa");
            System.out.println("   BD:      " + con.getMetaData().getDatabaseProductName());
            System.out.println("   Versión: " + con.getMetaData().getDatabaseProductVersion());
            System.out.println("   URL:     " + con.getMetaData().getURL());
            System.out.println("   Usuario: " + con.getMetaData().getUserName());
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("\n--- Categorías desde la Base de Datos ---");
        try {
            CategoriaDAO categoriaDAO = new CategoriaDAO();
            List<Categoria> categorias = categoriaDAO.listar();
            System.out.println("Total: " + categorias.size());
            categorias.forEach(c ->
                    System.out.println("  " + c.getId() + " | " + c.getNombre() + " | activa=" + c.isActiva()));
        } catch (SQLException e) {
            System.err.println("Error en CategoriaDAO: " + e.getMessage());
        }

        System.out.println("\n--- Productos desde la Base de Datos ---");
        try {
            ProductoDAO productoDAO = new ProductoDAO();
            List<Producto> productos = productoDAO.listar();
            System.out.println("Total: " + productos.size());
            productos.forEach(p ->
                    System.out.println("  " + p.getCodigo() + " | " + p.getNombre()
                            + " | " + p.getCategoria().getNombre()
                            + " | C$" + p.getPrecioVenta()
                            + " | stock=" + p.getExistencia()));
        } catch (SQLException e) {
            System.err.println("Error en ProductoDAO: " + e.getMessage());
        }
    }
}