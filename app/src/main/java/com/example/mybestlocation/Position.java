package com.example.mybestlocation;

public class Position {
    int id;
    private String latitude;
    private String longitude;
    private String description;

    public Position(String latitude, String longitude, String description) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    public Position(int id, String latitude, String longitude, String description) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Position{" +
                "id=" + id +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDescription() {
        return description;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
