package ni.edu.uam.facturacion.dao;

import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;
import ni.edu.uam.facturacion.util.ConexionDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public void guardar(Producto p) throws SQLException {
        String sql = """
                INSERT INTO producto
                    (codigo, nombre, categoria_id, precio_venta, existencia, ruta_imagen, activo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getId());
            ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setInt(5, p.getExistencia());
            ps.setString(6, p.getRutaImagen());
            ps.setBoolean(7, p.isActivo());
            ps.executeUpdate();
        }
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> lista = new ArrayList<>();
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia,
                       p.ruta_imagen, p.activo,
                       c.id     AS cat_id,
                       c.nombre AS cat_nombre,
                       c.activa AS cat_activa
                FROM producto p
                INNER JOIN categoria c ON p.categoria_id = c.id
                ORDER BY p.codigo
                """;
        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearProducto(rs));
            }
        }
        return lista;
    }

    public Producto buscar(int id) throws SQLException {
        String sql = """
                SELECT p.id, p.codigo, p.nombre, p.precio_venta, p.existencia,
                       p.ruta_imagen, p.activo,
                       c.id     AS cat_id,
                       c.nombre AS cat_nombre,
                       c.activa AS cat_activa
                FROM producto p
                INNER JOIN categoria c ON p.categoria_id = c.id
                WHERE p.id = ?
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearProducto(rs);
                }
            }
        }
        return null;
    }

    public void actualizar(Producto p) throws SQLException {
        String sql = """
                UPDATE producto
                SET codigo = ?, nombre = ?, categoria_id = ?,
                    precio_venta = ?, existencia = ?, ruta_imagen = ?, activo = ?
                WHERE id = ?
                """;

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setInt(3, p.getCategoria().getId());
            ps.setBigDecimal(4, p.getPrecioVenta());
            ps.setInt(5, p.getExistencia());
            ps.setString(6, p.getRutaImagen());
            ps.setBoolean(7, p.isActivo());
            ps.setInt(8, p.getId());
            ps.executeUpdate();
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM producto WHERE id = ?";

        try (Connection con = ConexionDB.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Producto mapearProducto(ResultSet rs) throws SQLException {
        Categoria cat = new Categoria(
                rs.getInt("cat_id"),
                rs.getString("cat_nombre"),
                rs.getBoolean("cat_activa"));
        return new Producto(
                rs.getInt("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                cat,
                rs.getBigDecimal("precio_venta"),
                rs.getInt("existencia"),
                rs.getString("ruta_imagen"),
                rs.getBoolean("activo"));
    }
}