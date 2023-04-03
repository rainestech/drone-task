package com.musalasoft.drone.repository;

import com.musalasoft.drone.dto.DroneStateOptions;
import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.entity.Medications;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drones, String> {
    Optional<List<Drones>> findByState(DroneStateOptions state);

    List<Drones> findByLoadedMedicationsIsNotNull();

    @Query("select d from Drones d where d.weight < :weight and d.batteryCapacity > :battery and d.state in :state")
    Optional<List<Drones>> dronesAvailableForLoading(@Param("weight") float weight, @Param("battery") int battery, @Param("state") List<DroneStateOptions> state);

    @Query("select d from Drones d where :data member of d.loadedMedications")
    List<Drones> getDroneLoadedWithMedication(Medications data);
}
