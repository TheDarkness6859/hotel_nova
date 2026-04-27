package com.hotelNova.controllers;

import com.hotelNova.models.Room;
import com.hotelNova.services.GenericService;
import com.hotelNova.services.impl.RoomServiceImpl;
import java.util.List;

public class RoomController {

    private final GenericService<Room> roomService = new RoomServiceImpl();

    public List<Room> getAvailableRooms() {

        return roomService.findAll().stream()
                .filter(Room::isAvailable)
                .toList();

    }

    public boolean saveRoom(Room room) {

        return roomService.save(room);

    }

    public boolean updateRoom(Room room) {

        return roomService.edit(room);

    }
}