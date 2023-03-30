package com.musalasoft.ayoola.services;

import com.musalasoft.ayoola.entity.DroneBatteryEventLog;
import com.musalasoft.ayoola.repository.DroneBatteryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DroneBatteryService {
    private final DroneBatteryRepository repository;

    public DroneBatteryService(DroneBatteryRepository batteryRepository) {
        this.repository = batteryRepository;
    }

    /* Get all events
     *
     * @return list of drone battery events
     */
    public List<DroneBatteryEventLog> getBatteryEvent() {
        return repository.findAll();
    }

    /* Save new event
     *
     * @return saved Drone
     */
    public DroneBatteryEventLog saveEvent(DroneBatteryEventLog data) {
        return repository.save(data);
    }

    /* Get list of events for a particular drone
     *
     * @return list of Battery event logs
     */
    public List<DroneBatteryEventLog> getDronesEventLog(String droneSerialNumber) {
        return repository.getDroneBySerialNumber(droneSerialNumber)
                .orElse(new ArrayList<>());
    }
}
