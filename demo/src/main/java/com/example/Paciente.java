package com.example;

import java.util.UUID;

public class Paciente {
    private final UUID id;
    private String identificacion;
    private String nombre;
    private int edad;
    private String direccion;
    private String telefono;
    private String eps;

    public Paciente(String identificacion, String nombre, int edad, String direccion, String telefono, String eps) {
        this.id = UUID.randomUUID();
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.edad = edad;
        this.direccion = direccion;
        this.telefono = telefono;
        this.eps = eps;
    }

    public UUID getId() { return id; }
    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getEps() { return eps; }
    public void setEps(String eps) { this.eps = eps; }

    @Override
    public String toString() {
        return String.format("Paciente[%s - %s, %d años]", identificacion, nombre, edad);
    }
}