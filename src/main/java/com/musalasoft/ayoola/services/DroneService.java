package com.musalasoft.ayoola.services;

import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import com.musalasoft.ayoola.repository.DroneBatteryRepository;
import com.musalasoft.ayoola.repository.DroneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class DroneService {
    private final DroneRepository repository;
    private final DroneBatteryRepository batteryRepository;

    public DroneService(DroneRepository repository, DroneBatteryRepository batteryRepository) {
        this.repository = repository;
        this.batteryRepository = batteryRepository;
    }

    /* Get a drone by its serial number
     *
     * @param serialNumber
     * @return Null if not found or Drone record if found
     */
    @Nullable
    public Drones getDrone(String serialNumber) {
        return repository.findById(serialNumber)
                .orElse(null);
    }

    /* Get list of drones by its current state
     *
     * @param desired state
     * @return list of drones in the supplied state
     */
    public List<Drones> getListOfDronesByState(DroneStateOptions state) {
        return repository.findByState(state)
                .orElse(new ArrayList<>());
    }

    /* Get list of drones by available for loading
     *
     * @param desired state
     * @return list of drones in the supplied state
     */
    public List<Drones> getListOfDronesForLoading() {
        List<DroneStateOptions> states = new ArrayList<>();
        states.add(DroneStateOptions.LOADING);
        states.add(DroneStateOptions.IDLE);

        return repository.dronesAvailableForLoading(500, 24, states)
                .orElse(new ArrayList<>());
    }

    /* Check if record of the requested drone exists in the database
     *
     * @param data request Drone
     * @return boolean
     */
    public boolean droneExists(Drones data) {
        return repository.findById(data.getSerialNumber()).isPresent();
    }

    /* Get all available records of drones in the database
     *
     * @return list of Drones
     */
    public List<Drones> getDrones() {
        return repository.findAll();
    }

    /* Save / Register new Drone record
     *
     * @return saved Drone
     */
    public Drones saveDrone(Drones data) {
        return repository.save(data);
    }

    /* Remove Drone record from the database
     *
     * @throws ResponseStatusException
     * @return the removed record
     */
    public Drones deleteDrone(String serialNumber) {
        Drones data = repository.findById(serialNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Drone with serialNumber: " + serialNumber + " not found"));

        batteryRepository.getDroneBySerialNumber(serialNumber)
                .ifPresent(batteryRepository::deleteAll);

        repository.delete(data);
        return data;
    }

    public void unloadMedication(Medications data) {
        for (Drones d : repository.getDroneLoadedWithMedication(data)) {
            d.getLoadedMedications().remove(data);
            repository.save(d);
        }
    }
}
