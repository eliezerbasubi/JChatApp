package com.example.eliezer.jchatapp;

/**
 * Created by Eliezer on 12/03/2018.
 */

class RequestClass {

    public  String request_type;

    public RequestClass(){

    }

    public RequestClass(String notifications){
        this.request_type = notifications;
    }

    public String getNotifications() {
        return request_type;
    }

    public void setNotifications(String notifications) {
        this.request_type = notifications;
    }
}
