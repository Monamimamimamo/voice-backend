package org.kurento.tutorial.groupcall;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RoomManager {


    @Autowired
    private KurentoClient kurento;

    private final ConcurrentMap<String, Room> rooms = new ConcurrentHashMap<>();


    public Room getRoom(String roomName) {
        log.debug("Searching for room {}", roomName);
        Room room = rooms.get(roomName);

        if (room == null) {
            log.debug("Room {} not existent.", roomName);
            return null; // или выбросите исключение
        }
        log.debug("Room {} found!", roomName);
        return room;
    }

    public Room createRoom(String roomName) {
        Room room = rooms.get(roomName);
        if (room == null) {
            room = new Room(roomName, kurento.createMediaPipeline());
            rooms.put(roomName, room);
            log.debug("Room {} created!", roomName);
        } else {
            throw new IllegalArgumentException("Room with name " + roomName + " already exists.");
        }
        return room;
    }


    public void removeRoom(Room room) {
        this.rooms.remove(room.getName());
        room.close();
        log.info("Room {} removed and closed", room.getName());
    }

    public Set<String> getRoomNames() {
        return rooms.keySet();
    }
}
