package com.example.gui;

import com.example.Cita;
import com.example.EPSService;
import com.example.Paciente;
import com.example.Doctor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentManageController {

    private EPSService service;

    @FXML
    private TableView<Cita> appointmentsTable;
    @FXML
    private TableColumn<Cita, String> colDate;
    @FXML
    private TableColumn<Cita, String> colPatient;
    @FXML
    private TableColumn<Cita, String> colDoctor;
    @FXML
    private TableColumn<Cita, String> colStatus;
    @FXML
    private TableColumn<Cita, String> colReason;

    @FXML
    private DatePicker newDatePicker;
    @FXML
    private TextField newTimeField;

    public void setService(EPSService service) {
        this.service = service;
        loadData();
    }

    private void loadData() {
        if (service == null)
            return;
        // Load only PENDING appointments
        var list = service.listarCitas().stream()
                .filter(c -> c.getEstado() == Cita.Estado.PENDIENTE)
                .collect(Collectors.toList());
        appointmentsTable.setItems(FXCollections.observableArrayList(list));
    }

    @FXML
    public void initialize() {
        colDate.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getFechaHora().toString().replace("T", " ")));
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getEstado().toString()));
        colReason.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMotivo()));

        colPatient.setCellValueFactory(cell -> {
            Optional<Paciente> p = service.listarPacientes().stream()
                    .filter(x -> x.getId().equals(cell.getValue().getPacienteId()))
                    .findFirst();
            return new SimpleStringProperty(p.map(Paciente::getNombre).orElse("?"));
        });

        colDoctor.setCellValueFactory(cell -> {
            Optional<Doctor> d = service.listarDoctores().stream()
                    .filter(x -> x.getId().equals(cell.getValue().getDoctorId()))
                    .findFirst();
            return new SimpleStringProperty(d.map(Doctor::getNombre).orElse("?"));
        });

        // Auto-fill form when selected
        appointmentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                newDatePicker.setValue(newVal.getFechaHora().toLocalDate());
                newTimeField.setText(newVal.getFechaHora().toLocalTime().toString());
            } else {
                newDatePicker.setValue(null);
                newTimeField.clear();
            }
        });
    }

    @FXML
    protected void onCancel() {
        Cita selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Seleccione una cita.");
            return;
        }

        boolean ok = service.cancelarCita(selected.getId());
        if (ok) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cita cancelada.");
            loadData();
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo cancelar.");
        }
    }

    @FXML
    protected void onModify() {
        Cita selected = appointmentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Selección", "Seleccione una cita.");
            return;
        }

        LocalDate newDate = newDatePicker.getValue();
        String timeTxt = newTimeField.getText();

        if (newDate == null || timeTxt.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos", "Ingrese nueva fecha y hora.");
            return;
        }

        try {
            LocalTime newTime = LocalTime.parse(timeTxt);
            LocalDateTime newDateTime = LocalDateTime.of(newDate, newTime);

            // Assuming duration and reason stay same for simplify (or could add fields)
            Duration dur = selected.getDuracion();
            String reason = selected.getMotivo();

            boolean ok = service.modificarCita(selected.getId(), newDateTime, dur, reason);
            if (ok) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cita reprogramada.");
                loadData();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error",
                        "No se pudo modificar (Conflicto de horario o fecha pasada).");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error",
                    "Formato de hora inválido (HH:mm) o error: " + e.getMessage());
        }
    }

    private void mostrarAlerta(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
