package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryStore {
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private InMemoryStore() {
    }

    public static Map<String, Room> rooms() {
        return rooms;
    }

    public static Map<String, Sensor> sensors() {
        return sensors;
    }

    public static List<SensorReading> readingsFor(String sensorId) {
        return sensorReadings.computeIfAbsent(sensorId, key -> new CopyOnWriteArrayList<>());
    }
}
