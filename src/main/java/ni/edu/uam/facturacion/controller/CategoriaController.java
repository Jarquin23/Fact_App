package ni.edu.uam.facturacion.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import ni.edu.uam.facturacion.dao.CategoriaDAO;
import ni.edu.uam.facturacion.model.Categoria;

import java.sql.SQLException;


public class CategoriaController {
    @FXML
    private TextField txtNombre;
    @FXML
    private CheckBox chkActiva;
    @FXML
    private TableView<Categoria> tblCategorias;

    @FXML
    private TableColumn<Categoria, Integer> colId;
    @FXML
    private TableColumn<Categoria, String> colNombre;
    @FXML
    private TableColumn<Categoria, Boolean> colActiva;

    private final CategoriaDAO cDAO = new CategoriaDAO();

    private final ObservableList<Categoria> categorias = FXCollections.observableArrayList();
    private Categoria seleccionada;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colActiva.setCellValueFactory(new PropertyValueFactory<>("activa"));

        tblCategorias.setItems(categorias);

        chkActiva.setSelected(true);

        tblCategorias.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        seleccionada = newVal;
                        txtNombre.setText(newVal.getNombre());
                        chkActiva.setSelected(newVal.isActiva());
                    }
                });

        cargarCategorias();
    }

    private void cargarCategorias() {
        try {
            categorias.setAll(cDAO.listar());
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al cargar categorías: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void cargarEnFormulario(Categoria cat) {
        if (cat != null) return;
        seleccionada = cat;
        txtNombre.setText(cat.getNombre());
        chkActiva.setSelected(cat.isActiva());
    }

    private boolean validarCategoria() {
        String nombre = txtNombre.getText().trim();
        if(nombre.isEmpty()) {
            mostrarAdvertencia("Validación",
                    "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return false;
        }
        return true;
    }

    @FXML
    private void guardar() {
        if (!validarCategoria()) return;

        try {
            String nombre = txtNombre.getText().trim();
            if(cDAO.existeNombre(nombre, null)) {
                mostrarAdvertencia("Validación",
                        "Ya existe una categoría con el nombre \"" + nombre + "\".");
                txtNombre.requestFocus();
                return;
            }
            Categoria c = new Categoria(null, nombre, chkActiva.isSelected());
            cDAO.guardar(c);
            mostrarExito("Categoría registrada", "La categoría se ha guardado correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarError("Error en la base de datos", "No fue posible registrar esta categoría: " + e.getMessage());
        }
    }

    @FXML
    private void actualizar() {
        if (seleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría",
                    "Debe seleccionar la categoría que desea actualizar.");
            return;
        }
        if (txtNombre.getText().isBlank()) {
            mostrarAdvertencia("Validación",
                    "El nombre de la categoría es obligatorio.");
            txtNombre.requestFocus();
            return;
        }
        if (!validarCategoria()) return;
        try {
            String nombre = txtNombre.getText().trim();
            if (cDAO.existeNombre(nombre, seleccionada.getId())) {
                mostrarAdvertencia("Nombre duplicado",
                        "Ya existe otra categoría con ese nombre.");
                txtNombre.requestFocus();
                return;
            }
            seleccionada.setNombre(nombre);
            seleccionada.setActiva(chkActiva.isSelected());
            cDAO.actualizar(seleccionada);
            mostrarExito("Categoría actualizada", "La categoría se ha actualizado correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarError("Error en la base de datos", "No fue posible actualizar esta categoría: " + e.getMessage());
        }
    }

    @FXML
    private void eliminar() {
        if (seleccionada == null) {
            mostrarAdvertencia("Seleccione una categoría",
                    "Debe seleccionar la categoría que desea eliminar.");
            return;
        }

        try {
            if (cDAO.tieneProductos(seleccionada.getId())) {
                mostrarError("No se puede eliminar",
                        "La categoría tiene productos asociados.\n" +
                                "Elimine o reasigne los productos primero.");
                return;
            }
        } catch (SQLException e) {
            mostrarError("Error de base de datos",
                    "No fue posible verificar la integridad.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría \"" + seleccionada.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) return;
        try {
            cDAO.eliminar(seleccionada.getId());
            mostrarExito("Categoría eliminada", "La categoría se ha eliminado correctamente.");
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            mostrarError("Error de base de datos", "No fue posible eliminar esta categoría: " + e.getMessage());
        }
    }

    @FXML
    private void limpiar() {
        txtNombre.clear();
        chkActiva.setSelected(true);
        seleccionada = null;
        tblCategorias.getSelectionModel().clearSelection();
    }

    @FXML
    private void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
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