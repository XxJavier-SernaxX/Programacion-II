package com.example.gui;

import com.example.Doctor;
import com.example.EPSService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DoctorController {

    private EPSService service;

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> espCombo;
    @FXML
    private TextField diasField; // e.g., "1,2,3"
    @FXML
    private TextField horaInicioField; // e.g., "08:00"
    @FXML
    private TextField horaFinField; // e.g., "17:00"

    public void setService(EPSService service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        espCombo.getItems().addAll("Dentista", "General", "Opt\u00F3metra", "Nutricionista");
    }

    @FXML
    protected void onSave() {
        try {
            String id = idField.getText();
            String name = nameField.getText();
            String esp = espCombo.getValue();

            // VALIDACIÓN: Campos vacíos
            if (id == null || id.isBlank() ||
                    name == null || name.isBlank() ||
                    esp == null || esp.isBlank()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos",
                        "Por favor llene ID, Nombre y Especialidad.");
                return;
            }

            Doctor d = new Doctor(id, name, esp);

            // Horario Logic (Simplified for text fields)
            String diasTxt = diasField.getText();
            String inicioTxt = horaInicioField.getText();
            String finTxt = horaFinField.getText();

            if (!diasTxt.isEmpty()) {
                if (inicioTxt.isEmpty() || finTxt.isEmpty()) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Horario incompleto",
                            "Si define días, debe definir hora inicio y fin.");
                    return;
                }
                List<Integer> dias = parseListaEnteros(diasTxt);
                LocalTime inicio = LocalTime.parse(inicioTxt);
                LocalTime fin = LocalTime.parse(finTxt);

                for (int dia : dias) {
                    d.agregarHorario(DayOfWeek.of(dia), inicio, fin);
                }
            }

            service.registrarDoctor(d);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Doctor registrado correctamente.");
            limpiarCampos();
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al guardar: " + e.getMessage());
        }
    }

    private List<Integer> parseListaEnteros(String s) {
        List<Integer> res = new ArrayList<>();
        if (s == null || s.isBlank())
            return res;
        for (String p : s.split(",")) {
            try {
                res.add(Integer.parseInt(p.trim()));
            } catch (Exception ignored) {
            }
        }
        return res;
    }

    private void limpiarCampos() {
        idField.clear();
        nameField.clear();
        espCombo.getSelectionModel().clearSelection();
        diasField.clear();
        horaInicioField.clear();
        horaFinField.clear();
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
