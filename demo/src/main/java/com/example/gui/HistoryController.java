package com.example.gui;

import com.example.EPSService;
import com.example.HistoriaClinica;
import com.example.Paciente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import javafx.scene.control.TextArea;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class HistoryController {

    private EPSService service;

    @FXML
    private javafx.scene.control.ListView<Paciente> patientList;
    @FXML
    private TextArea detailsArea;

    public void setService(EPSService service) {
        this.service = service;
        loadPatients();
    }

    @FXML
    public void initialize() {
        patientList.setCellFactory(param -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Paciente item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getIdentificacion() + " - " + item.getNombre());
                }
            }
        });

        patientList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                showHistory(newVal);
        });
    }

    private void loadPatients() {
        if (service == null)
            return;
        patientList.setItems(FXCollections.observableArrayList(service.listarPacientes()));
    }

    private void showHistory(Paciente p) {
        System.out.println("DEBUG: showHistory called for " + p.getNombre());
        Optional<HistoriaClinica> opt = service.consultarHistoria(p.getId());
        if (opt.isEmpty() || opt.get().verHistorial().isEmpty()) {
            System.out.println("DEBUG: No history found.");
            detailsArea.setText("No hay historia clínica registrada para este paciente.");
            detailsArea.setStyle("-fx-text-fill: black;"); // Force color
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Historia Clínica de ").append(p.getNombre()).append("\n");
        sb.append("=====================================\n\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (HistoriaClinica.Consulta c : opt.get().verHistorial()) {
            sb.append("Fecha: ").append(c.getFecha().format(fmt)).append("\n");

            // Look up doctor details
            java.util.Optional<com.example.Doctor> docOpt = service.listarDoctores().stream()
                    .filter(d -> d.getId().equals(c.getDoctorId()))
                    .findFirst();

            if (docOpt.isPresent()) {
                com.example.Doctor doc = docOpt.get();
                sb.append("Doctor: ").append(doc.getNombre())
                        .append(" (").append(doc.getEspecialidad()).append(")\n");
            } else {
                sb.append("Doctor ID: ").append(c.getDoctorId()).append("\n");
            }

            sb.append("Diagnóstico: ").append(c.getDiagnostico()).append("\n");
            sb.append("Tratamiento: ").append(c.getTratamiento()).append("\n");
            sb.append("-------------------------------------\n");
        }

        String finalContent = sb.toString();
        System.out.println("DEBUG: Setting content:\n" + finalContent);
        detailsArea.setText(finalContent);
        detailsArea.setStyle("-fx-text-fill: black;"); // Force color
    }
}
