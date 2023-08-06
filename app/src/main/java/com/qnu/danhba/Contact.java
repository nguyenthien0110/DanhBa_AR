package com.qnu.danhba;

public class Contact {
    private int id;
    private String fullname;
    private String phoneNumber;

    public Contact(int id, String fullname, String phoneNumber) {
        this.id = id;
        this.fullname = fullname;
        this.phoneNumber = phoneNumber;
    }

    public int getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

