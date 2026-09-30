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
        txtBuscar.textProperty().addListener((obs, o, n) -> aplicarFiltros());
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
            mensaje(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage());
        }
    }

    private void cargarProductos() {
        try {
            productos.setAll(productoDAO.listar());
            aplicarFiltros();
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al cargar productos: " + e.getMessage());
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

    @FXML
    private void guardar() {
        if (!validarCampos()) return;
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING,
                        "Precio mayor que cero y existencia no negativa.");
                return;
            }

            // validación de código duplicado
            String codigoNuevo = txtCodigo.getText().trim();
            boolean duplicado = productos.stream()
                    .anyMatch(p -> p.getCodigo().equalsIgnoreCase(codigoNuevo));
            if (duplicado) {
                mensaje(Alert.AlertType.WARNING,
                        "Ya existe un producto con el código: " + codigoNuevo);
                return;
            }
            Producto p = new Producto(
                    null,
                    codigoNuevo,
                    txtNombre.getText().trim(),
                    cmbCategoria.getValue(),
                    precio,
                    existencia,
                    rutaImagen,
                    chkActivo.isSelected());
            productoDAO.guardar(p);
            mensaje(Alert.AlertType.INFORMATION, "Producto agregado correctamente.");
            limpiar();
            cargarProductos();
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla.");
            return;
        }
        if (!validarCampos()) return;
        try {
            BigDecimal precio = new BigDecimal(txtPrecio.getText().trim());
            int existencia = Integer.parseInt(txtExistencia.getText().trim());

            if (precio.signum() <= 0 || existencia < 0) {
                mensaje(Alert.AlertType.WARNING,
                        "Precio mayor que cero y existencia no negativa.");
                return;
            }
            // validación de código duplicado (excluyendo el mismo)
            String codigoNuevo = txtCodigo.getText().trim();
            boolean duplicado = productos.stream()
                    .anyMatch(p -> p.getId() != seleccionado.getId()
                            && p.getCodigo().equalsIgnoreCase(codigoNuevo));
            if (duplicado) {
                mensaje(Alert.AlertType.WARNING,
                        "Ya existe otro producto con el código: " + codigoNuevo);
                return;
            }

            seleccionado.setCodigo(codigoNuevo);
            seleccionado.setNombre(txtNombre.getText().trim());
            seleccionado.setCategoria(cmbCategoria.getValue());
            seleccionado.setPrecioVenta(precio);
            seleccionado.setExistencia(existencia);
            seleccionado.setRutaImagen(rutaImagen);
            seleccionado.setActivo(chkActivo.isSelected());

            productoDAO.actualizar(seleccionado);
            mensaje(Alert.AlertType.INFORMATION, "Producto actualizado.");
            limpiar();
            cargarProductos();
        } catch (NumberFormatException e) {
            mensaje(Alert.AlertType.ERROR, "Precio o existencia no válidos.");
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al actualizar: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (seleccionado == null) {
            mensaje(Alert.AlertType.WARNING, "Seleccione un producto de la tabla.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar el producto \"" + seleccionado.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            productoDAO.eliminar(seleccionado.getId());
            mensaje(Alert.AlertType.INFORMATION, "Producto eliminado.");
            limpiar();
            cargarProductos();
        } catch (SQLException e) {
            mensaje(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage());
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

    private boolean validarCampos() {
        if (txtCodigo.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El código es obligatorio.");
            return false;
        }
        if (txtNombre.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El nombre es obligatorio.");
            return false;
        }
        if (cmbCategoria.getValue() == null) {
            mensaje(Alert.AlertType.WARNING, "Debe seleccionar una categoría.");
            return false;
        }
        if (txtPrecio.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "El precio es obligatorio.");
            return false;
        }
        if (txtExistencia.getText().isBlank()) {
            mensaje(Alert.AlertType.WARNING, "La existencia es obligatoria.");
            return false;
        }
        return true;
    }

    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}