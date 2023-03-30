package com.musalasoft.ayoola.repository;

import com.musalasoft.ayoola.entity.DroneBatteryEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DroneBatteryRepository extends JpaRepository<DroneBatteryEventLog, Integer> {
    @Query("select d from DroneBatteryEventLog d where d.drone.serialNumber = :droneSerialNumber")
    Optional<List<DroneBatteryEventLog>> getDroneBySerialNumber(@Param("droneSerialNumber") String droneSerialNumber);
}
