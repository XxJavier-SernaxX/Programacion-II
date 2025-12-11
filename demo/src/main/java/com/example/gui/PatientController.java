package com.example.gui;

import com.example.EPSService;
import com.example.Paciente;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class PatientController {

    private EPSService service;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField ageField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField epsField;

    public void setService(EPSService service) {
        this.service = service;
    }

    @FXML
    protected void onSave() {
        try {
            String id = idField.getText();
            String name = nameField.getText();
            int age = Integer.parseInt(ageField.getText());
            String address = addressField.getText();
            String phone = phoneField.getText();
            String eps = epsField.getText();

            Paciente p = new Paciente(id, name, age, address, phone, eps);
            service.registrarPaciente(p);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Paciente registrado correctamente.");
            limpiarCampos();
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "La edad debe ser un número válido.");
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
        }
    }

    private void limpiarCampos() {
        idField.clear();
        nameField.clear();
        ageField.clear();
        addressField.clear();
        phoneField.clear();
        epsField.clear();
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
