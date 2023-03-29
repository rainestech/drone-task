package com.musalasoft.ayoola.services;

import com.musalasoft.ayoola.dto.ChangeStateRequest;
import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.dto.LoadDifferentMedicationRequest;
import com.musalasoft.ayoola.dto.LoadMedicationRequest;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.util.exceptions.DroneNotInLoadableStateException;
import com.musalasoft.ayoola.util.exceptions.LowBatteryException;
import com.musalasoft.ayoola.util.exceptions.MaximumLoadExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DispatchService {
    private final DroneService droneService;
    private final MedicationService medicationService;

    public DispatchService(DroneService droneService, MedicationService medicationService) {
        this.droneService = droneService;
        this.medicationService = medicationService;
    }

    public Drones registerDrone(Drones drone) {
        if (droneService.droneExists(drone))
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Drone Record already in database");

        return droneService.saveDrone(drone);
    }

    public List<Medications> getLoadedItems(String serialNumber) {
        Drones drone = droneService.getDrone(serialNumber);
        if (drone == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone Not Found");

        return drone.getLoadedMedications();
    }

    public List<Medications> getLoadedItems(Drones drone) {
        return getLoadedItems(drone.getSerialNumber());
    }

    public Drones loadMedication(String medicationCode, String droneSerialNumber, Integer quantity) {
        Drones drone = droneService.getDrone(droneSerialNumber);
        Medications medication = medicationService.getMedication(medicationCode);

        if (drone == null || medication == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplied Drone serial number / medication code not found");

        return loadMedication(medication, drone, quantity);
    }

    public Drones loadMedication(LoadMedicationRequest request) {
        return loadMedication(request.getMedication().getCode(), request.getDrone().getSerialNumber(), request.getQuantity());
    }

    public Drones loadMedication(LoadDifferentMedicationRequest request) {
        Drones drone = request.getDrone();
        for (Medications medication : request.getMedications()) {
            drone = loadMedication(medication.getCode(), drone.getSerialNumber(), 1);
        }

        return drone;
    }

    private Drones loadMedication(Medications medication, Drones drone, Integer quantity) {
        if (drone.getLoadedMedications() == null)
            drone.setLoadedMedications(new ArrayList<>());

        if (drone.getState().equals(DroneStateOptions.IDLE) || drone.getState().equals(DroneStateOptions.LOADING)) {
            if (drone.getState().equals(DroneStateOptions.IDLE))
                changeState(DroneStateOptions.LOADING, drone);
        } else
            throw new DroneNotInLoadableStateException("The selected drone not currently IDLE nor in a LOADING state");

        double alreadyLoaded = drone.getLoadedMedications().stream()
                .mapToDouble(Medications::getWeight).sum();

        if (quantity == null)
            quantity = 1;

        if ((medication.getWeight() * quantity + alreadyLoaded) > 500)
            throw new MaximumLoadExceededException(alreadyLoaded + " loaded already on drone " + drone.getSerialNumber());

        drone.getLoadedMedications().add(medication);
        return droneService.saveDrone(drone);
    }

    public Drones changeState(String droneSerialNumber, DroneStateOptions state) {
        Drones drone = droneService.getDrone(droneSerialNumber);
        if (drone == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone Not Found");

        return changeState(state, drone);
    }

    public Drones changeState(ChangeStateRequest request) {
        Drones drone = droneService.getDrone(request.getDrone().getSerialNumber());
        if (drone == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Drone Not Found");

        return changeState(request.getState(), drone);
    }

    private Drones changeState(DroneStateOptions state, Drones drone) {
        if (state.equals(DroneStateOptions.LOADING) && drone.getBatteryCapacity() < 25)
            throw new LowBatteryException("Drones battery capacity below 25%, please charge!");

        drone.setState(state);
        return droneService.saveDrone(drone);
    }

    public List<Drones> getAvailableDronesForLoading() {
        return droneService.getListOfDronesForLoading();
    }
}
