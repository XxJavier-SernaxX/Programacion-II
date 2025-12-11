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
