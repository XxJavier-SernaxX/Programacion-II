package com.example.gui;

import com.example.EPS;
import com.example.EPSService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import java.io.IOException;

public class MainController {

    private final EPSService service;

    @FXML
    private BorderPane mainLayout;

    @FXML
    private Label welcomeLabel;

    public MainController() {
        EPS eps = new EPS("MiEPS");
        this.service = new EPSService(eps);
        inicializarDatosPrueba();
    }

    private void inicializarDatosPrueba() {
        // Doctores
        // Doctores
        com.example.Doctor d1 = new com.example.Doctor("DOC001", "Dr. Gregory House", "General");
        com.example.Doctor d2 = new com.example.Doctor("DOC002", "Dra. Meredith Grey", "General");
        com.example.Doctor d3 = new com.example.Doctor("DOC003", "Dr. Derek Shepherd", "Neurología");
        com.example.Doctor d4 = new com.example.Doctor("DOC004", "Dr. Strange", "Opt\u00F3metria");

        // Add schedules (Monday to Friday, 8am - 5pm)
        java.time.LocalTime start = java.time.LocalTime.of(8, 0);
        java.time.LocalTime end = java.time.LocalTime.of(17, 0);
        // Only Mon-Fri as per user request "horario de oficina de lunes a viernes"
        for (java.time.DayOfWeek day : java.time.DayOfWeek.values()) {
            if (day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY)
                continue;

            d1.agregarHorario(day, start, end);
            d2.agregarHorario(day, start, end);
            d3.agregarHorario(day, start, end);
            d4.agregarHorario(day, start, end);
        }

        service.registrarDoctor(d1);
        service.registrarDoctor(d2);
        service.registrarDoctor(d3);
        service.registrarDoctor(d4);

        // Pacientes
        service.registrarPaciente(
                new com.example.Paciente("1001", "Pepito Pérez", 30, "Calle 123", "555-0101", "Sanitas"));
        service.registrarPaciente(
                new com.example.Paciente("1002", "María López", 25, "Carrera 45", "555-0102", "Sura"));
        service.registrarPaciente(
                new com.example.Paciente("1003", "Juan Rodriguez", 45, "Av Siempre Viva", "555-0103", "Compensar"));
        service.registrarPaciente(
                new com.example.Paciente("1004", "Ana García", 60, "Calle False 123", "555-0104", "Famisanar"));

        // Medicamentos
        // Names: Acetaminofén, Ibuprofeno, Amoxicilina, Loratadina, Omeprazol,
        // Losartán, Salbutamol, Metformina
        java.time.LocalDate exp = java.time.LocalDate.now().plusYears(1);
        service.agregarMedicamento(new com.example.Medicamento("Acetaminofén", "500mg", 100, exp));
        service.agregarMedicamento(new com.example.Medicamento("Ibuprofeno", "400mg", 50, exp));
        service.agregarMedicamento(new com.example.Medicamento("Amoxicilina", "500mg", 30, exp));
        service.agregarMedicamento(new com.example.Medicamento("Loratadina", "10mg", 100, exp));
        service.agregarMedicamento(new com.example.Medicamento("Omeprazol", "20mg", 60, exp));
        service.agregarMedicamento(new com.example.Medicamento("Losartán", "50mg", 40, exp));
        service.agregarMedicamento(new com.example.Medicamento("Salbutamol", "Inhalador", 20, exp));
        service.agregarMedicamento(new com.example.Medicamento("Metformina", "850mg", 80, exp));

        // Historias Clínicas (Crear citas futuras cercanas y atenderlas inmediatamente
        // para generar historial)
        try {
            // Find IDs
            java.util.UUID docId = service.listarDoctores().stream().filter(d -> d.getNombre().contains("House"))
                    .findFirst().orElse(
                            service.listarDoctores().stream().findFirst().get())
                    .getId();

            // Paciente 1: Pepito -> Historia de Gripe
            java.util.UUID pacId1 = service.listarPacientes().stream().filter(p -> p.getNombre().contains("Pepito"))
                    .findFirst().get().getId();
            java.time.LocalDateTime date1 = java.time.LocalDateTime.now().plusHours(1); // Future to pass validation

            var cita1Opt = service.crearCita(pacId1, docId, date1, java.time.Duration.ofMinutes(30),
                    "Malestar general");
            if (cita1Opt.isPresent()) {
                // Prescribe Ibuprofeno
                java.util.UUID ibuId = service.listarMedicamentos().stream()
                        .filter(m -> m.getNombre().contains("Ibuprofeno")).findFirst().get().getId();
                java.util.List<com.example.HistoriaClinica.MedicamentoPrescrito> meds = new java.util.ArrayList<>();
                meds.add(new com.example.HistoriaClinica.MedicamentoPrescrito(ibuId, 10));

                service.atenderCita(cita1Opt.get().getId(), "Gripe Estacional", "Reposo y líquidos", meds);
            }

            // Paciente 2: Maria -> Historia de Migraña
            java.util.UUID pacId2 = service.listarPacientes().stream().filter(p -> p.getNombre().contains("María"))
                    .findFirst().get().getId();
            java.time.LocalDateTime date2 = java.time.LocalDateTime.now().plusHours(2);

            var cita2Opt = service.crearCita(pacId2, docId, date2, java.time.Duration.ofMinutes(30),
                    "Dolor de cabeza fuerte");
            if (cita2Opt.isPresent()) {
                // Prescribe Acetaminofen
                java.util.UUID acetId = service.listarMedicamentos().stream()
                        .filter(m -> m.getNombre().contains("Acetaminofén")).findFirst().get().getId();
                java.util.List<com.example.HistoriaClinica.MedicamentoPrescrito> meds = new java.util.ArrayList<>();
                meds.add(new com.example.HistoriaClinica.MedicamentoPrescrito(acetId, 2));

                service.atenderCita(cita2Opt.get().getId(), "Migraña Tensional",
                        "Evitar luz fuerte, Acetaminofén cada 8h", meds);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void initialize() {
        onInicioClick();
    }

    @FXML
    protected void onInicioClick() {
        cargarVista("welcome.fxml");
    }

    @FXML
    protected void onRegistrarPacienteClick() {
        cargarVista("patient_register.fxml");
    }

    @FXML
    protected void onRegistrarDoctorClick() {
        cargarVista("doctor_register.fxml");
    }

    @FXML
    protected void onListarPacientesClick() {
        cargarVista("patient_list.fxml");
    }

    @FXML
    protected void onListarDoctoresClick() {
        cargarVista("doctor_list.fxml");
    }

    @FXML
    protected void onAgendarCitaClick() {
        cargarVista("appointment_create.fxml");
    }

    @FXML
    protected void onHistoriaClinicaClick() {
        cargarVista("clinical_history.fxml");
    }

    @FXML
    protected void onAtenderCitaClick() {
        cargarVista("appointment_attend.fxml");
    }

    @FXML
    protected void onInventarioClick() {
        cargarVista("drug_inventory.fxml");
    }

    @FXML
    protected void onGestionarCitasClick() {
        cargarVista("appointment_manage.fxml");
    }

    @FXML
    protected void onToggleTheme() {
        if (mainLayout == null || mainLayout.getScene() == null)
            return;

        var stylesheets = mainLayout.getScene().getStylesheets();
        String darkThemePath = getClass().getResource("dark_theme.css").toExternalForm();

        if (stylesheets.contains(darkThemePath)) {
            stylesheets.remove(darkThemePath);
        } else {
            stylesheets.add(darkThemePath);
        }
    }

    private void cargarVista(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            // Cargar la vista
            Object view = loader.load();

            // Si el controlador destino necesita el servicio, se lo pasamos
            Object controller = loader.getController();
            if (controller instanceof PatientController) {
                ((PatientController) controller).setService(this.service);
            } else if (controller instanceof DoctorController) {
                ((DoctorController) controller).setService(this.service);
            } else if (controller instanceof PatientListController) {
                ((PatientListController) controller).setService(this.service);
            } else if (controller instanceof DoctorListController) {
                ((DoctorListController) controller).setService(this.service);
            } else if (controller instanceof AppointmentController) {
                ((AppointmentController) controller).setService(this.service);
            } else if (controller instanceof HistoryController) {
                ((HistoryController) controller).setService(this.service);
            } else if (controller instanceof AppointmentAttendController) {
                ((AppointmentAttendController) controller).setService(this.service);
            } else if (controller instanceof DrugInventoryController) {
                ((DrugInventoryController) controller).setService(this.service);
            } else if (controller instanceof AppointmentManageController) {
                ((AppointmentManageController) controller).setService(this.service);
            } else if (controller instanceof WelcomeController) {
                ((WelcomeController) controller).setService(this.service);
            }

            // Poner la vista en el centro del BorderPane
            mainLayout.setCenter((javafx.scene.Node) view);

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la vista: " + fxmlFile);
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(contenido);
        alert.showAndWait();
    }
}
