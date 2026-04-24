package com.smartcampus.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room {
    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds = new CopyOnWriteArrayList<>();

    public Room() {
        this.sensorIds = new CopyOnWriteArrayList<>();
    }

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = new CopyOnWriteArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public List<String> getSensorIds() {
        if (sensorIds == null) {
            sensorIds = new CopyOnWriteArrayList<>();
        }
        return sensorIds;
    }

    public void setSensorIds(List<String> sensorIds) {
        if (sensorIds == null) {
            this.sensorIds = new CopyOnWriteArrayList<>();
        } else {
            this.sensorIds = new CopyOnWriteArrayList<>(sensorIds);
        }
    }
}
