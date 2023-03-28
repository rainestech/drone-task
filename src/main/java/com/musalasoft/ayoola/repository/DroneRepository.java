package com.musalasoft.ayoola.repository;

import com.musalasoft.ayoola.dto.DroneStateOptions;
import com.musalasoft.ayoola.entity.Drones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DroneRepository extends JpaRepository<Drones, String> {
    Optional<List<Drones>> findByState(DroneStateOptions state);
}
