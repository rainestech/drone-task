package com.musalasoft.ayoola.repository;

import com.musalasoft.ayoola.entity.DroneBatteryEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneBatteryRepository extends JpaRepository<DroneBatteryEventLog, Integer> {
    Optional<List<DroneBatteryEventLog>> findByDrone_SerialNumber(String droneSerialNumber);
}
