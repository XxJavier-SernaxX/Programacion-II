package com.example.gui;

import com.example.Cita;
import com.example.EPSService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDate;

public class WelcomeController {

    private EPSService service;

    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label totalDoctorsLabel;
    @FXML
    private Label todayAppointmentsLabel;

    public void setService(EPSService service) {
        this.service = service;
        updateStats();
    }

    private void updateStats() {
        if (service == null)
            return;

        // Count Patients
        int patientCount = service.listarPacientes().size();
        if (totalPatientsLabel != null)
            totalPatientsLabel.setText(String.valueOf(patientCount));

        // Count Doctors
        int doctorCount = service.listarDoctores().size();
        if (totalDoctorsLabel != null)
            totalDoctorsLabel.setText(String.valueOf(doctorCount));

        // Count Today's Appointments
        long todayCount = service.listarCitas().stream()
                .filter(c -> c.getFechaHora().toLocalDate().equals(LocalDate.now()))
                .filter(c -> c.getEstado() != Cita.Estado.CANCELADA)
                .count();

        if (todayAppointmentsLabel != null)
            todayAppointmentsLabel.setText(String.valueOf(todayCount));
    }
}
