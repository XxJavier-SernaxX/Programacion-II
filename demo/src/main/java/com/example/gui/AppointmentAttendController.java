package com.example.gui;

import com.example.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.util.ArrayList;
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

    // Prescription UI
    @FXML
    private ComboBox<Medicamento> medsCombo;
    @FXML
    private TextField medQtyField;
    @FXML
    private TableView<HistoriaClinica.MedicamentoPrescrito> prescribedMedsTable;
    @FXML
    private TableColumn<HistoriaClinica.MedicamentoPrescrito, String> colMedName;
    @FXML
    private TableColumn<HistoriaClinica.MedicamentoPrescrito, Integer> colMedQty;

    private final List<HistoriaClinica.MedicamentoPrescrito> tempPrescriptions = new ArrayList<>();

    public void setService(EPSService service) {
        this.service = service;
        loadDoctors();
        loadMeds();
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

    private void loadMeds() {
        if (service == null)
            return;
        // Only load meds with stock > 0
        List<Medicamento> availableMeds = service.listarMedicamentos().stream()
                .filter(m -> m.getCantidadDisponible() > 0)
                .collect(Collectors.toList());
        medsCombo.setItems(FXCollections.observableArrayList(availableMeds));
        medsCombo.setConverter(new StringConverter<Medicamento>() {
            @Override
            public String toString(Medicamento m) {
                return m == null ? "" : m.getNombre() + " (" + m.getDosis() + ") - Stock: " + m.getCantidadDisponible();
            }

            @Override
            public Medicamento fromString(String string) {
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
                tempPrescriptions.clear();
                prescribedMedsTable.getItems().clear();
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

        // Setup Prescription Table
        colMedName.setCellValueFactory(cell -> {
            // Find med name manually since MedicamentoPrescrito only has ID
            Optional<Medicamento> m = service.listarMedicamentos().stream()
                    .filter(med -> med.getId().equals(cell.getValue().getMedicamentoId()))
                    .findFirst();
            return new SimpleStringProperty(m.map(Medicamento::getNombre).orElse("Unknown"));
        });
        colMedQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getCantidad()).asObject());
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
    protected void onAddMed() {
        Medicamento m = medsCombo.getValue();
        String qtyTxt = medQtyField.getText();

        if (m == null || qtyTxt.isBlank()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Datos Faltantes", "Seleccione medicamento y cantidad.");
            return;
        }

        try {
            int qty = Integer.parseInt(qtyTxt);
            if (qty <= 0) {
                mostrarAlerta(Alert.AlertType.WARNING, "Cantidad Inválida", "La cantidad debe ser mayor a 0.");
                return;
            }
            if (qty > m.getCantidadDisponible()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Stock Insuficiente",
                        "Solo hay " + m.getCantidadDisponible() + " unidades disponibles.");
                return;
            }

            // Check if already added
            boolean exists = tempPrescriptions.stream().anyMatch(p -> p.getMedicamentoId().equals(m.getId()));
            if (exists) {
                mostrarAlerta(Alert.AlertType.WARNING, "Duplicado", "Ya agregó este medicamento a la lista.");
                return;
            }

            HistoriaClinica.MedicamentoPrescrito pres = new HistoriaClinica.MedicamentoPrescrito(m.getId(), qty);
            tempPrescriptions.add(pres);
            prescribedMedsTable.setItems(FXCollections.observableArrayList(tempPrescriptions));

            medQtyField.clear();
            medsCombo.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Cantidad inválida.");
        }
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

        boolean success = service.atenderCita(selectedCita.getId(), diagnosis, treatment,
                new ArrayList<>(tempPrescriptions));

        if (success) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Cita Finalizada",
                    "La cita ha sido registrada y medicamentos descontados.");
            diagnosisArea.clear();
            treatmentArea.clear();
            tempPrescriptions.clear();
            prescribedMedsTable.getItems().clear();
            loadMeds(); // Refresh stock in combo
            onDoctorSelect(); // Refresh appointments
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo finalizar la cita (verifique stock o estado).");
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
