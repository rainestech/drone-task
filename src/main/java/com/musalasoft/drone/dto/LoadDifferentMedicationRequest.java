package com.musalasoft.drone.dto;

import com.musalasoft.drone.entity.Drones;
import com.musalasoft.drone.entity.Medications;
import lombok.Data;

import java.util.List;

@Data
public class LoadDifferentMedicationRequest {
    private Drones drone;
    private List<Medications> medications;
}
