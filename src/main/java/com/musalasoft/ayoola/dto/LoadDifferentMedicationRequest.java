package com.musalasoft.ayoola.dto;

import com.musalasoft.ayoola.entity.Drones;
import com.musalasoft.ayoola.entity.Medications;
import lombok.Data;

import java.util.List;

@Data
public class LoadDifferentMedicationRequest {
    private Drones drone;
    private List<Medications> medications;
}
