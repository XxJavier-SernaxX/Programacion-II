package com.example.gui;

import com.example.EPSService;
import com.example.Paciente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class PatientListController {

    private EPSService service;

    @FXML
    private TableView<Paciente> patientTable;
    @FXML
    private TableColumn<Paciente, String> colId;
    @FXML
    private TableColumn<Paciente, String> colName;
    @FXML
    private TableColumn<Paciente, Integer> colAge;
    @FXML
    private TableColumn<Paciente, String> colEps;
    @FXML
    private TableColumn<Paciente, String> colPhone;

    public void setService(EPSService service) {
        this.service = service;
        cargarDatos();
    }

    private void cargarDatos() {
        if (service != null) {
            ObservableList<Paciente> data = FXCollections.observableArrayList(service.listarPacientes());
            patientTable.setItems(data);
        }
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("edad"));
        colEps.setCellValueFactory(new PropertyValueFactory<>("eps"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("telefono"));
    }
}
