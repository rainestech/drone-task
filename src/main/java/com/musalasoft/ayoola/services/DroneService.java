package com.musalasoft.ayoola.services;

import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.repository.DroneRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DroneService {
    private final DroneRepository repository;

    public DroneService(DroneRepository repository) {
        this.repository = repository;
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

        repository.delete(data);
        return data;
    }
}
