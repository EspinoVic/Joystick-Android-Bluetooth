package com.example.controlador;

public class Device {

    String info;
    String addres;

    public Device(String info, String addres) {
        this.info = info;
        this.addres = addres;
    }

    public String getInfo() {
        return info;
    }

    public String getAddres() {
        return addres;
    }
}
