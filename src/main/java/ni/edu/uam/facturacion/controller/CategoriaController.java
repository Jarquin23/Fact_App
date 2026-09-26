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

    @FXML
    private void guardar() {
        if (txtNombre.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ingresa el nombre de la categoría.", ButtonType.OK);
            alert.showAndWait();
            return;
        }

        try {
            Categoria c = new Categoria(null, txtNombre.getText().trim(), chkActiva.isSelected());
            cDAO.guardar(c);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Categoría agregada correctamente.", ButtonType.OK);
            alert.showAndWait();
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al guardar: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void actualizar() {
        if (seleccionada == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecciona una categoría de la tabla.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        if (txtNombre.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Ingresa el nombre de la categoría para poder continuar.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        try {
            seleccionada.setNombre(txtNombre.getText().trim());
            seleccionada.setActiva(chkActiva.isSelected());
            cDAO.actualizar(seleccionada);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Categoría actualizada correctamente.", ButtonType.OK);
            alert.showAndWait();
            limpiar();
            cargarCategorias();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error al actualizar: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }
    }

    @FXML
    private void eliminar() {
        if (seleccionada == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Selecciona una categoría de la tabla.", ButtonType.OK);
            alert.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar la categoría \"" + seleccionada.getNombre() + "\"?",
                ButtonType.OK, ButtonType.CANCEL);
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                cDAO.eliminar(seleccionada.getId());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Categoría eliminada correctamente.", ButtonType.OK);
                alert.showAndWait();
                limpiar();
                cargarCategorias();
            } catch (SQLException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error al eliminar: " + e.getMessage(), ButtonType.OK);
                alert.showAndWait();
            }
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
    private void mensaje(Alert.AlertType tipo, String texto) {
        new Alert(tipo, texto, ButtonType.OK).showAndWait();
    }
}