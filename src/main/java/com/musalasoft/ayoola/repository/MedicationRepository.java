package com.musalasoft.ayoola.repository;

import com.musalasoft.ayoola.entity.Medications;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medications, String> {
}
