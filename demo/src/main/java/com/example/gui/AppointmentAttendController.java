package com.example.gui;

import com.example.Cita;
import com.example.Doctor;
import com.example.EPSService;
import com.example.Paciente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AppointmentAttendController {

    private EPSService service;

    @FXML
    private ComboBox<Doctor> doctorCombo;
    @FXML
    private ListView<Cita> appointmentsList;
    @FXML
    private TextArea diagnosisArea;
    @FXML
    private TextArea treatmentArea;
    @FXML
    private Button finishButton;

    public void setService(EPSService service) {
        this.service = service;
        loadDoctors();
    }

    private void loadDoctors() {
        if (service == null)
            return;
        doctorCombo.setItems(FXCollections.observableArrayList(service.listarDoctores()));
        doctorCombo.setConverter(new StringConverter<Doctor>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : d.getNombre() + " (" + d.getEspecialidad() + ")";
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void initialize() {
        // Enable finish button only when an appointment is selected
        appointmentsList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean selected = newVal != null;
            finishButton.setDisable(!selected);
            if (!selected) {
                diagnosisArea.clear();
                treatmentArea.clear();
            }
        });

        // Custom cell factory for Cita list to show patient name and time
        appointmentsList.setCellFactory(param -> new ListCell<Cita>() {
            @Override
            protected void updateItem(Cita item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || service == null) {
                    setText(null);
                } else {
                    // We need to fetch patient name. Ideally Cita should have it or we fetch it.
                    // Accessing service here might be slow for long lists but okay for demo.
                    Optional<Paciente> p = service.listarPacientes().stream()
                            .filter(pac -> pac.getId().equals(item.getPacienteId()))
                            .findFirst();
                    String pName = p.map(Paciente::getNombre).orElse("Desconocido");
                    setText(item.getFechaHora().toLocalTime() + " - " + pName);
                }
            }
        });
    }

    @FXML
    protected void onDoctorSelect() {
        Doctor selectedDoc = doctorCombo.getValue();
        if (selectedDoc == null) {
            appointmentsList.getItems().clear();
            return;
        }

        // Filter appointments: Same Doctor, Status PENDIENTE, Not Cancelled
        // Note: For a real app we might filter by Date too. For now show ALL pending
        // for this doctor.
        List<Cita> pending = service.listarCitas().stream()
                .filter(c -> c.getDoctorId().equals(selectedDoc.getId()))
                .filter(c -> c.getEstado() == Cita.Estado.PENDIENTE)
                .collect(Collectors.toList());

        appointmentsList.setItems(FXCollections.observableArrayList(pending));
    }

    @FXML
    protected void onAttend() {
        Cita selectedCita = appointmentsList.getSelectionModel().getSelectedItem();
        String diagnosis = diagnosisArea.getText();
        String treatment = treatmentArea.getText();

        if (selectedCita == null)
            return;

        if (diagnosis.isBlank() || treatment.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos faltantes", "Debe ingresar diagnóstico y tratamiento.");
            return;
        }

        boolean success = service.atenderCita(selectedCita.getId(), diagnosis, treatment, null); // No meds for now

        if (success) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cita Finalizada",
                    "La cita ha sido registrada en la historia clínica.");
            diagnosisArea.clear();
            treatmentArea.clear();
            onDoctorSelect(); // Refresh list
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo finalizar la cita.");
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
