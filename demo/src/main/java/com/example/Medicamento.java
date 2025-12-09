package com.example;

import java.time.LocalDate;
import java.util.UUID;

public class Medicamento {
    private final UUID id;
    private String nombre;
    private String dosis;
    private int cantidadDisponible;
    private LocalDate fechaVencimiento;

    public Medicamento(String nombre, String dosis, int cantidadDisponible, LocalDate fechaVencimiento) {
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.dosis = dosis;
        this.cantidadDisponible = cantidadDisponible;
        this.fechaVencimiento = fechaVencimiento;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDosis() { return dosis; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }

    public void actualizarStock(int nuevaCantidad) {
        this.cantidadDisponible = nuevaCantidad;
    }

    public void dispensar(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad inválida");
        if (cantidad > cantidadDisponible) throw new IllegalStateException("Stock insuficiente para " + nombre);
        cantidadDisponible -= cantidad;
    }

    @Override
    public String toString() {
        return String.format("Medicamento[%s %s qty=%d exp=%s]", nombre, dosis, cantidadDisponible, fechaVencimiento);
    }
}