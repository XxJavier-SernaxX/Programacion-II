package com.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Cita {
    public enum Estado { PENDIENTE, ATENDIDA, CANCELADA }

    private final UUID id;
    private final UUID pacienteId;
    private final UUID doctorId;
    private LocalDateTime fechaHora;
    private Duration duracion;
    private String motivo;
    private Estado estado;

    public Cita(UUID pacienteId, UUID doctorId, LocalDateTime fechaHora, Duration duracion, String motivo) {
        this.id = UUID.randomUUID();
        this.pacienteId = pacienteId;
        this.doctorId = doctorId;
        this.fechaHora = fechaHora;
        this.duracion = duracion;
        this.motivo = motivo;
        this.estado = Estado.PENDIENTE;
    }

    public UUID getId() { return id; }
    public UUID getPacienteId() { return pacienteId; }
    public UUID getDoctorId() { return doctorId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public Duration getDuracion() { return duracion; }
    public void setDuracion(Duration duracion) { this.duracion = duracion; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public LocalDateTime getFin() { return fechaHora.plus(duracion); }

    public boolean solapaCon(Cita otra) {
        if (!this.doctorId.equals(otra.doctorId)) return false;
        return this.fechaHora.isBefore(otra.getFin()) && otra.getFechaHora().isBefore(this.getFin());
    }

    @Override
    public String toString() {
        return String.format("Cita[%s doctor=%s paciente=%s %s estado=%s]", id, doctorId, pacienteId, fechaHora, estado);
    }
}