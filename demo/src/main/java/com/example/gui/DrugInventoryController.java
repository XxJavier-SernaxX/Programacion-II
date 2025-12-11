package com.example.gui;

import com.example.EPSService;
import com.example.Medicamento;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDate;

public class DrugInventoryController {

    private EPSService service;

    @FXML
    private TableView<Medicamento> medsTable;
    @FXML
    private TableColumn<Medicamento, String> colName;
    @FXML
    private TableColumn<Medicamento, String> colDose;
    @FXML
    private TableColumn<Medicamento, Integer> colQty;
    @FXML
    private TableColumn<Medicamento, LocalDate> colExpiry;

    @FXML
    private TextField nameField;
    @FXML
    private TextField doseField;
    @FXML
    private TextField qtyField;
    @FXML
    private DatePicker expiryPicker;

    @FXML
    private Label selectedMedLabel;
    @FXML
    private TextField newStockField;

    public void setService(EPSService service) {
        this.service = service;
        loadData();
    }

    @FXML
    public void initialize() {
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNombre()));
        colDose.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDosis()));
        colQty.setCellValueFactory(
                cell -> new SimpleIntegerProperty(cell.getValue().getCantidadDisponible()).asObject());
        colExpiry.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getFechaVencimiento()));

        medsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedMedLabel.setText(newVal.getNombre());
                newStockField.setText(String.valueOf(newVal.getCantidadDisponible()));
            } else {
                selectedMedLabel.setText("Ninguno");
                newStockField.clear();
            }
        });
    }

    private void loadData() {
        if (service == null)
            return;
        medsTable.setItems(FXCollections.observableArrayList(service.listarMedicamentos()));
    }

    @FXML
    protected void onAdd() {
        try {
            String name = nameField.getText();
            String dose = doseField.getText();
            String qtyTxt = qtyField.getText();
            LocalDate expiry = expiryPicker.getValue();

            if (name.isBlank() || dose.isBlank() || qtyTxt.isBlank() || expiry == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", "Todos los campos son obligatorios.");
                return;
            }

            if (expiry.isBefore(LocalDate.now())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Fecha Inválida", "La fecha de vencimiento debe ser futura.");
                return;
            }

            int qty = Integer.parseInt(qtyTxt);
            if (qty < 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Cantidad Inválida", "La cantidad no puede ser negativa.");
                return;
            }

            Medicamento m = new Medicamento(name, dose, qty, expiry);
            service.agregarMedicamento(m);
            loadData();
            clearForm();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Medicamento agregado correctamente.");

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número entero.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    protected void onUpdateStock() {
        Medicamento selected = medsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección Requerida", "Seleccione un medicamento de la tabla.");
            return;
        }

        try {
            int newQty = Integer.parseInt(newStockField.getText());
            if (newQty < 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Stock no puede ser negativo.");
                return;
            }

            boolean ok = service.actualizarStockMedicamento(selected.getId(), newQty);
            if (ok) {
                loadData();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Stock actualizado.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo actualizar el stock.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Stock inválido.");
        }
    }

    private void clearForm() {
        nameField.clear();
        doseField.clear();
        qtyField.clear();
        expiryPicker.setValue(null);
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
