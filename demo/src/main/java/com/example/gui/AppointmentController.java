package com.example.gui;

import com.example.Doctor;

import com.example.EPSService;
import com.example.Paciente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentController {

    private EPSService service;

    @FXML
    private ComboBox<Paciente> patientCombo;
    @FXML
    private ComboBox<Doctor> doctorCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField durationField; // Default 30 min
    @FXML
    private TextField reasonField;
    @FXML
    private ListView<String> availabilityList;
    @FXML
    private Button bookButton;
    @FXML
    private Button checkButton; // To explicitly check

    public void setService(EPSService service) {
        this.service = service;
        loadData();
    }

    @FXML
    public void initialize() {
        durationField.setText("30");
        durationField.setText("30");
        // bookButton is enabled by default now to allow validation feedback

        // Setup Combo converters
        patientCombo.setConverter(new StringConverter<Paciente>() {
            @Override
            public String toString(Paciente p) {
                return p == null ? "" : p.getIdentificacion() + " - " + p.getNombre();
            }

            @Override
            public Paciente fromString(String string) {
                return null;
            }
        });

        doctorCombo.setConverter(new StringConverter<Doctor>() {
            @Override
            public String toString(Doctor d) {
                return d == null ? "" : d.getIdentificacion() + " - " + d.getNombre();
            }

            @Override
            public Doctor fromString(String string) {
                return null;
            }
        });

        // Logic to clear book button disable state removed
    }

    private void loadData() {
        if (service == null)
            return;
        System.out.println("DEBUG: Loading data into AppointmentController");
        System.out.println("DEBUG: Patients count: " + service.listarPacientes().size());
        System.out.println("DEBUG: Doctors count: " + service.listarDoctores().size());
        patientCombo.setItems(FXCollections.observableArrayList(service.listarPacientes()));
        doctorCombo.setItems(FXCollections.observableArrayList(service.listarDoctores()));
    }

    @FXML
    private Label scheduleLabel;

    @FXML
    protected void onCheckAvailability() {
        Doctor doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String durTxt = durationField.getText();

        // RF Update: Allow checking general schedule without date
        if (doctor == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos", "Seleccione un doctor.");
            return;
        }

        // Show general schedule
        StringBuilder sb = new StringBuilder("Horario General de " + doctor.getNombre() + ":\n");
        boolean hasSchedule = false;

        // Helper for Spanish day names
        java.time.format.TextStyle style = java.time.format.TextStyle.FULL;
        java.util.Locale es = new java.util.Locale("es", "ES");

        for (java.time.DayOfWeek d : java.time.DayOfWeek.values()) {
            List<Doctor.Horario> horarios = doctor.getHorariosDelDia(d);
            if (!horarios.isEmpty()) {
                hasSchedule = true;
                // Capitalize first letter
                String dayName = d.getDisplayName(style, es);
                dayName = dayName.substring(0, 1).toUpperCase() + dayName.substring(1);

                sb.append(dayName).append(": ");
                for (Doctor.Horario h : horarios) {
                    sb.append(h).append(" ");
                }
                sb.append("\n");
            }
        }
        if (!hasSchedule) {
            sb.append("No tiene horarios registrados.\n");
        }

        if (scheduleLabel != null)
            scheduleLabel.setText(sb.toString());

        // Validate duration for slots
        int mins = 30;
        try {
            if (!durTxt.isBlank()) {
                mins = Integer.parseInt(durTxt);
            }
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Duración inválida.");
            return;
        }

        // If date is selected, check specific availability
        if (date != null) {
            List<String> slots = service.obtenerDisponibilidad(doctor.getId(), date, Duration.ofMinutes(mins));
            availabilityList.setItems(FXCollections.observableArrayList(slots));

            if (slots.isEmpty() || (slots.size() == 1 && slots.get(0).startsWith("No"))) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Sin disponibilidad",
                        "No se encontraron horarios con los criterios.");
            }
        } else {
            // Clear or set hint
            availabilityList.setItems(FXCollections.observableArrayList("Seleccione fecha para ver cupos."));
        }
    }

    @FXML
    protected void onBook() {
        Paciente patient = patientCombo.getValue();
        Doctor doctor = doctorCombo.getValue();
        LocalDate date = datePicker.getValue();
        String selectedSlot = availabilityList.getSelectionModel().getSelectedItem();
        String reason = reasonField.getText();
        String durTxt = durationField.getText();

        if (patient == null || doctor == null || date == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos incompletos",
                    "Por favor seleccione paciente, doctor y fecha.");
            return;
        }

        if (selectedSlot == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Horario no seleccionado",
                    "Debe consultar disponibilidad y seleccionar un horario de la lista antes de confirmar.");
            return;
        }

        // Parse start time from slot string "HH:mm - HH:mm"
        try {
            String[] parts = selectedSlot.split("-");
            LocalTime time = LocalTime.parse(parts[0].trim());
            LocalDateTime dateTime = date.atTime(time);
            int mins = Integer.parseInt(durTxt);

            String validation = service.validarCrearCita(patient.getId(), doctor.getId(), dateTime,
                    Duration.ofMinutes(mins));
            if (!validation.equals("OK")) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error Validación", validation);
                return;
            }

            var cita = service.crearCita(patient.getId(), doctor.getId(), dateTime, Duration.ofMinutes(mins), reason);
            if (cita.isPresent()) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cita agendada correctamente.");
                onCheckAvailability(); // Refresh
                reasonField.clear();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo agendar la cita.");
            }
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al procesar: " + e.getMessage());
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
