package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.dao.ProductoDAO;
import ni.edu.uam.facturacion.model.Categoria;
import ni.edu.uam.facturacion.model.Producto;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductoController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencia;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private CheckBox chkActivo;
    @FXML private ImageView imgProducto;
    @FXML private TableView<Producto> tblProductos;
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroEstado;
    @FXML private ComboBox<Categoria> cmbFiltroCategoria;

    @FXML private TableColumn<Producto, String>     colCodigo;
    @FXML private TableColumn<Producto, String>     colNombre;
    @FXML private TableColumn<Producto, Categoria>  colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer>    colExistencia;
    @FXML private TableColumn<Producto, Boolean>    colActivo;

    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ProductoDAO  productoDAO  = new ProductoDAO();

    private final ObservableList<Producto> productos = FXCollections.observableArrayList();
    private FilteredList<Producto> productosFiltrados;   // 👈 NUEVO
    private Producto seleccionado;
    private String rutaImagen;

    @FXML
    private void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colExistencia.setCellValueFactory(new PropertyValueFactory<>("existencia"));
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));

        productosFiltrados = new FilteredList<>(productos, p -> true);
        tblProductos.setItems(productosFiltrados);
        chkActivo.setSelected(true);
        cmbFiltroEstado.setItems(FXCollections.observableArrayList(
                "Todos", "Activos", "Inactivos"));
        cmbFiltroEstado.getSelectionModel().selectFirst();
        txtBuscar.setOnAction(e -> aplicarFiltros());
        cmbFiltroEstado.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
        cmbFiltroCategoria.valueProperty().addListener((obs, o, n) -> aplicarFiltros());
        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> cargarEnFormulario(newVal));

        cargarCategorias();
        cargarProductos();
    }

    private void cargarCategorias() {
        try {
            ObservableList<Categoria> cats =
                    FXCollections.observableArrayList(categoriaDAO.listar());
            cmbCategoria.setItems(cats);
            cmbFiltroCategoria.setItems(cats);
        } catch (SQLException e) {
            mostrarError("Error al cargar categorías: ", "No fue posible cargar las categorías.");
            System.err.println(e.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
            aplicarFiltros();
        } catch (SQLException e) {
            mostrarError("Error al cargar productos: ", "No fue posible cargar los productos.");
            System.err.println(e.getMessage());
        }
    }

    private void aplicarFiltros() {
        String texto = txtBuscar.getText() == null
                ? "" : txtBuscar.getText().trim().toLowerCase();

        String estado = cmbFiltroEstado.getValue();
        Categoria catFiltro = cmbFiltroCategoria.getValue();

        productosFiltrados.setPredicate(p -> {
            if (!texto.isEmpty()) {
                boolean coincide =
                        p.getCodigo().toLowerCase().contains(texto) ||
                                p.getNombre().toLowerCase().contains(texto) ||
                                p.getCategoria().getNombre().toLowerCase().contains(texto);
                if (!coincide) return false;
            }

            if ("Activos".equals(estado) && !p.isActivo()) return false;
            if ("Inactivos".equals(estado) && p.isActivo()) return false;

            if (catFiltro != null && p.getCategoria().getId() != catFiltro.getId()) {
                return false;
            }

            return true;
        });
    }

    @FXML
    private void buscar(){
        aplicarFiltros();
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroEstado.getSelectionModel().selectFirst();
        cmbFiltroCategoria.getSelectionModel().clearSelection();
        aplicarFiltros();
    }

    private void cargarEnFormulario(Producto p) {
        if (p == null) return;
        seleccionado = p;
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        txtPrecio.setText(p.getPrecioVenta().toString());
        txtExistencia.setText(String.valueOf(p.getExistencia()));
        cmbCategoria.setValue(p.getCategoria());
        chkActivo.setSelected(p.isActivo());

        rutaImagen = p.getRutaImagen();
        if (rutaImagen != null && !rutaImagen.isBlank()) {
            try {
                imgProducto.setImage(new Image(rutaImagen));
            } catch (Exception e) {
                imgProducto.setImage(null);
            }
        } else {
            imgProducto.setImage(null);
        }
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg"));
        File archivo = chooser.showOpenDialog(txtCodigo.getScene().getWindow());
        if (archivo != null) {
            rutaImagen = archivo.toURI().toString();
            imgProducto.setImage(new Image(rutaImagen));
        }
    }

    private Producto obtenerProductoFormulario() {
        String codigo = txtCodigo.getText().trim();
        String nombre = txtNombre.getText().trim();

        if (codigo.isEmpty()) {
            txtCodigo.requestFocus();
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        if (nombre.isEmpty()) {
            txtNombre.requestFocus();
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }

        Categoria categoria = cmbCategoria.getSelectionModel().getSelectedItem();
        if (categoria == null) {
            cmbCategoria.requestFocus();
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim());
        } catch (NumberFormatException e) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("Precio no válido.");
        }
        if (precio.compareTo(BigDecimal.ZERO) <= 0) {
            txtPrecio.requestFocus();
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        int existencia;
        try {
            existencia = Integer.parseInt(txtExistencia.getText().trim());
        } catch (NumberFormatException e) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("El valor de la existencia debe ser un número entero.");
        }
        if (existencia < 0) {
            txtExistencia.requestFocus();
            throw new IllegalArgumentException("La existencia no puede ser negativa.");
        }
        return new Producto(null, codigo, nombre, categoria, precio, existencia, rutaImagen, chkActivo.isSelected());
    }

    @FXML
    private void guardar() {
        try {
            Producto producto = obtenerProductoFormulario();
            productoDAO.guardar(producto);
            mostrarExito("Producto registrado",
                    "La información fue almacenada correctamente.");
            cargarProductos();
            limpiar();
        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible registrar el producto.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto que desea actualizar.");
            return;
        }

        try {
            Producto datos = obtenerProductoFormulario();

            if (productoDAO.existeCodigo(datos.getCodigo(), seleccionado.getId())) {
                mostrarAdvertencia("Código duplicado",
                        "Ya existe otro producto con ese código.");
                txtCodigo.requestFocus();
                return;
            }

            seleccionado.setCodigo(datos.getCodigo());
            seleccionado.setNombre(datos.getNombre());
            seleccionado.setCategoria(datos.getCategoria());
            seleccionado.setPrecioVenta(datos.getPrecioVenta());
            seleccionado.setExistencia(datos.getExistencia());
            seleccionado.setRutaImagen(datos.getRutaImagen());
            seleccionado.setActivo(datos.isActivo());

            productoDAO.actualizar(seleccionado);
            mostrarExito("Producto actualizado",
                    "La información fue actualizada correctamente.");
            cargarProductos();
            limpiar();

        } catch (IllegalArgumentException e) {
            mostrarAdvertencia("Validación", e.getMessage());
        } catch (SQLException e) {
            mostrarError("Error de base de datos",
                    "No fue posible actualizar el producto.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (seleccionado == null) {
            mostrarAdvertencia("Seleccione un producto", "Debe seleccionar un producto que desea eliminar.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto \"" + seleccionado.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            productoDAO.eliminar(seleccionado.getId());
            mostrarExito("Producto eliminado", "El producto fue eliminado correctamente.");
            limpiar();
            cargarProductos();
        } catch (SQLException e) {
            mostrarError("Error al eliminar", "No fue posible eliminar el producto.");
            System.err.println(e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        txtCodigo.clear();
        txtNombre.clear();
        txtPrecio.clear();
        txtExistencia.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        chkActivo.setSelected(true);
        imgProducto.setImage(null);
        rutaImagen = null;
        seleccionado = null;
        tblProductos.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        ((Stage) txtCodigo.getScene().getWindow()).close();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
    private void mostrarExito(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.setContentText(mensaje);
        a.showAndWait();
    }
}