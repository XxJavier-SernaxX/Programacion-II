package com.example.gui;

import com.example.Doctor;
import com.example.EPSService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class DoctorListController {

    private EPSService service;

    @FXML
    private TableView<Doctor> doctorTable;
    @FXML
    private TableColumn<Doctor, String> colId;
    @FXML
    private TableColumn<Doctor, String> colName;
    @FXML
    private TableColumn<Doctor, String> colSpecialty;

    public void setService(EPSService service) {
        this.service = service;
        cargarDatos();
    }

    private void cargarDatos() {
        if (service != null) {
            ObservableList<Doctor> data = FXCollections.observableArrayList(service.listarDoctores());
            doctorTable.setItems(data);
        }
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("identificacion"));
        colName.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSpecialty.setCellValueFactory(new PropertyValueFactory<>("especialidad"));
    }
}
