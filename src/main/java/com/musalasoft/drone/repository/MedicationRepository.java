package com.musalasoft.drone.repository;

import com.musalasoft.drone.entity.Medications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medications, String> {
}
