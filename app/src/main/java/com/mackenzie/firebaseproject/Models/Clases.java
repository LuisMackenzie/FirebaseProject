package com.mackenzie.firebaseproject.Models;

public class Clases {

    String seccion, area, tema, claseId;

    public Clases(String seccion, String area, String tema, String claseId) {
        this.seccion = seccion;
        this.area = area;
        this.tema = tema;
        this.claseId = claseId;
    }

    public Clases() {

    }

    public String getSeccion() {
        return seccion;
    }

    public String getArea() {
        return area;
    }

    public String getTema() {
        return tema;
    }

    public String getClaseId() {
        return claseId;
    }
}
