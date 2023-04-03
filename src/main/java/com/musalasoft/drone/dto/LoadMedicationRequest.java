package com.musalasoft.drone.dto;

import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.entity.Medications;
import lombok.Data;

@Data
public class LoadMedicationRequest {
    private Drones drone;
    private Medications medication;
    private Integer quantity;
}
