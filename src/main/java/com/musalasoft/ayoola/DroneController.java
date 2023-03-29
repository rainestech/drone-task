package com.musalasoft.ayoola;

import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.services.DroneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drones")
public class DroneController {
    private final DroneService service;

    public DroneController(DroneService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Drones>> index() {
        return ResponseEntity.ok(service.getDrones());
    }

    @GetMapping("/sn/{serialNumber}")
    public ResponseEntity<Drones> getDroneBySerialNumber(@PathVariable String serialNumber) {
        Drones drone = service.getDrone(serialNumber);

        if (drone == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone Not Found");

        return ResponseEntity.ok(drone);
    }

    @PostMapping
    public ResponseEntity<Drones> saveDrone(@Valid @RequestBody Drones data) {
        if (service.droneExists(data))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Drone exists in the database");

        return ResponseEntity.ok(service.saveDrone(data));
    }

    @PutMapping
    public ResponseEntity<Drones> editDrone(@Valid @RequestBody Drones data) {
        return ResponseEntity.ok(service.saveDrone(data));
    }

    @DeleteMapping("/remove/{serialNumber}")
    public ResponseEntity<Drones> delete(@PathVariable String serialNumber) {
        return ResponseEntity.ok(service.deleteDrone(serialNumber));
    }
}
