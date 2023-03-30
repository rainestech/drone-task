package com.musalasoft.ayoola;

import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.services.MedicationService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/v1/medications")
public class MedicationController {
    private final MedicationService service;

    public MedicationController(MedicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Medications>> index() {
        return ResponseEntity.ok(service.getMedications());
    }

    @PostMapping
    public ResponseEntity<Medications> saveMedication(@Valid @RequestBody Medications data) {
        if (service.medicationExists(data))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Medication exists in the database");

        return ResponseEntity.ok(service.saveMedication(data));
    }

    @PutMapping
    public ResponseEntity<Medications> editMedication(@Valid @RequestBody Medications data) {
        return ResponseEntity.ok(service.saveMedication(data));
    }

    @DeleteMapping({"/remove/{code}", "/remove/{code}/{force}"})
    public ResponseEntity<Medications> delete(@PathVariable String code, @PathVariable Optional<Integer> force) {
        if (force.isPresent() && force.get() == 1)
            return ResponseEntity.ok(service.deleteMedication(code, true));

        try {
            return ResponseEntity.ok(service.deleteMedication(code));
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.EXPECTATION_FAILED,
                    "Unable to delete loaded medication, Unload from the Drone first");
        }
    }
}
