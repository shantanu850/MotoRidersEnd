package com.gcf.motoriders;

public class List_Data {
    private String email;
    private String name;

    public List_Data(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}