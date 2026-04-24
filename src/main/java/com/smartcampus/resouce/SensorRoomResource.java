package com.smartcampus.resouce;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.store.InMemoryStore;
import com.smartcampus.model.Room;
import com.smartcampus.model.ErrorResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Path("rooms")
@Produces(MediaType.APPLICATION_JSON)
public class SensorRoomResource {

    @GET
    public List<Room> getRooms() {
        return new ArrayList<>(InMemoryStore.rooms().values());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null || isBlank(room.getId()) || isBlank(room.getName())) {
            throw new WebApplicationException(
                    Response.status(Response.Status.BAD_REQUEST)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("BAD_REQUEST", "Room id and name are required."))
                            .build()
            );
        }

        room.setSensorIds(new CopyOnWriteArrayList<>());

        Room existing = InMemoryStore.rooms().putIfAbsent(room.getId(), room);
        if (existing != null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("ROOM_EXISTS", "A room with this id already exists."))
                            .build()
            );
        }

        return Response.status(Response.Status.CREATED).entity(room).build();
    }

    @GET
    @Path("{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.rooms().get(roomId);
        if (room == null) {
            throw new WebApplicationException(
                    Response.status(Response.Status.NOT_FOUND)
                            .type(MediaType.APPLICATION_JSON)
                            .entity(new ErrorResponse("ROOM_NOT_FOUND", "Room " + roomId + " was not found."))
                            .build()
            );
        }
        return room;
    }

    @DELETE
    @Path("{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = InMemoryStore.rooms().get(roomId);
        if (room == null) {
            return Response.noContent().build();
        }

        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room " + roomId + " still has active sensors and cannot be deleted.");
        }

        InMemoryStore.rooms().remove(roomId);
        return Response.noContent().build();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
