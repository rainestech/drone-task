package com.musalasoft.ayoola.repository;

import com.musalasoft.ayoola.entity.Drones;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DroneRepository extends JpaRepository<Drones, String> {
}
