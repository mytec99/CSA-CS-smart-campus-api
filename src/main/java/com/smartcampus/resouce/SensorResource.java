package com.smartcampus.resouce;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.store.InMemoryStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.ErrorResponse;
import com.smartcampus.model.Sensor;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Path("sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(InMemoryStore.sensors().values());
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }

        List<Sensor> filtered = new ArrayList<>();
        for (Sensor sensor : allSensors) {
            if (sensor.getType() != null
                    && sensor.getType().toLowerCase(Locale.ROOT).equals(type.toLowerCase(Locale.ROOT))) {
                filtered.add(sensor);
            }
        }
        return filtered;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || isBlank(sensor.getId()) || isBlank(sensor.getType()) || isBlank(sensor.getRoomId())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("BAD_REQUEST", "Sensor id, type, and roomId are required."))
                            .build()
            );
        }

        Room room = InMemoryStore.rooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("The linked roomId " + sensor.getRoomId() + " does not exist.");
        }

        if (isBlank(sensor.getStatus())) {
            sensor.setStatus("ACTIVE");
        }

        Sensor existing = InMemoryStore.sensors().putIfAbsent(sensor.getId(), sensor);
        if (existing != null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("SENSOR_EXISTS", "A sensor with this id already exists."))
                            .build()
            );
        }

        room.getSensorIds().add(sensor.getId());
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    @Path("{sensorId}/readings")
    public SensorReadingResource sensorReadings(@PathParam("sensorId") String sensorId) {
        Sensor sensor = InMemoryStore.sensors().get(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("SENSOR_NOT_FOUND", "Sensor " + sensorId + " was not found."))
                            .build()
            );
        }
        return new SensorReadingResource(sensorId);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
