package com.example;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

public class Doctor {
    private final UUID id;
    private String identificacion;
    private String nombre;
    private String especialidad;
    // horarios: day -> list de intervalos (from,to)
    private final Map<DayOfWeek, List<Horario>> horarios = new EnumMap<>(DayOfWeek.class);

    public Doctor(String identificacion, String nombre, String especialidad) {
        this.id = UUID.randomUUID();
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.especialidad = especialidad;
    }

    public UUID getId() { return id; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public void agregarHorario(DayOfWeek dia, LocalTime desde, LocalTime hasta) {
        horarios.computeIfAbsent(dia, d -> new ArrayList<>()).add(new Horario(desde, hasta));
    }

    public List<Horario> getHorariosDelDia(DayOfWeek dia) {
        return horarios.getOrDefault(dia, Collections.emptyList());
    }

    @Override
    public String toString() {
        return String.format("Doctor[%s - %s (%s)]", identificacion, nombre, especialidad);
    }

    public static class Horario {
        private final LocalTime desde;
        private final LocalTime hasta;
        public Horario(LocalTime desde, LocalTime hasta) { this.desde = desde; this.hasta = hasta; }
        public LocalTime getDesde() { return desde; }
        public LocalTime getHasta() { return hasta; }
        public boolean contiene(LocalTime tInicio, LocalTime tFin) {
            return !tInicio.isBefore(desde) && !tFin.isAfter(hasta);
        }
        @Override
        public String toString() { return desde + " - " + hasta; }
    }
}