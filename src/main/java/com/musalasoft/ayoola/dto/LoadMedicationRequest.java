package com.musalasoft.ayoola.dto;

import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import lombok.Data;

@Data
public class LoadMedicationRequest {
    private Drones drone;
    private Medications medication;
    private Integer quantity;
}
