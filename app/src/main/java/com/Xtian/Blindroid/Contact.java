package com.Xtian.Blindroid;

public class Contact {
    String name;
    String fullName;
    String phone;
    String id;

    public Contact(String name, String fullName, String phone, String id) {
        this.name = name;
        this.fullName = fullName;
        this.phone = phone;
        this.id = id;

    }

    public String getName() {
        return name;
    }

    public void setName(String nombre) {
        this.name = nombre;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return this.fullName;
    }
}
