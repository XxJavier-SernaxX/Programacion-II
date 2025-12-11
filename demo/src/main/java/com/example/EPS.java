package com.example;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EPS {
    private final String nombre;
    // repos simples en memoria
    private final Map<UUID, Paciente> pacientes = new HashMap<>();
    private final Map<UUID, Doctor> doctores = new HashMap<>();
    private final Map<UUID, Cita> citas = new HashMap<>();
    private final Map<UUID, Medicamento> medicamentos = new HashMap<>();
    private final Map<UUID, HistoriaClinica> historias = new HashMap<>();

    public EPS(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    // pacientes
    public void registrarPaciente(Paciente p) {
        pacientes.put(p.getId(), p);
        historias.putIfAbsent(p.getId(), new HistoriaClinica(p.getId()));
    }

    public Optional<Paciente> obtenerPaciente(UUID id) {
        return Optional.ofNullable(pacientes.get(id));
    }

    public Collection<Paciente> listarPacientes() {
        return Collections.unmodifiableCollection(pacientes.values());
    }

    // doctores
    public void registrarDoctor(Doctor d) {
        doctores.put(d.getId(), d);
    }

    public Optional<Doctor> obtenerDoctor(UUID id) {
        return Optional.ofNullable(doctores.get(id));
    }

    public Collection<Doctor> listarDoctores() {
        return Collections.unmodifiableCollection(doctores.values());
    }

    // citas
    public void guardarCita(Cita c) {
        citas.put(c.getId(), c);
    }

    public Optional<Cita> obtenerCita(UUID id) {
        return Optional.ofNullable(citas.get(id));
    }

    public Collection<Cita> listarCitas() {
        return Collections.unmodifiableCollection(citas.values());
    }

    // medicamentos
    public void guardarMedicamento(Medicamento m) {
        medicamentos.put(m.getId(), m);
    }

    public Optional<Medicamento> obtenerMedicamento(UUID id) {
        return Optional.ofNullable(medicamentos.get(id));
    }

    public Collection<Medicamento> listarMedicamentos() {
        return Collections.unmodifiableCollection(medicamentos.values());
    }

    // historias
    public HistoriaClinica obtenerHistoria(UUID pacienteId) {
        return historias.computeIfAbsent(pacienteId, HistoriaClinica::new);
    }
}