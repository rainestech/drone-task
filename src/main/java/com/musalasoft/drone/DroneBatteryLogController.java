package com.musalasoft.drone;

import com.musalasoft.drone.entity.DroneBatteryEventLog;
import com.musalasoft.drone.services.DroneBatteryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/drones/battery/log")
public class DroneBatteryLogController {
    private final DroneBatteryService service;

    public DroneBatteryLogController(DroneBatteryService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<DroneBatteryEventLog>> index() {
        return ResponseEntity.ok(service.getBatteryEvent());
    }

    @GetMapping("/{droneSn}")
    public ResponseEntity<List<DroneBatteryEventLog>> getByDroneSerialNumber(@PathVariable String droneSn) {
        return ResponseEntity.ok(service.getDronesEventLog(droneSn));
    }
}
