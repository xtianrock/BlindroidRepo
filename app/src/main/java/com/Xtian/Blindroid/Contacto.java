package com.Xtian.Blindroid;

public class Contacto {
    String nombre;
    String nombreOriginal;
    String telefono;
    String id;

    public Contacto(String nombre, String nombreOriginal, String telefono, String id) {
        this.nombre = nombre;
        this.nombreOriginal = nombreOriginal;
        this.telefono = telefono;
        this.id = id;

    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public void setNombreOriginal(String nombreOriginal) {
        this.nombreOriginal = nombreOriginal;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

}
