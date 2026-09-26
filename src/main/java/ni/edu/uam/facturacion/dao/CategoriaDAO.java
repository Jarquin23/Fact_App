package ni.edu.uam.facturacion.dao;

import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public void guardar(Categoria c) throws SQLException {
        String sql = "INSERT INTO categoria (nombre, activa) VALUES (?, ?)";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActiva());
            ps.executeUpdate();
        }
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id, nombre, activa FROM categoria WHERE activa = TRUE ORDER BY nombre";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Categoria(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBoolean("activa")));
            }
        }
        return lista;
    }

    public Categoria buscar(int id) throws SQLException {
        String sql = "SELECT id, nombre, activa FROM categoria WHERE id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Categoria(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getBoolean("activa"));
                }
            }
        }
        return null;
    }

    public void actualizar(Categoria c) throws SQLException {
        String sql = "UPDATE categoria SET nombre = ?, activa = ? WHERE id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setBoolean(2, c.isActiva());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM categoria WHERE id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}