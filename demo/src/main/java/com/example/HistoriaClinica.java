package com.example;

import java.time.LocalDateTime;
import java.util.*;

public class HistoriaClinica {
    private final UUID pacienteId;
    private final List<Consulta> consultas = new ArrayList<>();

    public HistoriaClinica(UUID pacienteId) {
        this.pacienteId = pacienteId;
    }

    public UUID getPacienteId() { return pacienteId; }

    // No se permite modificar una consulta una vez creada (solo agregar nuevas)
    public void registrarConsulta(LocalDateTime fecha, UUID doctorId, String diagnostico, String tratamiento, List<MedicamentoPrescrito> meds) {
        consultas.add(new Consulta(fecha, doctorId, diagnostico, tratamiento, meds));
    }

    public List<Consulta> verHistorial() {
        return Collections.unmodifiableList(consultas);
    }

    public static class Consulta {
        private final LocalDateTime fecha;
        private final UUID doctorId;
        private final String diagnostico;
        private final String tratamiento;
        private final List<MedicamentoPrescrito> medicamentos;

        public Consulta(LocalDateTime fecha, UUID doctorId, String diagnostico, String tratamiento, List<MedicamentoPrescrito> medicamentos) {
            this.fecha = fecha;
            this.doctorId = doctorId;
            this.diagnostico = diagnostico;
            this.tratamiento = tratamiento;
            this.medicamentos = medicamentos == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(medicamentos));
        }

        public LocalDateTime getFecha() { return fecha; }
        public UUID getDoctorId() { return doctorId; }
        public String getDiagnostico() { return diagnostico; }
        public String getTratamiento() { return tratamiento; }
        public List<MedicamentoPrescrito> getMedicamentos() { return medicamentos; }

        @Override
        public String toString() {
            return String.format("Consulta[%s doctor=%s diag=%s trat=%s meds=%s]", fecha, doctorId, diagnostico, tratamiento, medicamentos);
        }
    }

    public static class MedicamentoPrescrito {
        private final UUID medicamentoId;
        private final int cantidad;
        public MedicamentoPrescrito(UUID medicamentoId, int cantidad) {
            this.medicamentoId = medicamentoId; this.cantidad = cantidad;
        }
        public UUID getMedicamentoId() { return medicamentoId; }
        public int getCantidad() { return cantidad; }
        @Override
        public String toString() { return String.format("Prescrito[%s x%d]", medicamentoId, cantidad); }
    }
}