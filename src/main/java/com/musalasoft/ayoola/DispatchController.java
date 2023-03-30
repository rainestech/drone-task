package com.musalasoft.ayoola;

import com.musalasoft.ayoola.dto.ChangeStateRequest;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.dto.LoadDifferentMedicationRequest;
import com.musalasoft.ayoola.dto.LoadMedicationRequest;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.services.DispatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/dispatch")
public class DispatchController {
    private final DispatchService service;

    public DispatchController(DispatchService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<Drones> registerDrone(@Valid @RequestBody Drones drone) {
        return ResponseEntity.ok(service.registerDrone(drone));
    }

    @GetMapping({"/load/{droneSerialNumber}/{medicationCode}/{qty}", "/load/{droneSerialNumber}/{medicationCode}"})
    public ResponseEntity<Drones> loadDrone(@PathVariable String droneSerialNumber, @PathVariable String medicationCode, @PathVariable Optional<Integer> qty) {
        Integer quantity = qty.orElse(null);
        return ResponseEntity.ok(service.loadMedication(medicationCode, droneSerialNumber, quantity));
    }

    @PostMapping("/load")
    public ResponseEntity<Drones> loadDrone(@Valid @RequestBody LoadMedicationRequest request) {
        return ResponseEntity.ok(service.loadMedication(request));
    }

    @PostMapping("/load/diff")
    public ResponseEntity<Drones> loadDrone(@Valid @RequestBody LoadDifferentMedicationRequest request) {
        return ResponseEntity.ok(service.loadMedication(request));
    }

    @PostMapping("/items")
    public ResponseEntity<List<Medications>> loadedItemsOnDrone(@Valid @RequestBody Drones drone) {
        return ResponseEntity.ok(service.getLoadedItems(drone));
    }

    @GetMapping("/items/{droneSerialNumber}")
    public ResponseEntity<List<Medications>> loadedItemsOnDrone(@PathVariable String droneSerialNumber) {
        return ResponseEntity.ok(service.getLoadedItems(droneSerialNumber));
    }

    @GetMapping("/change_state/{droneSerialNumber}/{state}")
    public ResponseEntity<Drones> changeState(@PathVariable String droneSerialNumber, @PathVariable DroneStateOptions state) {
        return ResponseEntity.ok(service.changeState(droneSerialNumber, state));
    }

    @PostMapping("/change_state")
    public ResponseEntity<Drones> changeState(@Valid @RequestBody ChangeStateRequest request) {
        return ResponseEntity.ok(service.changeState(request));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Drones>> availableDronesForLoading() {
        return ResponseEntity.ok(service.getAvailableDronesForLoading());
    }

    @GetMapping("/check_battery/{serialNumber}")
    public ResponseEntity<Integer> getCheckBattery(@PathVariable String serialNumber) {
        Drones drone = service.getDrone(serialNumber);
        return ResponseEntity.ok(drone.getBatteryCapacity());
    }

}
