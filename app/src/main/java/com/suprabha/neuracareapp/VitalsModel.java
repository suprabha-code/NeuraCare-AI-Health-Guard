package com.suprabha.neuracareapp;

public class VitalsModel {
    public int heartRate;
    public float temperature;
    public int spo2;
    public boolean alert;

    public VitalsModel() {
        // Required for Firebase deserialization
    }

    public VitalsModel(int heartRate, float temperature, int spo2, boolean alert) {
        this.heartRate = heartRate;
        this.temperature = temperature;
        this.spo2 = spo2;
        this.alert = alert;
    }

    // Getters and setters (optional but good practice)
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public int getSpo2() { return spo2; }
    public void setSpo2(int spo2) { this.spo2 = spo2; }

    public boolean isAlert() { return alert; }
    public void setAlert(boolean alert) { this.alert = alert; }
}